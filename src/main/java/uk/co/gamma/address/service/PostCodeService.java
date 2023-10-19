package uk.co.gamma.address.service;

import org.springframework.stereotype.Service;
import uk.co.gamma.address.model.Address;
import uk.co.gamma.address.model.Zone;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostCodeService {

    private final BlackListService blackListService;


    public PostCodeService(BlackListService blackListService) {
        this.blackListService = blackListService;
    }

    public List<Address> filterBlacklistedAddresses(List<Address> addresses) throws IOException, InterruptedException {

        List<Zone> blackListedZones = blackListService.getAll();

        return addresses.stream()
                .filter(address -> blackListedZones.stream()
                        .noneMatch(zone -> address.postcode().equalsIgnoreCase(zone.getPostCode())))
                .collect(Collectors.toList());
    }

    public boolean isAddressBlackListed(String postcode) throws IOException, InterruptedException {

        List<Zone> blackListedZones = blackListService.getAll();

        return blackListedZones.stream().anyMatch(zone -> zone.getPostCode().equalsIgnoreCase(postcode));
    }
}
