package com.natlex.repository;

import com.natlex.model.Job;
import com.natlex.model.enums.JobType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends BaseRespository<Job, Long> {

    Optional<Job> findByIdAndType(Long jobId, JobType jobType);
}
