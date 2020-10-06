package com.natlex.controller;

import com.natlex.model.Job;
import com.natlex.model.enums.JobType;
import com.natlex.service.JobService;
import com.natlex.service.StorageService;
import com.natlex.util.Constants;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
public class JobRestController {

    private final JobService jobService;
    private final StorageService storageService;

    @Autowired
    public JobRestController(JobService jobService, StorageService storageService) {
        this.jobService = jobService;
        this.storageService = storageService;
    }

    @GetMapping(value = "/list-jobs")
    public ResponseEntity<?> getJobList(
            @RequestParam(value = "size", required = false) Integer pageSize,
            @RequestParam(value = "page", required = false) Integer pageNumber) {
        if (pageSize == null) {
            pageSize = Constants.PAGE_SIZE;
        }
        if (pageNumber == null) {
            pageNumber = Constants.PAGE_NUMBER;
        }
        return new ResponseEntity<>(jobService.getAllJobs(pageSize, pageNumber), HttpStatus.OK);
    }

    @SneakyThrows
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importXLSFile(@RequestParam("file") MultipartFile file) {
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Job newImportJob = jobService.startNewJob(JobType.IMPORT);
        String importFileName = newImportJob.getId().toString() + ".xls";
        jobService.importXLS(file.getInputStream(), newImportJob, importFileName);
        storageService.storeImportFile(file.getInputStream(), importFileName);
        return new ResponseEntity<>(newImportJob.getId(), HttpStatus.OK);
    }

    @GetMapping("/import/{jobId}")
    public ResponseEntity<?> getImportStatus(@PathVariable Long jobId) {
        if (jobId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(jobService.getJobStatus(jobId, JobType.IMPORT), HttpStatus.OK);
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportSections() {
        Job newExportJob = jobService.startNewJob(JobType.EXPORT);
        jobService.exportXLS(newExportJob);
        return new ResponseEntity<>(newExportJob.getId(), HttpStatus.OK);
    }

    @GetMapping("/export/{jobId}")
    public ResponseEntity<?> getExportStatus(@PathVariable Long jobId) {
        if (jobId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(jobService.getJobStatus(jobId, JobType.EXPORT), HttpStatus.OK);
    }

    @GetMapping(value = "/export/{jobId}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> getXLSFileByJobId(@PathVariable Long jobId) {
        if (jobId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Resource resource = jobService.downloadXLSUrlResource(jobId);
        return ResponseEntity.ok()
                             .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                             .body(resource);
        }
}
