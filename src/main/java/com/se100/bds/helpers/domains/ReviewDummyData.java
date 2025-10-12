package com.se100.bds.helpers.domains;

import com.se100.bds.entities.appointment.Appointment;
import com.se100.bds.entities.contract.Contract;
import com.se100.bds.entities.review.Review;
import com.se100.bds.repositories.appointment.AppointmentRepository;
import com.se100.bds.repositories.contract.ContractRepository;
import com.se100.bds.repositories.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewDummyData {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final ContractRepository contractRepository;
    private final Random random = new Random();

    public void createDummy() {
        createDummyReviews();
    }

    private void createDummyReviews() {
        log.info("Creating dummy reviews");

        List<Review> reviews = new ArrayList<>();

        // Create reviews for completed appointments
        List<Appointment> appointments = appointmentRepository.findAll();
        for (Appointment appointment : appointments) {
            if ("COMPLETED".equals(appointment.getStatus()) && random.nextDouble() < 0.6) { // 60% of completed appointments get reviews
                Review review = Review.builder()
                        .appointment(appointment)
                        .contract(null)
                        .rating((short) (3 + random.nextInt(3))) // Rating 3-5
                        .comment(generateAppointmentReviewComment())
                        .build();
                reviews.add(review);
            }
        }

        // Create reviews for completed contracts
        List<Contract> contracts = contractRepository.findAll();
        for (Contract contract : contracts) {
            if (contract.getStatus() == com.se100.bds.utils.Constants.ContractStatusEnum.COMPLETED && random.nextDouble() < 0.7) { // 70% of completed contracts get reviews
                Review review = Review.builder()
                        .appointment(null)
                        .contract(contract)
                        .rating((short) (3 + random.nextInt(3))) // Rating 3-5
                        .comment(generateContractReviewComment())
                        .build();
                reviews.add(review);
            }
        }

        reviewRepository.saveAll(reviews);
        log.info("Saved {} reviews to database", reviews.size());
    }

    private String generateAppointmentReviewComment() {
        String[] comments = {
                "Very professional agent, showed great knowledge of the property",
                "The viewing was well organized and on time",
                "Agent was helpful and answered all my questions",
                "Good experience, would recommend to others",
                "The property was exactly as described",
                "Agent was courteous and professional throughout",
                "Excellent service, very satisfied with the viewing"
        };
        return comments[random.nextInt(comments.length)];
    }

    private String generateContractReviewComment() {
        String[] comments = {
                "Smooth transaction from start to finish",
                "Very satisfied with the entire process",
                "Agent handled everything professionally",
                "Great experience, highly recommend",
                "All paperwork was handled efficiently",
                "The whole process was transparent and fair",
                "Excellent service throughout the contract period"
        };
        return comments[random.nextInt(comments.length)];
    }
}

