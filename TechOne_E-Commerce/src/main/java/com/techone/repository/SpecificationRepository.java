package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techone.model.Specification;

@Repository
public interface SpecificationRepository extends JpaRepository<Specification, Integer> {

}
