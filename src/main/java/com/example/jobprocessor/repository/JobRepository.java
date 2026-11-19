package com.example.jobprocessor.repository;

import com.example.jobprocessor.entity.Job;
import com.example.jobprocessor.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findByStatusIn(List<JobStatus> statuses);
}
