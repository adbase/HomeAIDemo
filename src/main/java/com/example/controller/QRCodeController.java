package com.example.controller;

import com.example.entity.QRCode;
import com.example.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/qrcode")
public class QRCodeController {
    @Autowired
    private QRCodeService qrCodeService;

    @PostMapping("/add")
    public ResponseEntity<QRCode> addQRCode(@RequestBody QRCode qrCode) {
        return ResponseEntity.ok(qrCodeService.addQRCode(qrCode));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQRCode(@PathVariable Long id) {
        if (qrCodeService.deleteQRCode(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<QRCode> updateQRCode(@RequestBody QRCode qrCode) {
        return ResponseEntity.ok(qrCodeService.updateQRCode(qrCode));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QRCode> getQRCode(@PathVariable Long id) {
        Optional<QRCode> qrCode = qrCodeService.getQRCode(id);
        return qrCode.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/list")
    public ResponseEntity<List<QRCode>> listQRCodes() {
        return ResponseEntity.ok(qrCodeService.listQRCodes());
    }
} 