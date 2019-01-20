package com.st;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("generated_data")
public class GeneratedDataEntity {
    @Id
    private Long id;
    private LocalDateTime created;

    public GeneratedDataEntity(LocalDateTime created) {
        this.created = created;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreated() {
        return created;
    }
}
