package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.model.Favourite;
import com.techone.model.Variant;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Integer> {
    void deleteByVariant(Variant variant);
}
