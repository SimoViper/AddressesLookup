package uk.co.gamma.address.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.gamma.address.exception.BlackListReadingException;
import uk.co.gamma.address.model.Address;
import uk.co.gamma.address.model.db.entity.AddressEntity;
import uk.co.gamma.address.model.db.repository.AddressRepository;
import uk.co.gamma.address.model.mapper.AddressMapper;

@ExtendWith(MockitoExtension.class)
class AddressServiceTests {

    @Spy
    private final AddressMapper addressMapper = Mappers.getMapper(AddressMapper.class);
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private PostCodeBlacklistService postCodeBlacklistService;
    @InjectMocks
    private AddressService addressService;

    @DisplayName("getAll() - Given no addresses, then an empty list is returned")
    @Test
    void getAll_when_noAddresses_then_emptyListReturned() {

        given(addressRepository.findAll()).willReturn(List.of());

        List<Address> actual = addressService.getAll(false);

        then(actual).isEmpty();
    }

    @DisplayName("getAll() - Given addresses and include_blacklisted flag true, then the full list is returned")
    @Test
    void getAll_when_multipleAddresses_and_include_blacklisted_true_then_allAddressesReturned() {

        List<AddressEntity> expected = List.of(
                new AddressEntity(1, "King's House", "Kings Road West", "Newbury", "RG14 5BY"),
                new AddressEntity(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new AddressEntity(3, "Holland House", "Bury Street", "London", "EC3A 5AW")
        );

        given(addressRepository.findAll()).willReturn(expected);

        List<Address> actual = addressService.getAll(true);

        // verify
        then(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @DisplayName("getAll() - Given addresses and include_blacklisted flag false, then only non-blacklisted addresses list is returned")
    @Test
    void getAll_when_multipleAddresses_and_include_blacklisted_false_then_only_non_blacklisted_AddressesReturned() throws IOException, InterruptedException {

        List<AddressEntity> addressesEntities = List.of(
                new AddressEntity(1, "King's House", "Kings Road West", "Newbury", "RG14 5BY"),
                new AddressEntity(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new AddressEntity(3, "Holland House", "Bury Street", "London", "EC3A 5AW")
        );

        List<Address> addresses = List.of(
                new Address(1, "King's House", "Kings Road West", "Newbury", "RG14 5BY"),
                new Address(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new Address(3, "Holland House", "Bury Street", "London", "EC3A 5AW")
        );

        List<Address> expected = List.of(
                new Address(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new Address(3, "Holland House", "Bury Street", "London", "EC3A 5AW")
        );

        given(addressRepository.findAll()).willReturn(addressesEntities);
        given(postCodeBlacklistService.filterBlacklistedAddresses(addresses)).willReturn(expected);

        List<Address> actual = addressService.getAll(false);

        // verify
        then(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @DisplayName("getAll() - Given addresses and include_blacklisted flag false, then if InterruptedException is thrown BlackListReadingException with related error message thrown")
    @Test
    void getAll_when_multipleAddresses_and_include_blacklisted_false_then_BlackListReadingExceptionThrown() throws IOException, InterruptedException {

        List<AddressEntity> addressesEntities = List.of(
                new AddressEntity(1, "King's House", "Kings Road West", "Newbury", "RG14 5BY"),
                new AddressEntity(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new AddressEntity(3, "Holland House", "Bury Street", "London", "EC3A 5AW")
        );

        List<Address> addresses = List.of(
                new Address(1, "King's House", "Kings Road West", "Newbury", "RG14 5BY"),
                new Address(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new Address(3, "Holland House", "Bury Street", "London", "EC3A 5AW")
        );

        given(addressRepository.findAll()).willReturn(addressesEntities);
        given(postCodeBlacklistService.filterBlacklistedAddresses(addresses)).willThrow(new InterruptedException());

        // verify
        assertThatThrownBy(() ->
                addressService.getAll(false))
                .isInstanceOf(BlackListReadingException.class)
                .hasMessageContaining("Error Occurred getting Blacklisted addresses.");
    }

    // if  all retrys fail the IOException will be thrown and the message shown, so worth testing I think.
    @DisplayName("getAll() - Given addresses and include_blacklisted flag false, then if IOException is thrown BlackListReadingException with related error message thrown")
    @Test
    void getAll_when_multipleAddresses_and_include_blacklisted_false_then_retry_BlackListReadingExceptionThrown() throws IOException, InterruptedException {

        List<AddressEntity> addressesEntities = List.of(
                new AddressEntity(1, "King's House", "Kings Road West", "Newbury", "RG14 5BY"),
                new AddressEntity(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new AddressEntity(3, "Holland House", "Bury Street", "London", "EC3A 5AW")
        );

        List<Address> addresses = List.of(
                new Address(1, "King's House", "Kings Road West", "Newbury", "RG14 5BY"),
                new Address(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new Address(3, "Holland House", "Bury Street", "London", "EC3A 5AW")
        );

        given(addressRepository.findAll()).willReturn(addressesEntities);
        given(postCodeBlacklistService.filterBlacklistedAddresses(addresses)).willThrow(new IOException());

        // verify
        assertThatThrownBy(() ->
                addressService.getAll(false))
                .isInstanceOf(BlackListReadingException.class)
                .hasMessageContaining("Error Occurred getting Blacklisted addresses, please retry later.");
    }

    @DisplayName("getByPostcode() - Given blacklisted postcode and include_blacklisted flag true, then all Addresses for postcode are returned")
    @Test
    void getByPostcode_when_include_blacklisted_true_all_addresses_for_postcode_returned() {

        List<AddressEntity> expected = List.of(
                new AddressEntity(1, "King's House", "Kings Road West", "Newbury", "RG14 5BY")
        );

        given(addressRepository.findByPostcodeIgnoreCase("RG14 5BY")).willReturn(expected);

        List<Address> actual = addressService.getByPostcode("RG14 5BY",true);

        // verify
        then(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @DisplayName("getByPostcode() - Given blacklisted postcode and include_blacklisted flag false, then empty list returned")
    @Test
    void getByPostcode_when_include_blacklisted_false_empty_list_returned() throws IOException, InterruptedException {

        List<AddressEntity> expected = Collections.emptyList();

        given(postCodeBlacklistService.isAddressBlackListed("RG14 5BY")).willReturn(true);

        List<Address> actual = addressService.getByPostcode("RG14 5BY",false);

        // verify
        then(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @DisplayName("getByPostcode() - Given non-blacklisted postcode and include_blacklisted flag false, then all Addresses for postcode are returned")
    @Test
    void getByPostcode_when_include_blacklisted_false_non_blacklisted_postcode_empty_list_returned() throws IOException, InterruptedException {

        List<AddressEntity> expected = List.of(
                new AddressEntity(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR")
        );

        given(postCodeBlacklistService.isAddressBlackListed("M17 1BR")).willReturn(false);
        given(addressRepository.findByPostcodeIgnoreCase("M17 1BR")).willReturn(expected);

        List<Address> actual = addressService.getByPostcode("M17 1BR",false);

        // verify
        then(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @DisplayName("getByPostcode() - Given postcode and include_blacklisted flag false, then if InterruptedException is thrown BlackListReadingException with related error message thrown")
    @Test
    void getByPostcode_when_include_blacklisted_false_blacklistReadingExceptionThrown() throws IOException, InterruptedException {

        given(postCodeBlacklistService.isAddressBlackListed("M17 1BR")).willThrow(new InterruptedException());

        // verify
        assertThatThrownBy(() ->
                 addressService.getByPostcode("M17 1BR", false))
                .isInstanceOf(BlackListReadingException.class)
                .hasMessageContaining("Error Occurred getting Blacklisted addresses.");
    }

    // if  all retrys fail the IOException will be thrown and the message shown, so worth testing I think.
    @DisplayName("getByPostcode() - Given postcode and include_blacklisted flag false, then if IOException is thrown BlackListReadingException with related error message thrown")
    @Test
    void getByPostcode_when_include_blacklisted_false_retry_blacklistReadingExceptionThrown() throws IOException, InterruptedException {

        given(postCodeBlacklistService.isAddressBlackListed("M17 1BR")).willThrow(new IOException());

        // verify
        assertThatThrownBy(() ->
                addressService.getByPostcode("M17 1BR", false))
                .isInstanceOf(BlackListReadingException.class)
                .hasMessageContaining("Error Occurred getting Blacklisted addresses, please retry later.");
    }
}
