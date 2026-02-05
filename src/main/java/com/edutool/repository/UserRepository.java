package com.edutool.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edutool.model.Role;
import com.edutool.model.User;
import com.edutool.model.UserStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByEmailOrUsername(String email, String username);

    /**
     * Search users with multiple filters (combines fuzzy search and exact match)
     * - Fuzzy search: username, email, fullName, keyword (LIKE %term%)
     * - Exact match: role, status (= value)
     * - If parameter is null/empty, it's ignored (acts as OR 1=1)
     * - Results are intersection of all conditions
     */
    @Query(value = "SELECT * FROM users u WHERE " +
           "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:fullName IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :fullName, '%'))) " +
           "AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:status IS NULL OR u.status = :status) ",
           nativeQuery = true,
       countQuery = "SELECT COUNT(*) FROM users u WHERE " +
           "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:fullName IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :fullName, '%'))) " +
           "AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:status IS NULL OR u.status = :status) ")
    Page<User> searchUsersWithMultipleFilters(
           @Param("username") String username,
           @Param("email") String email,
           @Param("fullName") String fullName,
           @Param("keyword") String keyword,
           @Param("role") String role,
           @Param("status") String status,
           Pageable pageable);

}