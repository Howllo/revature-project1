package net.revature.project1.service;

import net.revature.project1.enumerator.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${file.resource.url}")
    private String fileResourceUrl;

    @Value("${app.resource.path}")
    private String resourcePath;

    private final List<String> fileLocation = List.of("videos", "images");

    private final List<String> allowedImageTypes = List.of(
            "image/jpeg", "image/png", "image/jpg", "image/gif", "image/bmp", "image/ico",
            "image/tif", "image/tiff", "image/webp", "image/svg", "image/svgz", "image/ai", "image/drw",
            "image/pct", "image/psp", "image/xcf", "image/psd", "image/raw", "image/heic"
    );

    private final List<String> allowedVideoTypes = List.of(
            "video/mp4", "video/m4a", "video/m4b", "video/webm", "video/mov", "video/gif"
    );

    /**
     * Upload a file to the server.
     * @param fileType The type of file to upload.
     * @param filePath The path to the file.
     * @param fileName The name of the file.
     * @return The URL of the uploaded file.
     * @throws IOException If an I/O error occurs.
     */
    public String uploadFile(FileType fileType, String filePath, String fileName) throws IOException {
        if (fileType == null || filePath == null || fileName == null) {
            throw new IllegalArgumentException("File path and name cannot be null");
        }

        long maxImageSize = 10 * 1024 * 1024;
        long maxVideoSize = 150 * 1024 * 1024;

        Path fromPath = Paths.get(filePath);
        long fileSize = Files.size(fromPath);

        String mimeType = Files.probeContentType(fromPath);

        Path toPath;
        String resourceLocation;
        List<String> allowedTypes;

        switch (fileType) {
            case IMAGE:
                allowedTypes = allowedImageTypes;
                resourceLocation = fileLocation.get(1);
                toPath = Paths.get(resourcePath + resourceLocation + "/" + fileName);

                if (!allowedImageTypes.contains(mimeType)) {
                    logger.warn("Unsupported image type: {}", mimeType);
                    throw new IllegalArgumentException("Unsupported image type: " + mimeType);
                }

                if (fileSize > maxImageSize) {
                    logger.warn("Image exceeds maximum size: {} bytes", fileSize);
                    throw new IllegalArgumentException("Image exceeds maximum size of 10 MB");
                }
                break;

            case VIDEO:
                allowedTypes = allowedVideoTypes;
                resourceLocation = fileLocation.getFirst();
                toPath = Paths.get(resourcePath + resourceLocation + "/" + fileName);

                if (!allowedVideoTypes.contains(mimeType)) {
                    logger.warn("Unsupported video type: {}", mimeType);
                    throw new IllegalArgumentException("Unsupported video type: " + mimeType);
                }

                if (fileSize > maxVideoSize) {
                    logger.warn("Video exceeds maximum size: {} bytes", fileSize);
                    throw new IllegalArgumentException("Video exceeds maximum size of 100 MB");
                }
                break;

            default:
                logger.error("Unsupported file type: {}", fileType);
                return null;
        }

        Files.createDirectories(toPath.getParent());

        Files.copy(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);

        return fileResourceUrl + resourceLocation + "/" + fileName;
    }
}