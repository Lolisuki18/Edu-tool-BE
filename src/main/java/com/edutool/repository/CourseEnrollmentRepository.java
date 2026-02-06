package com.edutool.repository;

import com.edutool.model.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Integer> {
    
    // Kiểm tra sinh viên đã enroll vào course chưa (chỉ active)
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM CourseEnrollment e " +
           "WHERE e.student.studentId = :studentId AND e.course.courseId = :courseId AND e.deletedAt IS NULL")
    boolean existsByStudent_StudentIdAndCourse_CourseId(@Param("studentId") Integer studentId, 
                                                         @Param("courseId") Integer courseId);
    
    // Lấy enrollment của sinh viên trong course (chỉ active)
    @Query("SELECT e FROM CourseEnrollment e " +
           "WHERE e.student.studentId = :studentId AND e.course.courseId = :courseId AND e.deletedAt IS NULL")
    Optional<CourseEnrollment> findByStudent_StudentIdAndCourse_CourseId(@Param("studentId") Integer studentId, 
                                                                          @Param("courseId") Integer courseId);
    
    // Lấy tất cả enrollment của course (chỉ active)
    @Query("SELECT e FROM CourseEnrollment e WHERE e.course.courseId = :courseId AND e.deletedAt IS NULL")
    List<CourseEnrollment> findByCourse_CourseId(@Param("courseId") Integer courseId);
    
    // Lấy tất cả enrollment của sinh viên (chỉ active)
    @Query("SELECT e FROM CourseEnrollment e WHERE e.student.studentId = :studentId AND e.deletedAt IS NULL")
    List<CourseEnrollment> findByStudent_StudentId(@Param("studentId") Integer studentId);
    
    // Kiểm tra sinh viên đã có project trong course chưa (chỉ active)
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM CourseEnrollment e " +
           "WHERE e.student.studentId = :studentId AND e.course.courseId = :courseId " +
           "AND e.project IS NOT NULL AND e.deletedAt IS NULL AND e.removedFromProjectAt IS NULL")
    boolean hasProjectInCourse(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);
    
    // Lấy tất cả sinh viên trong project (chỉ active)
    @Query("SELECT e FROM CourseEnrollment e WHERE e.project.projectId = :projectId " +
           "AND e.deletedAt IS NULL AND e.removedFromProjectAt IS NULL")
    List<CourseEnrollment> findByProject_ProjectId(@Param("projectId") Integer projectId);
    
    // Đếm số sinh viên active trong project (không tính removed)
    @Query("SELECT COUNT(e) FROM CourseEnrollment e WHERE e.project.projectId = :projectId " +
           "AND e.deletedAt IS NULL AND e.removedFromProjectAt IS NULL")
    long countByProject_ProjectId(@Param("projectId") Integer projectId);
    
    // Đếm TẤT CẢ sinh viên có reference đến project (bao gồm cả removed - để validate xóa project)
    @Query("SELECT COUNT(e) FROM CourseEnrollment e WHERE e.project.projectId = :projectId " +
           "AND e.deletedAt IS NULL")
    long countAllMembersByProject(@Param("projectId") Integer projectId);
    
    // Lấy lịch sử sinh viên đã bị xóa khỏi project
    @Query("SELECT e FROM CourseEnrollment e WHERE e.project.projectId = :projectId " +
           "AND e.deletedAt IS NULL AND e.removedFromProjectAt IS NOT NULL " +
           "ORDER BY e.removedFromProjectAt DESC")
    List<CourseEnrollment> findRemovedStudentsByProject(@Param("projectId") Integer projectId);
}
