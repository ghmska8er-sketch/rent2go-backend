package app.web.rtgtechnologies.rent2go.shared.infrastructure.cloudinary;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryStorageService {
    /**
     * Uploads a file to Cloudinary using the configured upload preset and returns the secure URL.
     */
    String upload(MultipartFile file) throws IOException;
}
