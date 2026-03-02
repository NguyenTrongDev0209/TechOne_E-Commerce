package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.model.Favourite;
import com.techone.model.Variant;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Integer> {
    void deleteByVariant(com.techone.model.Variant variant);

    java.util.Optional<Favourite> findByAccountAndVariant(com.techone.model.Account account,
            com.techone.model.Variant variant);

    java.util.Optional<Favourite> findByAccountAndPost(com.techone.model.Account account, com.techone.model.Post post);

    java.util.List<Favourite> findByAccount(com.techone.model.Account account);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Favourite f WHERE f.account = :account " +
            "AND f.variant.status = true " +
            "AND f.variant.product.status = true " +
            "AND f.variant.stock > 0")
    org.springframework.data.domain.Page<Favourite> findActiveFavouritesByAccount(
            @org.springframework.data.repository.query.Param("account") com.techone.model.Account account,
            org.springframework.data.domain.Pageable pageable);
}
