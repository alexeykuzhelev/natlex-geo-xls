package com.natlex.repository;

import com.natlex.model.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepository extends BaseRespository<Section, Long> {

    @Query(value = "SELECT * FROM section s JOIN geological_class gc ON s.id = gc.section_id WHERE gc.code=?1", nativeQuery = true)
    Page<Section> findSectionsByGeologicalCode(@Param("code") String code, Pageable pageable);
}
