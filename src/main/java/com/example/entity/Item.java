package com.example.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "object")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    private String description;
    
    @Column(name = "photo_id")
    private Long photoId;

    private String objectName; // 物体名称
    private String category;   // 物体分类

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
} 