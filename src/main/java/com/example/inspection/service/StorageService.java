package com.example.inspection.service;

import com.example.inspection.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StorageService {

    private final Path baseDir;

    public StorageService(@Value("${app.storage.local-dir}") String dir) {
        this.baseDir = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (Exception e) {
            throw new IllegalStateException("无法创建存储目录: " + baseDir, e);
        }
    }

    /**
     * 写入文件到存储根目录下
     * @param key 相对路径，如 "reports/INS-xxx.pdf"
     * @param data 数据流
     * @return 存储 key（原样返回）
     */
    public String store(String key, InputStream data) {
        try {
            Path target = baseDir.resolve(key).normalize();
            if (!target.startsWith(baseDir)) {
                throw new SecurityException("invalid path: " + key);
            }
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
            return key;
        } catch (SecurityException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PATH", e.getMessage());
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", e.getMessage());
        }
    }

    /**
     * 读取文件
     * @param key 相对路径
     * @return 文件输入流（由调用方负责关闭）
     */
    public InputStream load(String key) {
        try {
            Path target = baseDir.resolve(key).normalize();
            if (!target.startsWith(baseDir)) {
                throw new SecurityException("invalid path: " + key);
            }
            if (!Files.exists(target)) {
                throw new ApiException(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "file not found: " + key);
            }
            return Files.newInputStream(target);
        } catch (ApiException e) {
            throw e;
        } catch (SecurityException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PATH", e.getMessage());
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", e.getMessage());
        }
    }

    /**
     * 判断文件是否存在
     */
    public boolean exists(String key) {
        Path target = baseDir.resolve(key).normalize();
        return target.startsWith(baseDir) && Files.exists(target);
    }

    /**
     * 删除文件（用于清理）
     */
    public boolean delete(String key) {
        try {
            Path target = baseDir.resolve(key).normalize();
            if (!target.startsWith(baseDir)) return false;
            return Files.deleteIfExists(target);
        } catch (Exception e) {
            return false;
        }
    }
}