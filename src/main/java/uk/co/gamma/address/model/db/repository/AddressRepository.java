package uk.co.gamma.address.model.db.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.gamma.address.model.db.entity.AddressEntity;

public interface AddressRepository extends JpaRepository<AddressEntity, Integer> {

    List<AddressEntity> findByPostcodeIgnoreCase(String postcode);

    void delete(AddressEntity address);
}
