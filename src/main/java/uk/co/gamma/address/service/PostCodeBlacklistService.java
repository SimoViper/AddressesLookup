package uk.co.gamma.address.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.co.gamma.address.model.Address;
import uk.co.gamma.address.model.Zone;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Postcode Blacklist service is a Service class that performs checks on postcodes
 * and filters Address classes for postcode.
 */
@Service
public class PostCodeBlacklistService {

    private final BlackListService blackListService;

    public PostCodeBlacklistService(BlackListService blackListService) {
        this.blackListService = blackListService;
    }

    /**
     * filterBlacklistedAddresses filters blacklisted addresses.
     *
     * @param addresses list of addresses to filter.
     * @return List  {@link Address} filtered addresses.
     */
    @Retryable(retryFor = IOException.class, maxAttempts = 2, backoff = @Backoff(delay = 100))
    public List<Address> filterBlacklistedAddresses(List<Address> addresses) throws IOException, InterruptedException {

        List<Zone> blackListedZones = blackListService.getAll();

        return addresses.stream()
                .filter(address -> blackListedZones.stream()
                        .noneMatch(zone -> address.postcode().equalsIgnoreCase(zone.getPostCode())))
                .collect(Collectors.toList());
    }

    /**
     * isAddressBlackListed checks if postcode blacklisted.
     *
     * @param postcode postcode to check.
     * @return {@link boolean} true if the postcode is blacklisted.
     */
    @Retryable(retryFor = IOException.class, maxAttempts = 2, backoff = @Backoff(delay = 100))
    public boolean isAddressBlackListed(String postcode) throws IOException, InterruptedException {

        List<Zone> blackListedZones = blackListService.getAll();

        return blackListedZones.stream().anyMatch(zone -> zone.getPostCode().equalsIgnoreCase(postcode));
    }
}
