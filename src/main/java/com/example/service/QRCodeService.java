package com.example.service;

import com.example.entity.QRCode;
import com.example.repository.QRCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import java.util.Hashtable;

@Service
public class QRCodeService {
    @Autowired
    private QRCodeRepository qrCodeRepository;

    public QRCode addQRCode(QRCode qrCode) {
        QRCode saved = qrCodeRepository.save(qrCode);
        try {
            // 生成二维码图片
            String fileName = "qrcode_" + saved.getId() + ".png";
            String dir = System.getProperty("user.dir") + File.separator + "files";
            File dirFile = new File(dir);
            if (!dirFile.exists()) dirFile.mkdirs();
            String filePath = dir + File.separator + fileName;
            generateQRCodeImage(saved.getContent(), 240, 240, filePath);
            saved.setImageUrl("/files/" + fileName);
            saved = qrCodeRepository.save(saved);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return saved;
    }

    private void generateQRCodeImage(String text, int width, int height, String filePath) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        ImageIO.write(image, "png", new File(filePath));
    }

    public boolean deleteQRCode(Long id) {
        if (qrCodeRepository.existsById(id)) {
            qrCodeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public QRCode updateQRCode(QRCode qrCode) {
        return qrCodeRepository.save(qrCode);
    }

    public Optional<QRCode> getQRCode(Long id) {
        return qrCodeRepository.findById(id);
    }

    public List<QRCode> listQRCodes() {
        return qrCodeRepository.findAll();
    }

    public List<QRCode> listQRCodesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();
        return qrCodeRepository.findAllById(ids);
    }
} 