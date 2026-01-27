package com.edutool.repository;

import com.edutool.model.Lecturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Integer> {
    
    Optional<Lecturer> findByStaffCode(String staffCode);
    
    Optional<Lecturer> findByUserUserId(Long userId);
    
    @Query("SELECT l FROM Lecturer l WHERE LOWER(l.user.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')) OR LOWER(l.staffCode) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    Page<Lecturer> searchByFullNameOrStaffCode(@Param("fullName") String fullName, Pageable pageable);
}
