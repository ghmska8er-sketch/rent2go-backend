package app.web.rtgtechnologies.rent2go.shared.infrastructure.cloudinary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            System.out.println("URL = " + url);
            System.out.println("Cloud Name = " + props.getCloudName());
            System.out.println("Upload Preset = " + props.getUploadPreset());
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                JsonNode node = objectMapper.readTree(response.body());
                JsonNode secure = node.get("secure_url");
                if (secure != null && !secure.isNull()) {
                    return secure.asText();
                }
                throw new IOException("Cloudinary response missing secure_url: " + response.body());
            }
            throw new IOException("Cloudinary upload failed: " + response.statusCode() + " " + response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
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
