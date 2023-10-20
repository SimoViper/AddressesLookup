package uk.co.gamma.address.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.co.gamma.address.exception.AddressNotFoundException;
import uk.co.gamma.address.exception.BlackListReadingException;
import uk.co.gamma.address.model.Address;
import uk.co.gamma.address.model.db.entity.AddressEntity;
import uk.co.gamma.address.model.db.repository.AddressRepository;
import uk.co.gamma.address.model.mapper.AddressMapper;

/**
 * Address service is a Component class that returns  {@link Address}.
 */
@Service
public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
    public static final String ERROR_OCCURRED_BLACKLISTED = "Error Occurred getting Blacklisted addresses.";
    public static final String ERROR_OCCURRED_BLACKLISTED_RETRY = "Error Occurred getting Blacklisted addresses, please retry later.";

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final PostCodeBlacklistService postCodeBlacklistService;

    /**
     * Constructor.
     *
     * @param addressRepository {@link AddressRepository}.
     * @param addressMapper     {@link AddressMapper}
     * @param postCodeBlacklistService
     */
    @Autowired
    AddressService(AddressRepository addressRepository, AddressMapper addressMapper, PostCodeBlacklistService postCodeBlacklistService) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
        this.postCodeBlacklistService = postCodeBlacklistService;
    }

    /**
     * getAll get all the addresses of the system.
     *
     * @param includeBlacklisted if false blacklisted addresses are not returned.
     * @return List  {@link Address} . Empty if none found.
     */
    @Retryable(retryFor = IOException.class, maxAttempts = 2, backoff = @Backoff(delay = 100))
    public List<Address> getAll(boolean includeBlacklisted) {
        List<Address> addresses = addressMapper.entityToModel(addressRepository.findAll());
        if (!includeBlacklisted && !addresses.isEmpty()) {
            try {
                return postCodeBlacklistService.filterBlacklistedAddresses(addresses);
            } catch (InterruptedException ie) {
                throw new BlackListReadingException(ERROR_OCCURRED_BLACKLISTED);
            } catch (IOException ioe) {
                throw new BlackListReadingException(ERROR_OCCURRED_BLACKLISTED_RETRY);
            }
        }
        return addresses;
    }

    /**
     * getByPostcode find Addresses by their postcode.

     * @param postcode the postcode to search by.
     * @param includeBlacklisted if false and postcode blacklisted an empty list is returned.
     * @return List of  {@link Address}. Empty list if not found.
     */
    @Retryable(retryFor = IOException.class, maxAttempts = 2, backoff = @Backoff(delay = 100))
    public List<Address> getByPostcode(String postcode, boolean includeBlacklisted) {

        try {
            if (!includeBlacklisted && postCodeBlacklistService.isAddressBlackListed(postcode)) {
                return Collections.emptyList();
            }
        } catch (InterruptedException ie) {
            throw new BlackListReadingException(ERROR_OCCURRED_BLACKLISTED);
        } catch (IOException ioe) {
            throw new BlackListReadingException(ERROR_OCCURRED_BLACKLISTED_RETRY);
        }

        return addressMapper.entityToModel(addressRepository.findByPostcode(postcode));
    }

    /**
     * findById find an address by Id.

     * @param id to search on.

     * @return  {@link Address} Optional.
     */
    public Optional<Address> getById(Integer id) {
        return addressRepository.findById(id).map(addressMapper::entityToModel);
    }

    /**
     * create Address and save to db.

     * @param address   {@link Address} to save

     * @return  {@link Address}
     */
    public Address create(Address address) {
        logger.info("Adding new address: {}", address);
        return save(addressMapper.modelToEntity(address));
    }

    /**
     * update an Address.

     * @param id of Address

     * @param address  {@link Address}

     * @return {@link Address}
     */
    public Address update(Integer id, Address address) {
        return addressRepository.findById(id).map(addressEntity -> {
            logger.info("Updating existing address {}: {}", id, address);
            addressEntity.setBuilding(address.building());
            addressEntity.setStreet(address.street());
            addressEntity.setTown(address.town());
            addressEntity.setPostcode(address.postcode());
            return save(addressEntity);
        }).orElseThrow(() -> new AddressNotFoundException(id));

    }

    /**
     * delete an address by id.

     * @param id of Address to delete
     */
    public void delete(Integer id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException(id);
        }
        logger.info("Deleting address {}", id);
        addressRepository.deleteById(id);
    }

    /**
     * save an  {@link Address}.

     * @param addressEntity  {@link AddressEntity}

     * @return  {@link Address}
     */
    private Address save(AddressEntity addressEntity) {
        return addressMapper.entityToModel(addressRepository.save(addressEntity));
    }
}
