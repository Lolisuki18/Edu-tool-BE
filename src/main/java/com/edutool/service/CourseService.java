package com.edutool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.edutool.repository.CourseRepository;
import com.edutool.repository.SemesterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    @Autowired
    private final CourseRepository courseRepository;
    @Autowired
    private final SemesterRepository semesterRepository;

    

}
