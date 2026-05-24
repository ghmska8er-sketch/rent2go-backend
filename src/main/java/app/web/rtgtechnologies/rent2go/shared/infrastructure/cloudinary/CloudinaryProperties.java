package app.web.rtgtechnologies.rent2go.shared.infrastructure.cloudinary;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "cloudinary")
@Component
public class CloudinaryProperties {
    private String cloudName;
    private String uploadPreset;

    public String getCloudName() {
        return cloudName;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public String getUploadPreset() {
        return uploadPreset;
    }

    public void setUploadPreset(String uploadPreset) {
        this.uploadPreset = uploadPreset;
    }
}
