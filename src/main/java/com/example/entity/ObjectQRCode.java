package com.example.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "object_qrcode")
public class ObjectQRCode implements Serializable {
    @EmbeddedId
    private ObjectQRCodeId id;

    public ObjectQRCode() {}
    public ObjectQRCode(ObjectQRCodeId id) { this.id = id; }
    public ObjectQRCodeId getId() { return id; }
    public void setId(ObjectQRCodeId id) { this.id = id; }
} 