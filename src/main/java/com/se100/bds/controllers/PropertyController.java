package com.se100.bds.controllers;

import com.se100.bds.controllers.base.AbstractBaseController;
import com.se100.bds.mappers.PropertyMapper;
import com.se100.bds.services.domains.property.PropertyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/properties")
@Tag(name = "006. Properties", description = "Property Listing API")
@Slf4j
public class PropertyController extends AbstractBaseController {
    private final PropertyMapper propertyMapper;
    private final PropertyService propertyService;


}
