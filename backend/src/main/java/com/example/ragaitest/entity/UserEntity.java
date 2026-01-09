package com.example.ragaitest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "USER")
public class UserEntity {
    @Id
    private Long id;
    private String name;
}
