package uk.co.gamma.address.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.gamma.address.model.Address;
import uk.co.gamma.address.model.Zone;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class PostCodeServiceTest {

    @Mock
    private BlackListService blackListService;

    @InjectMocks
    private PostCodeService postCodeService;

    @DisplayName("filterBlacklistedAddresses() - Given addresses, then blacklisted postcodes filtered")
    @Test
    void filterBlacklistedAddresses_when_postcode_blacklisted_address_not_returned() throws IOException, InterruptedException {

        List<Address> addresses = List.of(
                new Address(1, "King's House", "Kings Road West","Newbury", "RG14 5BY"),
                new Address(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new Address(3, "Holland House", "Bury Street", "London", "RG14 7DH"));

        List<Address> expected = List.of(
                new Address(1, "King's House", "Kings Road West","Newbury", "RG14 5BY"),
                new Address(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"));

        List<Zone> zones = List.of(new Zone("rg14 7dh"), new Zone("rg6 1ps"));

        //given
        given(blackListService.getAll()).willReturn(zones);

        List<Address> actual = postCodeService.filterBlacklistedAddresses(addresses);

        // verify
        then(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @DisplayName("filterBlacklistedAddresses() - Given addresses, then if not blacklisted postcodes all addresses returned")
    @Test
    void filterBlacklistedAddresses_when_postcode_not_blacklisted_all_addresses_returned() throws IOException, InterruptedException {

        List<Address> addresses = List.of(
                new Address(1, "King's House", "Kings Road West","Newbury", "RG14 5BY"),
                new Address(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new Address(3, "Holland House", "Bury Street", "London", "EC3A 5AW"));

        List<Address> expected = List.of(
                new Address(1, "King's House", "Kings Road West","Newbury", "RG14 5BY"),
                new Address(2, "The Malthouse", "Elevator Road", "Manchester", "M17 1BR"),
                new Address(3, "Holland House", "Bury Street", "London", "EC3A 5AW"));

        List<Zone> zones = List.of(new Zone("rg14 7dh"), new Zone("rg6 1ps"));

        //given
        given(blackListService.getAll()).willReturn(zones);

        List<Address> actual = postCodeService.filterBlacklistedAddresses(addresses);

        // verify
        then(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @DisplayName("isAddressBlackListed() - Given postcode, then if postcode is blacklisted true is returned")
    @Test
    void isAddressBlackListed_when_postcode_is_blacklisted_true_is_returned() throws IOException, InterruptedException {


        List<Zone> zones = List.of(new Zone("rg14 7dh"), new Zone("rg6 1ps"));

        //given
        given(blackListService.getAll()).willReturn(zones);

        boolean actual = postCodeService.isAddressBlackListed("RG14 7DH");

        // verify
        then(actual).isEqualTo(true);
    }

    @DisplayName("isAddressBlackListed() - Given postcode, then if postcode is not blacklisted false is returned")
    @Test
    void isAddressBlackListed_when_postcode_is_not_blacklisted_false_is_returned() throws IOException, InterruptedException {


        List<Zone> zones = List.of(new Zone("rg14 7dh"), new Zone("rg6 1ps"));

        //given
        given(blackListService.getAll()).willReturn(zones);

        boolean actual = postCodeService.isAddressBlackListed("RG14 5BY");

        // verify
        then(actual).isEqualTo(false);
    }
}

