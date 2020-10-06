package com.natlex.service;

import com.natlex.exсeptions.DataNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class StorageService {

    @Value("${storage.import}")
    private Path importStorage;

    @Value("${storage.export}")
    private Path exportStorage;

    public void storeImportFile(InputStream inputStream, String fileName) throws IOException {
        Files.copy(inputStream, importStorage.resolve(fileName).normalize());
        log.info("File {} was stored.", fileName);
        inputStream.close();
    }

    public void storeExportFile(HSSFWorkbook hssfWorkbook, String fileName) throws IOException {
        hssfWorkbook.write(exportStorage.resolve(fileName).normalize().toFile());
    }

    public Resource loadUrlResource(String fileName) {
        try {
            Resource resource = new UrlResource(exportStorage.resolve(fileName).normalize().toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new DataNotFoundException("File " + fileName + " is not found.");
            }
        } catch (MalformedURLException ex) {
            throw new DataNotFoundException("File " + fileName + " is not found.", ex);
        }
    }
}
