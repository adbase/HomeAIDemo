package com.example.entity;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ObjectQRCodeId implements Serializable {
    private Long objectId;
    private Long qrcodeId;

    public ObjectQRCodeId() {}
    public ObjectQRCodeId(Long objectId, Long qrcodeId) {
        this.objectId = objectId;
        this.qrcodeId = qrcodeId;
    }
    public Long getObjectId() { return objectId; }
    public void setObjectId(Long objectId) { this.objectId = objectId; }
    public Long getQrcodeId() { return qrcodeId; }
    public void setQrcodeId(Long qrcodeId) { this.qrcodeId = qrcodeId; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectQRCodeId that = (ObjectQRCodeId) o;
        return Objects.equals(objectId, that.objectId) && Objects.equals(qrcodeId, that.qrcodeId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(objectId, qrcodeId);
    }
} 