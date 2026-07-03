package app.web.rtgtechnologies.rent2go.shared.infrastructure.cloudinary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class CloudinaryStorageServiceImpl implements CloudinaryStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageServiceImpl.class);

    // Values Spring Boot binds when CLOUDINARY_CLOUD_NAME / CLOUDINARY_UPLOAD_PRESET
    // env vars are not set (see application.properties defaults). Uploading against
    // these placeholders always fails at Cloudinary with an auth/not-found error,
    // which is the confirmed root cause of "changing profile photo silently does
    // nothing": the client picks the image fine and sends the PATCH fine, but the
    // server-side Cloudinary call fails and the failure used to be swallowed into a
    // bodyless 500 with no server log line to diagnose it from.
    private static final String PLACEHOLDER_CLOUD_NAME = "your_cloud_name";
    private static final String PLACEHOLDER_UPLOAD_PRESET = "your_upload_preset";

    private final CloudinaryProperties props;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    public CloudinaryStorageServiceImpl(CloudinaryProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
    }

    @Override
    public String upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is empty");
        }

        if (isBlankOrPlaceholder(props.getCloudName(), PLACEHOLDER_CLOUD_NAME)
                || isBlankOrPlaceholder(props.getUploadPreset(), PLACEHOLDER_UPLOAD_PRESET)) {
            log.error("Cloudinary is not configured: cloud-name='{}', upload-preset='{}'. " +
                            "Set CLOUDINARY_CLOUD_NAME and CLOUDINARY_UPLOAD_PRESET environment variables.",
                    props.getCloudName(), props.getUploadPreset());
            throw new IOException("Image upload is not configured on the server (Cloudinary credentials missing).");
        }

        String boundary = UUID.randomUUID().toString();
        byte[] body = buildMultipartBody(boundary, file, props.getUploadPreset());

        String url = String.format("https://api.cloudinary.com/v1_1/%s/image/upload", props.getCloudName());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        try {
            log.debug("Uploading image to Cloudinary. cloudName={}", props.getCloudName());
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode node = objectMapper.readTree(response.body());
                JsonNode secure = node.get("secure_url");
                if (secure != null && !secure.isNull()) {
                    return secure.asText();
                }
                log.error("Cloudinary response missing secure_url: {}", response.body());
                throw new IOException("Cloudinary response missing secure_url: " + response.body());
            }
            log.error("Cloudinary upload failed: status={} body={}", response.statusCode(), response.body());
            throw new IOException("Cloudinary upload failed: " + response.statusCode() + " " + response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    private boolean isBlankOrPlaceholder(String value, String placeholder) {
        return value == null || value.isBlank() || value.equals(placeholder);
    }

    private byte[] buildMultipartBody(String boundary, MultipartFile file, String uploadPreset) throws IOException {
        String LINE = "\r\n";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder();

        // upload_preset field
        sb.append("--").append(boundary).append(LINE);
        sb.append("Content-Disposition: form-data; name=\"upload_preset\"").append(LINE).append(LINE);
        sb.append(uploadPreset == null ? "" : uploadPreset).append(LINE);

        // file field
        sb.append("--").append(boundary).append(LINE);
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                .append("\"").append(LINE);
        sb.append("Content-Type: ").append(file.getContentType() == null ? "application/octet-stream" : file.getContentType()).append(LINE).append(LINE);

        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.write(file.getBytes());
        out.write(LINE.getBytes(StandardCharsets.UTF_8));

        // end
        String end = "--" + boundary + "--" + LINE;
        out.write(end.getBytes(StandardCharsets.UTF_8));

        return out.toByteArray();
    }
}
