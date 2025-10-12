package com.se100.bds.helpers.domains;

import com.se100.bds.entities.property.Media;
import com.se100.bds.entities.property.Property;
import com.se100.bds.repositories.property.MediaRepository;
import com.se100.bds.repositories.property.PropertyRepository;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class MediaDummyData {

    private final MediaRepository mediaRepository;
    private final PropertyRepository propertyRepository;
    private final Random random = new Random();

    public void createDummy() {
        createDummyMedia();
    }

    private void createDummyMedia() {
        log.info("Creating dummy media files");

        List<Property> properties = propertyRepository.findAll();
        if (properties.isEmpty()) {
            log.warn("Cannot create media - no properties found");
            return;
        }

        List<Media> mediaList = new ArrayList<>();

        // Create 3-5 media files for each property
        for (Property property : properties) {
            int mediaCount = 3 + random.nextInt(3); // 3-5 media files

            for (int i = 0; i < mediaCount; i++) {
                Constants.MediaTypeEnum mediaType;
                String mimeType;

                if (i < mediaCount - 1) {
                    // First few are images
                    mediaType = Constants.MediaTypeEnum.IMAGE;
                    mimeType = "image/jpeg";
                } else {
                    // Last one is either video or document
                    mediaType = random.nextBoolean() ? Constants.MediaTypeEnum.VIDEO : Constants.MediaTypeEnum.DOCUMENT;
                    mimeType = mediaType == Constants.MediaTypeEnum.VIDEO ? "video/mp4" : "application/pdf";
                }

                Media media = Media.builder()
                        .property(property)
                        .mediaType(mediaType)
                        .fileName(String.format("property_%s_%s_%d.%s",
                                property.getId(),
                                mediaType.name().toLowerCase(),
                                i + 1,
                                getFileExtension(mimeType)))
                        .filePath(String.format("/uploads/properties/%s/%s_%d.%s",
                                property.getId(),
                                mediaType.name().toLowerCase(),
                                i + 1,
                                getFileExtension(mimeType)))
                        .mimeType(mimeType)
                        .documentType(mediaType == Constants.MediaTypeEnum.DOCUMENT ? "Property Certificate" : null)
                        .build();

                mediaList.add(media);
            }
        }

        mediaRepository.saveAll(mediaList);
        log.info("Saved {} media files to database", mediaList.size());
    }

    private String getFileExtension(String mimeType) {
        switch (mimeType) {
            case "image/jpeg": return "jpg";
            case "video/mp4": return "mp4";
            case "application/pdf": return "pdf";
            default: return "bin";
        }
    }
}

