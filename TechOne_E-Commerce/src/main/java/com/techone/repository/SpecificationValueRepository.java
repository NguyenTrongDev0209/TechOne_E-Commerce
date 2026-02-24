package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techone.model.SpecificationValue;

@Repository
public interface SpecificationValueRepository extends JpaRepository<SpecificationValue, Integer> {

}
