package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.techone.model.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
}
