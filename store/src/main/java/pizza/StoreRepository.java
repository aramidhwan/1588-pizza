package pizza;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="stores", path="stores")
public interface StoreRepository extends PagingAndSortingRepository<Store, Long>{

    List<Store> findByRegionNmOpenYN(String regionNm, Boolean openYn);
}
