package com.techone.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.user.entity.Account;
import com.techone.domain.user.entity.Favourite;
import com.techone.domain.post.entity.Post;
import com.techone.domain.product.entity.Variant;
import org.springframework.data.repository.query.Param;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Integer> {
    List<Favourite> findByAccount(Account account);

    Optional<Favourite> findByAccountAndPost(Account account, Post post);

    Optional<Favourite> findByAccountAndVariant(Account account, Variant variant);

    Page<Favourite> findByAccountIdAndPostIsNotNull(Integer accountId, Pageable pageable);

    Page<Favourite> findByAccountIdAndVariantIsNotNull(Integer accountId, Pageable pageable);

    long countByPost(Post post);

    boolean existsByAccountAndPost(Account account, Post post);

    boolean existsByAccountAndVariant(Account account, Variant variant);

    boolean existsByVariant(Variant variant);

    void deleteByVariant(Variant variant);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Favourite f WHERE f.account = :account " +
            "AND ((f.variant IS NOT NULL AND f.variant.status = true AND f.variant.product.status = true) " +
            "OR (f.post IS NOT NULL AND f.post.status = true)) " +
            "ORDER BY f.id DESC")
    Page<Favourite> findActiveFavouritesByAccount(@Param("account") Account account, Pageable pageable);
}
