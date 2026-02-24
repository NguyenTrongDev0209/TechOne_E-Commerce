package com.techone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techone.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

	// lay danh sách con hoạt đông(DSL)
	public List<Category> findByStatus(boolean status);

	public org.springframework.data.domain.Page<Category> findByType(Boolean type,
			org.springframework.data.domain.Pageable pageable);

	public List<Category> findByType(Boolean type);

	public List<Category> findByTypeAndStatus(Boolean type, Boolean status);

	public org.springframework.data.domain.Page<Category> findByTypeAndParentIsNull(Boolean type,
			org.springframework.data.domain.Pageable pageable);

	public List<Category> findByTypeAndParentIsNull(Boolean type);

	public List<Category> findByTypeAndParentIsNullAndStatus(Boolean type, Boolean status);

	boolean existsByName(String name);

	boolean existsBySlug(String slug);

	Category findByName(String name);

	Category findByNameAndType(String name, Boolean type);

	Category findBySlug(String slug);

	Category findBySlugAndType(String slug, Boolean type);

	boolean existsByNameAndType(String name, Boolean type);

	boolean existsBySlugAndType(String slug, Boolean type);

	@Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId")
	int countByParentId(@Param("parentId") Integer parentId);

	@Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
	int countProducts(@Param("categoryId") Integer categoryId);

	@Query("SELECT COUNT(p) FROM Post p WHERE p.category.id = :categoryId")
	int countPosts(@Param("categoryId") Integer categoryId);

	@Query("SELECT c FROM Category c WHERE c.type = :type AND c.parent IS NULL "
			+ "AND (:keyword IS NULL OR c.name LIKE %:keyword%) "
			+ "AND (:fromDate IS NULL OR c.createAt >= :fromDate) "
			+ "AND (:toDate IS NULL OR c.createAt <= :toDate)")
	org.springframework.data.domain.Page<Category> search(@Param("keyword") String keyword,
			@Param("fromDate") java.time.LocalDate fromDate, @Param("toDate") java.time.LocalDate toDate,
			@Param("type") Boolean type, org.springframework.data.domain.Pageable pageable);
}
