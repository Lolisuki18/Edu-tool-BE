package com.edutool.repository;

import com.edutool.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    
    Optional<Student> findByStudentCode(String studentCode);
    
    Optional<Student> findByUserUserId(Long userId);
    
    Optional<Student> findByGithubUsername(String githubUsername);
    
    @Query("SELECT s FROM Student s WHERE LOWER(s.user.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')) OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :fullName, '%')) OR LOWER(s.githubUsername) LIKE LOWER(CONCAT('%', :fullName, '%'))")
    Page<Student> searchByFullNameOrStudentCodeOrGithubUsername(@Param("fullName") String fullName, Pageable pageable);
}
