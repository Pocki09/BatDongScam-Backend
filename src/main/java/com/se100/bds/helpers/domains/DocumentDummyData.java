package com.se100.bds.helpers.domains;

import com.se100.bds.models.entities.document.DocumentType;
import com.se100.bds.models.entities.document.IdentificationDocument;
import com.se100.bds.models.entities.property.Property;
import com.se100.bds.repositories.domains.document.DocumentTypeRepository;
import com.se100.bds.repositories.domains.document.IdentificationDocumentRepository;
import com.se100.bds.repositories.domains.property.PropertyRepository;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentDummyData {

    private final DocumentTypeRepository documentTypeRepository;
    private final IdentificationDocumentRepository identificationDocumentRepository;
    private final PropertyRepository propertyRepository;
    private final Random random = new Random();

    public void createDummy() {
        createDummyDocumentTypes();
        createDummyIdentificationDocuments();
    }

    private void createDummyDocumentTypes() {
        log.info("Creating dummy document types");

        List<DocumentType> documentTypes = new ArrayList<>();

        documentTypes.add(DocumentType.builder()
                .name("Property Title Deed")
                .description("Legal document proving ownership of the property")
                .isCompulsory(true)
                .documents(new ArrayList<>())
                .build());

        documentTypes.add(DocumentType.builder()
                .name("Land Use Certificate")
                .description("Certificate of land use rights")
                .isCompulsory(true)
                .documents(new ArrayList<>())
                .build());

        documentTypes.add(DocumentType.builder()
                .name("Building Permit")
                .description("Permit for construction and building modifications")
                .isCompulsory(false)
                .documents(new ArrayList<>())
                .build());

        documentTypes.add(DocumentType.builder()
                .name("Tax Payment Certificate")
                .description("Proof of property tax payment")
                .isCompulsory(true)
                .documents(new ArrayList<>())
                .build());

        documentTypes.add(DocumentType.builder()
                .name("Owner ID Card")
                .description("National identification card of property owner")
                .isCompulsory(true)
                .documents(new ArrayList<>())
                .build());

        documentTypes.add(DocumentType.builder()
                .name("Household Registration")
                .description("Household registration book")
                .isCompulsory(false)
                .documents(new ArrayList<>())
                .build());

        documentTypeRepository.saveAll(documentTypes);
        log.info("Saved {} document types to database", documentTypes.size());
    }

    private void createDummyIdentificationDocuments() {
        log.info("Creating dummy identification documents");

        List<DocumentType> documentTypes = documentTypeRepository.findAll();
        List<Property> properties = propertyRepository.findAll();

        if (documentTypes.isEmpty() || properties.isEmpty()) {
            log.warn("Cannot create identification documents - missing required data");
            return;
        }

        List<IdentificationDocument> documents = new ArrayList<>();

        // Create 2-4 documents for each property
        for (Property property : properties) {
            int docCount = 2 + random.nextInt(3);

            for (int i = 0; i < docCount && i < documentTypes.size(); i++) {
                DocumentType docType = documentTypes.get(i);

                Constants.VerificationStatusEnum status = random.nextDouble() < 0.8
                        ? Constants.VerificationStatusEnum.VERIFIED
                        : Constants.VerificationStatusEnum.PENDING;

                IdentificationDocument document = IdentificationDocument.builder()
                        .documentType(docType)
                        .property(property)
                        .documentNumber(String.format("DOC%09d", random.nextInt(999999999)))
                        .documentName(docType.getName() + " - " + property.getTitle())
                        .filePath(String.format("/uploads/documents/property_%s/%s_%d.pdf",
                                property.getId(),
                                docType.getName().toLowerCase().replace(" ", "_"),
                                i))
                        .issueDate(LocalDate.now().minusYears(random.nextInt(10)))
                        .expiryDate(LocalDate.now().plusYears(5 + random.nextInt(10)))
                        .issuingAuthority(getRandomAuthority())
                        .verificationStatus(status)
                        .verifiedAt(status == Constants.VerificationStatusEnum.VERIFIED
                                ? LocalDateTime.now().minusDays(random.nextInt(180))
                                : null)
                        .rejectionReason(status == Constants.VerificationStatusEnum.REJECTED
                                ? "Document illegible or incomplete"
                                : null)
                        .build();

                documents.add(document);
            }
        }

        identificationDocumentRepository.saveAll(documents);
        log.info("Saved {} identification documents to database", documents.size());
    }

    private String getRandomAuthority() {
        String[] authorities = {
                "Ho Chi Minh City People's Committee",
                "Hanoi People's Committee",
                "Da Nang People's Committee",
                "Ministry of Natural Resources and Environment",
                "Department of Construction",
                "Land Registration Office"
        };
        return authorities[random.nextInt(authorities.length)];
    }
}
