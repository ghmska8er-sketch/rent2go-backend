package app.web.rtgtechnologies.rent2go.shared.interfaces.rest.controllers;

import app.web.rtgtechnologies.rent2go.shared.infrastructure.cloudinary.CloudinaryStorageService;
import app.web.rtgtechnologies.rent2go.shared.interfaces.rest.resources.ImageUploadResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final CloudinaryStorageService storageService;

    @Autowired
    public UploadController(CloudinaryStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        String url = storageService.upload(file);
        ImageUploadResponse resp = new ImageUploadResponse(url, null);
        return ResponseEntity.ok(resp);
    }
}
