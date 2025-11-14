package com.se100.bds.services.domains.appointment.impl;

import com.se100.bds.dtos.responses.appointment.ViewingCardDto;
import com.se100.bds.mappers.AppointmentMapper;
import com.se100.bds.models.entities.appointment.Appointment;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.repositories.domains.appointment.AppointmentRepository;
import com.se100.bds.services.domains.appointment.AppointmentService;
import com.se100.bds.services.domains.user.UserService;
import com.se100.bds.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserService userService;
    private final AppointmentMapper appointmentMapper;

    @Override
    public Page<ViewingCardDto> myViewingCards(Pageable pageable, Constants.AppointmentStatusEnum statusEnum, Integer day, Integer month, Integer year) {
        User me = userService.getUser();
        List<Appointment> appointments;
        if (statusEnum == null) {
            appointments = appointmentRepository.findAllByCustomer_Id(me.getId());
        } else {
            appointments = appointmentRepository.findAllByStatusAndCustomer_Id(statusEnum, me.getId());
        }

        List<ViewingCardDto> viewingCardDtos = appointments.stream().filter(appointment -> {
            LocalDateTime requestedTime = appointment.getRequestedDate();
            if (year != null && requestedTime.getYear() != year) {
                return false;
            }
            if (month != null && requestedTime.getMonthValue() != month) {
                return false;
            }
            if (day != null && requestedTime.getDayOfMonth() != day) {
                return false;
            }
            return true;
        })

        .map(appointment -> {

            String thumbnailUrl = appointment.getProperty().getMediaList().get(0).getFilePath();
            String districtName = appointment.getProperty().getWard().getDistrict().getDistrictName();
            String cityName = appointment.getProperty().getWard().getDistrict().getCity().getCityName();

            ViewingCardDto viewingCardDto = appointmentMapper.mapTo(appointment, ViewingCardDto.class);

            viewingCardDto.setTitle(appointment.getProperty().getTitle());
            viewingCardDto.setThumbnailUrl(thumbnailUrl);
            viewingCardDto.setDistrictName(districtName);
            viewingCardDto.setCityName(cityName);
            viewingCardDto.setPriceAmount(appointment.getProperty().getPriceAmount());
            viewingCardDto.setArea(appointment.getProperty().getArea());

            return viewingCardDto;
        }
        ).collect(Collectors.toList());

        return new PageImpl<>(viewingCardDtos, pageable, viewingCardDtos.size());
    }
}
