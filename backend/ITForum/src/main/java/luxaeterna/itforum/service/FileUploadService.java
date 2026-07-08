package luxaeterna.itforum.service;

import luxaeterna.itforum.entity.Upload;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    Upload uploadImage(Long userId, MultipartFile file);
    Upload uploadFile(Long userId, MultipartFile file);
}
