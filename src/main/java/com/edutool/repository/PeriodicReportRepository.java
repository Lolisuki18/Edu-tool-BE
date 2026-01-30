package com.edutool.repository;

import com.edutool.model.PeriodicReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodicReportRepository extends JpaRepository<PeriodicReport, Integer> {
    
    // Tìm periodic report theo ID (chỉ active)
    @Query("SELECT pr FROM PeriodicReport pr WHERE pr.reportId = :reportId AND pr.status = 'ACTIVE'")
    Optional<PeriodicReport> findActiveById(@Param("reportId") Integer reportId);
    
    // Lấy tất cả periodic report của course (chỉ active)
    @Query("SELECT pr FROM PeriodicReport pr WHERE pr.course.courseId = :courseId AND pr.status = 'ACTIVE'")
    Page<PeriodicReport> findByCourse_CourseId(@Param("courseId") Integer courseId, Pageable pageable);
    
    // Lấy tất cả periodic report (chỉ active)
    @Query("SELECT pr FROM PeriodicReport pr WHERE pr.status = 'ACTIVE'")
    Page<PeriodicReport> findAllActive(Pageable pageable);
    
    // Tìm periodic report theo khoảng thời gian (chỉ active)
    @Query("SELECT pr FROM PeriodicReport pr " +
           "WHERE pr.course.courseId = :courseId " +
           "AND pr.reportFromDate >= :fromDate " +
           "AND pr.reportToDate <= :toDate " +
           "AND pr.status = 'ACTIVE'")
    Page<PeriodicReport> findByCourseAndDateRange(@Param("courseId") Integer courseId,
                                                   @Param("fromDate") LocalDateTime fromDate,
                                                   @Param("toDate") LocalDateTime toDate,
                                                   Pageable pageable);
    
    // Tìm periodic report đang mở (submit date) (chỉ active)
    @Query("SELECT pr FROM PeriodicReport pr " +
           "WHERE pr.course.courseId = :courseId " +
           "AND :currentDate >= pr.submitStartAt " +
           "AND :currentDate <= pr.submitEndAt " +
           "AND pr.status = 'ACTIVE'")
    Page<PeriodicReport> findOpenReportsByCourse(@Param("courseId") Integer courseId,
                                                  @Param("currentDate") LocalDateTime currentDate,
                                                  Pageable pageable);
    
    // Kiểm tra periodic report có tồn tại không (chỉ active)
    @Query("SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END FROM PeriodicReport pr " +
           "WHERE pr.reportId = :reportId AND pr.status = 'ACTIVE'")
    boolean existsActiveById(@Param("reportId") Integer reportId);
    
    // Tìm periodic report overlap với khoảng thời gian mới (chỉ active)
    @Query("SELECT pr FROM PeriodicReport pr " +
           "WHERE pr.course.courseId = :courseId " +
           "AND ((pr.reportFromDate <= :toDate AND pr.reportToDate >= :fromDate) " +
           "OR (pr.submitStartAt <= :submitEndAt AND pr.submitEndAt >= :submitStartAt)) " +
           "AND pr.status = 'ACTIVE'")
    List<PeriodicReport> findOverlappingReports(@Param("courseId") Integer courseId,
                                                 @Param("fromDate") LocalDateTime fromDate,
                                                 @Param("toDate") LocalDateTime toDate,
                                                 @Param("submitStartAt") LocalDateTime submitStartAt,
                                                 @Param("submitEndAt") LocalDateTime submitEndAt);
    
    // Lấy periodic report đã xóa (inactive)
    @Query("SELECT pr FROM PeriodicReport pr WHERE pr.status = 'INACTIVE'")
    Page<PeriodicReport> findInactive(Pageable pageable);
}
