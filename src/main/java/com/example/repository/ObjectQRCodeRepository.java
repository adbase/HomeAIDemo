package com.example.repository;

import com.example.entity.ObjectQRCode;
import com.example.entity.ObjectQRCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ObjectQRCodeRepository extends JpaRepository<ObjectQRCode, ObjectQRCodeId> {
    List<ObjectQRCode> findByIdObjectId(Long objectId);
    List<ObjectQRCode> findByIdQrcodeId(Long qrcodeId);
    void deleteByIdObjectId(Long objectId);
    void deleteByIdQrcodeId(Long qrcodeId);
} 