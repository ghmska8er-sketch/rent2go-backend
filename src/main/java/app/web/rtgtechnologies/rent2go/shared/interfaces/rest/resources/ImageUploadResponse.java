package app.web.rtgtechnologies.rent2go.shared.interfaces.rest.resources;

public class ImageUploadResponse {
    private String imageUrl;
    private String imagePath;

    public ImageUploadResponse() {
    }

    public ImageUploadResponse(String imageUrl, String imagePath) {
        this.imageUrl = imageUrl;
        this.imagePath = imagePath;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
