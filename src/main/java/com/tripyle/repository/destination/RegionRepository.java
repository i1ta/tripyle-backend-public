package com.tripyle.repository.destination;

import com.tripyle.model.entity.destination.Nation;
import com.tripyle.model.entity.destination.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    Region findRegionById(Long id);

    Region findRegionByName(String regionName);

    List<Region> findByNationId(Long id);
}
