package com.natlex.model;

import com.natlex.model.enums.JobResultStatus;
import com.natlex.model.enums.JobType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@ToString
@NoArgsConstructor
public class Job extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private JobType type;

    @Enumerated(EnumType.STRING)
    private JobResultStatus status;

    @CreationTimestamp
    private LocalDateTime createdDate;

    public Job(JobType type) {
        this.type = type;
    }
}
