package com.edutool.repository;

import com.edutool.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    
    // Tìm project theo code (chỉ active)
    @Query("SELECT p FROM Project p WHERE p.projectCode = :projectCode AND p.deletedAt IS NULL")
    Optional<Project> findByProjectCode(@Param("projectCode") String projectCode);
    
    // Tìm project theo ID (chỉ active)
    @Query("SELECT p FROM Project p WHERE p.projectId = :projectId AND p.deletedAt IS NULL")
    Optional<Project> findActiveById(@Param("projectId") Integer projectId);
    
    // Kiểm tra project code đã tồn tại chưa (chỉ active)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p " +
           "WHERE p.projectCode = :projectCode AND p.deletedAt IS NULL")
    boolean existsByProjectCode(@Param("projectCode") String projectCode);
    
    // Lấy tất cả project của course (chỉ active)
    @Query("SELECT p FROM Project p WHERE p.course.courseId = :courseId AND p.deletedAt IS NULL")
    List<Project> findByCourse_CourseId(@Param("courseId") Integer courseId);
    
    // Lấy tất cả project active
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    List<Project> findAllActive();
    
    // Kiểm tra project có thuộc course không (chỉ active)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p " +
           "WHERE p.projectId = :projectId AND p.course.courseId = :courseId AND p.deletedAt IS NULL")
    boolean existsByProjectIdAndCourse_CourseId(@Param("projectId") Integer projectId, 
                                                 @Param("courseId") Integer courseId);
    
    // Lấy project đã bị xóa (cho admin xem)
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NOT NULL ORDER BY p.deletedAt DESC")
    List<Project> findDeleted();
    
    // Search project theo code hoặc name (partial match)
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL " +
           "AND (LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Project> searchByCodeOrName(@Param("keyword") String keyword);
}
