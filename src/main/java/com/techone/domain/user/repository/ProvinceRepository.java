package com.techone.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.domain.user.entity.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
}
