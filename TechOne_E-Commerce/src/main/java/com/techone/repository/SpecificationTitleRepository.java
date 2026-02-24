package com.techone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techone.model.SpecificationTitle;

@Repository
public interface SpecificationTitleRepository extends JpaRepository<SpecificationTitle, Integer> {

}
