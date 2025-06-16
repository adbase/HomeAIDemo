package com.example.service;

import com.example.entity.Item;
import com.example.entity.Photo;
import com.example.repository.ObjectRepository;
import com.example.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Base64;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.entity.ObjectQRCode;
import com.example.entity.ObjectQRCodeId;
import com.example.repository.ObjectQRCodeRepository;
import com.example.entity.QRCode;
import com.example.service.QRCodeService;

@Service
public class ObjectService {
    @Autowired
    private ObjectRepository objectRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private ExecutorService taskExecutor;

    @Autowired
    private ObjectQRCodeRepository objectQRCodeRepository;

    @Autowired
    private QRCodeService qrCodeService;

    private final String uploadDir;
    private static final Logger logger = LoggerFactory.getLogger(ObjectService.class);

    public ObjectService() {
        // Get the current working directory
        String currentDir = System.getProperty("user.dir");
        this.uploadDir = Paths.get(currentDir, "files").toString();
        
        // Create upload directory if it doesn't exist
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Item uploadObject(MultipartFile file, String name, String description) throws IOException {
        // Save photo
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        File dest = new File(uploadDir, fileName);
        file.transferTo(dest);

        // Save photo record
        Photo photo = new Photo();
        photo.setUrl("/files/" + fileName); // Use relative URL for web access
        photo = photoRepository.save(photo);

        // Save object record
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setPhotoId(photo.getId());
        item = objectRepository.save(item);

        logger.info("[uploadObject] Saved photo and item, itemId={}, filePath={}", item.getId(), dest.getAbsolutePath());
        // 异步分析图片
        final Long itemId = item.getId();
        final String filePath = dest.getAbsolutePath();
        taskExecutor.submit(() -> {
            logger.info("[Ollama-Async] Start analyzing image for itemId={}, filePath={}", itemId, filePath);
            try {
                byte[] imageBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath));
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                String requestBody = """
                {\n  \"model\": \"gemma3:latest\",\n  \"prompt\": \"请分析这张图片，返回物体的名称、分类和描述，结果用json格式返回。\",\n  \"images\": [\"%s\"],\n  \"format\": \"json\",\n  \"stream\": false\n}
                """.formatted(base64Image);
                logger.info("[Ollama-Async] Sending request to Ollama, itemId={}", itemId);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:11434/api/generate"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpClient client = HttpClient.newHttpClient();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String resultJson = response.body();
                logger.info("[Ollama-Async] Ollama response for itemId={}: {}", itemId, resultJson);
                // 解析resultJson，提取object_name, category, description
                ObjectMapper mapper = new ObjectMapper();
                String responseStr = resultJson;
                if (resultJson.contains("response")) {
                    Map<String, Object> outer = mapper.readValue(resultJson, Map.class);
                    responseStr = (String) outer.get("response");
                }
                Map<String, Object> result = mapper.readValue(responseStr, Map.class);
                logger.info("[Ollama-Async] Parsed result for itemId={}: {}", itemId, result);
                Item updateItem = objectRepository.findById(itemId).orElse(null);
                if (updateItem != null) {
                    Object objectName = result.get("object_name");
                    if (objectName == null) {
                        objectName = result.get("object");
                    }
                    if (objectName != null) {
                        updateItem.setObjectName(objectName.toString());
                    }
                    if (result.get("category") != null) updateItem.setCategory(result.get("category").toString());
                    if (result.get("description") != null) updateItem.setDescription(result.get("description").toString());
                    objectRepository.save(updateItem);
                    logger.info("[Ollama-Async] Updated item in DB for itemId={}", itemId);
                } else {
                    logger.warn("[Ollama-Async] Item not found in DB for itemId={}", itemId);
                }
            } catch (Exception e) {
                logger.error("[Ollama-Async] Exception for itemId=" + itemId, e);
            }
        });
        return item;
    }

    public List<Map<String, Object>> listObjects() {
        List<Item> items = objectRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Item item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("name", item.getName());
            
            // Get photo URL
            if (item.getPhotoId() != null) {
                Photo photo = photoRepository.findById(item.getPhotoId()).orElse(null);
                if (photo != null) {
                    map.put("photoUrl", photo.getUrl());
                }
            }
            
            result.add(map);
        }
        
        return result;
    }

    public Map<String, Object> getObjectDetail(Long id) {
        Item item = objectRepository.findById(id).orElse(null);
        if (item == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", item.getId());
        result.put("name", item.getName());
        result.put("description", item.getDescription());
        result.put("objectName", item.getObjectName());
        result.put("category", item.getCategory());
        
        // Get photo URL
        if (item.getPhotoId() != null) {
            Photo photo = photoRepository.findById(item.getPhotoId()).orElse(null);
            if (photo != null) {
                result.put("photoUrl", photo.getUrl());
            }
        }
        
        return result;
    }

    public boolean deleteObject(Long id) {
        Item item = objectRepository.findById(id).orElse(null);
        if (item == null) return false;
        Long photoId = item.getPhotoId();
        // 先删除物品
        objectRepository.deleteById(id);
        // 再删除图片文件和photo表记录
        if (photoId != null) {
            Photo photo = photoRepository.findById(photoId).orElse(null);
            if (photo != null) {
                // 删除文件
                String filePath = uploadDir + java.io.File.separator + new java.io.File(photo.getUrl()).getName();
                java.io.File file = new java.io.File(filePath);
                if (file.exists()) file.delete();
                photoRepository.deleteById(photo.getId());
            }
        }
        return true;
    }

    // 查询物品关联的所有二维码ID
    public List<Long> getQrcodeIdsByObjectId(Long objectId) {
        List<ObjectQRCode> list = objectQRCodeRepository.findByIdObjectId(objectId);
        List<Long> ids = new java.util.ArrayList<>();
        for (ObjectQRCode oq : list) {
            ids.add(oq.getId().getQrcodeId());
        }
        return ids;
    }

    // 关联物品和二维码
    public void addQrcodeToObject(Long objectId, Long qrcodeId) {
        objectQRCodeRepository.save(new ObjectQRCode(new ObjectQRCodeId(objectId, qrcodeId)));
    }

    // 取消关联
    public void removeQrcodeFromObject(Long objectId, Long qrcodeId) {
        objectQRCodeRepository.deleteById(new ObjectQRCodeId(objectId, qrcodeId));
    }

    // 设置物品二维码关联（先清空再批量添加）
    public void setQrcodesForObject(Long objectId, List<Long> qrcodeIds) {
        objectQRCodeRepository.deleteByIdObjectId(objectId);
        for (Long qid : qrcodeIds) {
            objectQRCodeRepository.save(new ObjectQRCode(new ObjectQRCodeId(objectId, qid)));
        }
    }

    public List<Map<String, Object>> searchObjects(String name, Long qrcodeId) {
        List<Item> byName = new ArrayList<>();
        List<Item> byQrcode = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            byName = objectRepository.findByNameContaining(name);
        }
        if (qrcodeId != null) {
            List<ObjectQRCode> rels = objectQRCodeRepository.findByIdQrcodeId(qrcodeId);
            for (ObjectQRCode rel : rels) {
                Item item = objectRepository.findById(rel.getId().getObjectId()).orElse(null);
                if (item != null) byQrcode.add(item);
            }
        }
        // 合并去重
        List<Item> merged = new ArrayList<>(byName);
        for (Item i : byQrcode) {
            if (merged.stream().noneMatch(x -> x.getId().equals(i.getId()))) {
                merged.add(i);
            }
        }
        // 转为Map，附带缩略图和二维码内容
        List<Map<String, Object>> result = new ArrayList<>();
        for (Item item : merged) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("name", item.getName());
            map.put("photoUrl", item.getPhotoId() != null ? photoRepository.findById(item.getPhotoId()).map(Photo::getUrl).orElse("") : "");
            List<Long> qrcodeIds = getQrcodeIdsByObjectId(item.getId());
            List<QRCode> qrcodes = new ArrayList<>();
            if (!qrcodeIds.isEmpty()) {
                qrcodes = qrCodeService.listQRCodesByIds(qrcodeIds);
            }
            map.put("qrcodes", qrcodes);
            result.add(map);
        }
        return result;
    }
} 