package luxaeterna.itforum.service.impl;

import luxaeterna.itforum.entity.Upload;
import luxaeterna.itforum.exception.BadRequestException;
import luxaeterna.itforum.mapper.UploadMapper;
import luxaeterna.itforum.service.FileUploadService;
import luxaeterna.itforum.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private final UploadMapper uploadMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final String uploadPath;
    private final List<String> allowedImageTypes;
    private final long maxFileSize;

    public FileUploadServiceImpl(UploadMapper uploadMapper, SnowflakeIdGenerator idGenerator,
                                  @Value("${file.upload.path}") String uploadPath,
                                  @Value("${file.upload.allowed-image-types}") String allowedImageTypes,
                                  @Value("${file.upload.max-file-size}") long maxFileSize) {
        this.uploadMapper = uploadMapper;
        this.idGenerator = idGenerator;
        this.uploadPath = uploadPath;
        this.allowedImageTypes = Arrays.asList(allowedImageTypes.split(","));
        this.maxFileSize = maxFileSize;
    }

    @Override
    public Upload uploadImage(Long userId, MultipartFile file) {
        validateFile(file, allowedImageTypes);
        return saveFile(userId, file);
    }

    @Override
    public Upload uploadFile(Long userId, MultipartFile file) {
        validateFile(file, null);
        return saveFile(userId, file);
    }

    private void validateFile(MultipartFile file, List<String> allowedTypes) {
        if (file.isEmpty()) {
            throw new BadRequestException("文件为空");
        }
        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("文件大小超过限制");
        }
        if (allowedTypes != null) {
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
                if (!allowedTypes.contains(ext)) {
                    throw new BadRequestException("不支持的文件类型: " + ext);
                }
            }
        }
    }

    private Upload saveFile(Long userId, MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID().toString() + ext;

            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = Paths.get(uploadPath, storedName);
            file.transferTo(filePath.toFile());

            Upload upload = new Upload();
            upload.setId(idGenerator.nextId());
            upload.setUserId(userId);
            upload.setFileName(originalName);
            upload.setStoredName(storedName);
            upload.setFilePath("/uploads/" + storedName);
            upload.setFileSize(file.getSize());
            upload.setMimeType(file.getContentType());
            uploadMapper.insert(upload);

            return upload;
        } catch (IOException e) {
            throw new BadRequestException("文件上传失败: " + e.getMessage());
        }
    }
}
