package com.example.controller;

import com.example.entity.Item;
import com.example.entity.QRCode;
import com.example.service.ObjectService;
import com.example.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/object")
public class ObjectController {
    @Autowired
    private ObjectService objectService;

    @Autowired
    private QRCodeService qrCodeService;

    @PostMapping("/upload")
    public ResponseEntity<Item> uploadObject(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("description") String description) throws IOException {
        Item item = objectService.uploadObject(file, name, description);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listObjects() {
        List<Map<String, Object>> objects = objectService.listObjects();
        return ResponseEntity.ok(objects);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<Map<String, Object>> getObjectDetail(@PathVariable Long id) {
        Map<String, Object> object = objectService.getObjectDetail(id);
        if (object != null) {
            // 查询已关联二维码详细信息
            List<Long> qrcodeIds = objectService.getQrcodeIdsByObjectId(id);
            List<QRCode> qrcodes = qrCodeService.listQRCodesByIds(qrcodeIds);
            object.put("qrcodes", qrcodes);
            object.put("qrcodeIds", qrcodeIds);
            return ResponseEntity.ok(object);
        }
        return ResponseEntity.notFound().build();
    }

    // 查询物品已关联二维码ID列表
    @GetMapping("/{id}/qrcodes")
    public ResponseEntity<List<Long>> getObjectQrcodeIds(@PathVariable Long id) {
        return ResponseEntity.ok(objectService.getQrcodeIdsByObjectId(id));
    }

    // 设置物品二维码关联
    @PostMapping("/{id}/qrcodes")
    public ResponseEntity<Void> setObjectQrcodes(@PathVariable Long id, @RequestBody List<Long> qrcodeIds) {
        objectService.setQrcodesForObject(id, qrcodeIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObject(@PathVariable Long id) {
        boolean deleted = objectService.deleteObject(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchObjects(@RequestParam(required = false) String name, @RequestParam(required = false) Long qrcodeId) {
        List<Map<String, Object>> result = objectService.searchObjects(name, qrcodeId);
        return ResponseEntity.ok(result);
    }
} 