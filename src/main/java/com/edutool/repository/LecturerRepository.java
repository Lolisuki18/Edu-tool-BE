package com.edutool.repository;

import com.edutool.model.Lecturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {
    
    Optional<Lecturer> findByStaffCode(String staffCode);
    
    Optional<Lecturer> findByUserUserId(Long userId);

    /**
     * Search lecturers with multiple filters (combines fuzzy search)
     * - Fuzzy search: fullName, staffCode, username, email (LIKE %term%)
     * - If parameter is null/empty, it's ignored
     * - Results are intersection of all conditions
     */
    @Query(value = "SELECT l.* FROM lecturers l " +
           "JOIN users u ON l.user_id = u.user_id " +
           "WHERE (:fullName IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :fullName, '%'))) " +
           "AND (:staffCode IS NULL OR LOWER(l.staff_code) LIKE LOWER(CONCAT('%', :staffCode, '%'))) " +
           "AND (:keyword IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(l.staff_code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM lecturers l " +
                   "JOIN users u ON l.user_id = u.user_id " +
                   "WHERE (:fullName IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :fullName, '%'))) " +
                   "AND (:staffCode IS NULL OR LOWER(l.staff_code) LIKE LOWER(CONCAT('%', :staffCode, '%'))) " +
                   "AND (:keyword IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "     LOWER(l.staff_code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "     LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "     LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Lecturer> searchLecturersWithMultipleFilters(
           @Param("fullName") String fullName,
           @Param("staffCode") String staffCode,
           @Param("keyword") String keyword,
           Pageable pageable);
}
