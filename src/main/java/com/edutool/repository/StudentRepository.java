package com.edutool.repository;

import com.edutool.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    
    Optional<Student> findByStudentCode(String studentCode);
    
    Optional<Student> findByUserUserId(Long userId);
    
    Optional<Student> findByGithubUsername(String githubUsername);

    /**
     * Search students with multiple filters (combines fuzzy search and exact match)
     * - Fuzzy search: fullName, studentCode, githubUsername, username, email (LIKE %term%)
     * - If parameter is null/empty, it's ignored (OR 1=1 logic)
     * - Results are intersection of all conditions
     */
    @Query(value = "SELECT s.* FROM students s " +
           "JOIN users u ON s.user_id = u.user_id " +
           "WHERE (:fullName IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :fullName, '%'))) " +
           "AND (:studentCode IS NULL OR LOWER(s.student_code) LIKE LOWER(CONCAT('%', :studentCode, '%'))) " +
           "AND (:githubUsername IS NULL OR LOWER(s.github_username) LIKE LOWER(CONCAT('%', :githubUsername, '%'))) " +
           "AND (:keyword IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(s.student_code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(s.github_username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM students s " +
                   "JOIN users u ON s.user_id = u.user_id " +
                   "WHERE (:fullName IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :fullName, '%'))) " +
                   "AND (:studentCode IS NULL OR LOWER(s.student_code) LIKE LOWER(CONCAT('%', :studentCode, '%'))) " +
                   "AND (:githubUsername IS NULL OR LOWER(s.github_username) LIKE LOWER(CONCAT('%', :githubUsername, '%'))) " +
                   "AND (:keyword IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "     LOWER(s.student_code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "     LOWER(s.github_username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "     LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                   "     LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Student> searchStudentsWithMultipleFilters(
           @Param("fullName") String fullName,
           @Param("studentCode") String studentCode,
           @Param("githubUsername") String githubUsername,
           @Param("keyword") String keyword,
           Pageable pageable);
}