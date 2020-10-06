package com.natlex.service;

import com.natlex.exсeptions.DataNotFoundException;
import com.natlex.model.Section;
import com.natlex.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    @Autowired
    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public boolean isExists(Long sectionId) {
        return sectionRepository.existsById(sectionId);
    }

    public Section saveSection(Section section){
        return sectionRepository.save(section);
    }

    public Section saveBlankSection(String sectionName){
        return saveSection(new Section(sectionName));
    }

    public Section getSectionById(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(()->new DataNotFoundException("The section is not found. Wrong section ID."));
    }

    public Section updateSection(Long sectionId, Section newSection) {
        return sectionRepository.findById(sectionId).map(section -> {
            section.setName(newSection.getName());
            return sectionRepository.save(section);
        }).orElseThrow(() -> new DataNotFoundException("The section is not found. 0 sections have been updated"));
    }

    public void deleteSectionById(Long sectionId) {
        sectionRepository.deleteById(sectionId);
    }

    public List<Section> getAllSections() {
        Iterable<Section> iterEntities = sectionRepository.findAll();
        return iterableToList(iterEntities);
    }

    public List<Section> getAllSections(int pageSize, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return sectionRepository.findAll(pageable).getContent();
    }

    public List<Section> getAllSectionsByCode(String geologicalClassCode, int pageSize, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return sectionRepository.findSectionsByGeologicalCode(geologicalClassCode, pageable).getContent();
    }

    public List<Section> iterableToList(Iterable<Section> iterEntities) {
        List<Section> entities = StreamSupport.stream(iterEntities.spliterator(), false)
                .collect(Collectors.toList());
        if(entities==null) {
            entities= new ArrayList<>();
        }
        return entities;
    }
}
