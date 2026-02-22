package com.fmk.cource.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUploadUtil {

    // 默认保存在当前目录下同级 fmk/uploads/ 文件夹中，保存到项目同级 fmk+时间戳 目录。
    @Value("${file.upload.path:${user.dir}/uploads/}")
    private String uploadPath;

    public String upload(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

        File dest = new File(dir, newFilename);
        file.transferTo(dest);

        return dest.getAbsolutePath();
    }
}
