package com.edutool.repository;

import com.edutool.model.ReportDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportDetailRepository extends JpaRepository<ReportDetail, Integer> {
    
    Page<ReportDetail> findByPeriodicReport_ReportId(Integer reportId, Pageable pageable);
    
    Page<ReportDetail> findByStudent_StudentId(Integer studentId, Pageable pageable);
    
    Page<ReportDetail> findByProject_ProjectId(Integer projectId, Pageable pageable);
    
    Page<ReportDetail> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT rd FROM ReportDetail rd WHERE " +
           "rd.periodicReport.reportId = :reportId AND rd.student.studentId = :studentId")
    Optional<ReportDetail> findByReportAndStudent(@Param("reportId") Integer reportId, 
                                                   @Param("studentId") Integer studentId);
    
    @Query("SELECT rd FROM ReportDetail rd WHERE " +
           "rd.periodicReport.reportId = :reportId AND rd.project.projectId = :projectId")
    Page<ReportDetail> findByReportAndProject(@Param("reportId") Integer reportId,
                                               @Param("projectId") Integer projectId,
                                               Pageable pageable);
    
    boolean existsByPeriodicReport_ReportIdAndStudent_StudentId(Integer reportId, Integer studentId);
}
