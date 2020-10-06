package com.natlex.service;

import com.natlex.exсeptions.DataNotFoundException;
import com.natlex.exсeptions.ExportErrorResultException;
import com.natlex.exсeptions.ExportInProgressException;
import com.natlex.model.GeologicalClass;
import com.natlex.model.Job;
import com.natlex.model.Section;
import com.natlex.model.enums.JobResultStatus;
import com.natlex.model.enums.JobType;
import com.natlex.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final SectionService sectionService;
    private final StorageService storageService;

    @Autowired
    public JobService(JobRepository jobRepository, SectionService sectionService
            , StorageService storageService) {
        this.jobRepository = jobRepository;
        this.sectionService = sectionService;
        this.storageService = storageService;
    }

    public JobResultStatus getJobStatus(Long jobId, JobType jobType) {
        return jobRepository.findByIdAndType(jobId,jobType)
                            .orElseThrow(()-> new DataNotFoundException("The job is not found. Wrong Job ID."))
                            .getStatus();
    }

    public List<Job> getAllJobs(int pageSize, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return jobRepository.findAll(pageable).getContent();
    }

    public Job startNewJob(JobType jobType) {
        Job newJob = new Job(jobType);
        newJob.setStatus(JobResultStatus.IN_PROGRESS);
        newJob.setCreatedDate(LocalDateTime.now());
        jobRepository.save(newJob);

        log.info("{} - NEW {} JOB #{} STARTED.", newJob.getCreatedDate(), newJob.getType(), newJob.getId());

        return newJob;
    }

    @Async
    public void importXLS(InputStream inputStream, Job importJob, String importFileName) {

        try {
            HSSFWorkbook xlsFile = new HSSFWorkbook(inputStream);
            inputStream.close();
            HSSFSheet sheet = xlsFile.getSheetAt(0);

            for (int r = 1; r < sheet.getPhysicalNumberOfRows(); r++) {
                HSSFRow currentRow = sheet.getRow(r);
                if (currentRow == null) {
                    continue;
                }
                String tempSectionName = currentRow.getCell(0).getStringCellValue();
                Section blank = sectionService.saveBlankSection(tempSectionName);

                List<GeologicalClass> tempGeologicalClasses = new LinkedList<>();

                for (int c = 1; c < currentRow.getLastCellNum(); c += 2) {
                    HSSFCell currentNameCell = currentRow.getCell(c);
                    if(currentNameCell == null) {
                        continue;
                    }
                    String geologicalClassName = currentNameCell.getStringCellValue();

                    HSSFCell currentCodeCell = currentRow.getCell(c + 1);
                    if(currentCodeCell == null) {
                        continue;
                    }
                    String geologicalClassCode = currentCodeCell.getStringCellValue();
                    tempGeologicalClasses.add(new GeologicalClass(geologicalClassName
                            , geologicalClassCode, blank));
                }
                blank.setGeologicalClasses(tempGeologicalClasses);
                sectionService.saveSection(blank);
            }
            importJob.setStatus(JobResultStatus.DONE);
            jobRepository.save(importJob);

            log.info("Parsing {} was successfully finished. JOB #{} is DONE"
                    , importFileName, importJob.getId());

        } catch (Exception e) {
            importJob.setStatus(JobResultStatus.ERROR);
            jobRepository.save(importJob);

            log.error("JOB #{} is FAILED.", importJob.getId());
        }
    }

    @Async
    public void exportXLS(Job exportJob) {

        try {
            List<Section> sections = sectionService.getAllSections();

            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            HSSFSheet sheet = hssfWorkbook.createSheet("Sections");

            HSSFRow headerRow = sheet.createRow(0);
            addCellToRow(headerRow,"Section name");

            for (Section s : sections) {
                HSSFRow currentRow = sheet.createRow(sheet.getLastRowNum() + 1);
                addCellToRow(currentRow,s.getName());

                List<GeologicalClass> geologicalClasses = s.getGeologicalClasses();

                for (GeologicalClass gc : geologicalClasses) {
                    if (currentRow.getLastCellNum() == headerRow.getLastCellNum()) {
                        int index = Math.floorDiv(headerRow.getLastCellNum(), 2) + 1;
                        addCellToRow(headerRow,String.format("Class %d name", index));
                        addCellToRow(headerRow,String.format("Class %d code", index));
                    }
                    addCellToRow(currentRow,(gc.getName()));
                    addCellToRow(currentRow,(gc.getCode()));
                }
            }

            String exportFileName = exportJob.getId().toString() + ".xls";
            storageService.storeExportFile(hssfWorkbook, exportFileName);

            exportJob.setStatus(JobResultStatus.DONE);
            jobRepository.save(exportJob);

            log.info("File {} was successfully generated. JOB #{} is DONE"
                    ,exportFileName, exportJob.getId());

        } catch (Exception e) {
            exportJob.setStatus(JobResultStatus.ERROR);
            jobRepository.save(exportJob);

            log.error("JOB #{} is FAILED.", exportJob.getId());
        }
    }

    public Resource downloadXLSUrlResource(Long jobId) {
        if (getJobStatus(jobId, JobType.EXPORT).equals(JobResultStatus.DONE)) {
            return storageService.loadUrlResource(jobId.toString() + ".xls");
        }
        if (getJobStatus(jobId, JobType.EXPORT).equals(JobResultStatus.IN_PROGRESS)) {
            throw new ExportInProgressException("Export is still in progress now, try again later!");
        }
        throw new ExportErrorResultException("Export job ended with errors, create new export!");
    }

    private void addCellToRow(HSSFRow row, String value) {
        HSSFCell newCell = row.createCell(Math.max(row.getLastCellNum(), 0),CellType.STRING);
        newCell.setCellValue(value);
    }
}
