package com.natlex.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.natlex.model.GeologicalClass;
import com.natlex.model.Section;
import com.natlex.service.SectionService;
import com.natlex.util.Constants;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/sections", produces = MediaType.APPLICATION_JSON_VALUE)
public class SectionsRestController {

    private final SectionService sectionService;

    @Autowired
    public SectionsRestController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @PostMapping
    public ResponseEntity<?> createSection(@Valid @RequestBody Section section) {
        if (section.getId() != null && sectionService.isExists(section.getId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Map<String, Object> mapEntity = objectMapper
                .convertValue(section, new TypeReference<Map<String, Object>>() { });
        List<Object> geologicalClasses = (ArrayList<Object>) mapEntity.get("geologicalClasses");
        List<GeologicalClass> geologicalClassesNew = new ArrayList<>();
        for (Object geologicalClass : geologicalClasses) {
            GeologicalClass geologicalClassMapper = objectMapper
                    .convertValue(geologicalClass, GeologicalClass.class);
            geologicalClassMapper.addSection(section);
            geologicalClassesNew.add(geologicalClassMapper);
        }
        section.addGeologicalClass(geologicalClassesNew);
        return new ResponseEntity<>(sectionService.saveSection(section), HttpStatus.CREATED);
    }

    @GetMapping(value = "/list-sections")
    public ResponseEntity<?> getAllSections(
            @RequestParam(value = "size", required = false) Integer pageSize,
            @RequestParam(value = "page", required = false) Integer pageNumber) {
        List<Integer> pageSettings = pageSettings(pageSize, pageNumber);
        pageSize = pageSettings.get(0);
        pageNumber = pageSettings.get(1);
        return new ResponseEntity<>(sectionService.getAllSections(pageSize, pageNumber), HttpStatus.OK);
    }

    @GetMapping ("/{sectionId}")
    public ResponseEntity<?> getSection(@PathVariable Long sectionId) {
        if (sectionId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        return new ResponseEntity<>(sectionService.getSectionById(sectionId),HttpStatus.OK);
    }

    @PutMapping("/{sectionId}")
    public ResponseEntity<?> updateSection(@PathVariable Long sectionId, @Valid @RequestBody Section newSection) {
        if (sectionId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(sectionService.updateSection(sectionId, newSection), HttpStatus.OK);
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<?> deleteSection(@PathVariable Long sectionId) {
        if (sectionId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        sectionService.deleteSectionById(sectionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/by-code")
    public ResponseEntity<?> filterByCode(
            @RequestParam(value = "code") String geologicalClassCode,
            @RequestParam(value = "size", required = false) Integer pageSize,
            @RequestParam(value = "page", required = false) Integer pageNumber) {
        if (geologicalClassCode == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<Integer> pageSettings = pageSettings(pageSize, pageNumber);
        pageSize = pageSettings.get(0);
        pageNumber = pageSettings.get(1);
        return new ResponseEntity<>(sectionService
                .getAllSectionsByCode(geologicalClassCode, pageSize, pageNumber), HttpStatus.OK);
    }

    private List<Integer> pageSettings(Integer pageSize, Integer pageNumber) {
        List<Integer> integerList = new LinkedList<>();
        if (pageSize == null) {
            integerList.add(Constants.PAGE_SIZE);
        } else {
            integerList.add(pageSize);
        }
        if (pageNumber == null) {
            integerList.add(Constants.PAGE_NUMBER);
        } else {
            integerList.add(pageNumber);
        }
        return integerList;
    }
}
