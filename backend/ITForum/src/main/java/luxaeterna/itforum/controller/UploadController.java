package luxaeterna.itforum.controller;

import luxaeterna.itforum.common.Result;
import luxaeterna.itforum.security.CurrentUser;
import luxaeterna.itforum.service.FileUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private final FileUploadService fileUploadService;

    public UploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/image")
    public Result<?> uploadImage(@CurrentUser Long userId, @RequestParam("file") MultipartFile file) {
        return Result.success(fileUploadService.uploadImage(userId, file));
    }

    @PostMapping("/file")
    public Result<?> uploadFile(@CurrentUser Long userId, @RequestParam("file") MultipartFile file) {
        return Result.success(fileUploadService.uploadFile(userId, file));
    }
}
