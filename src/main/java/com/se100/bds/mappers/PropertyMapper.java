package com.se100.bds.mappers;

import com.se100.bds.dtos.responses.property.SimplePropertyCard;
import com.se100.bds.services.dtos.results.PropertyCard;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper extends BaseMapper {
    @Autowired
    public PropertyMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        modelMapper.typeMap(PropertyCard.class, SimplePropertyCard.class)
                .addMappings(mapper -> {
                    mapper.using(ctx -> {
                        PropertyCard propertyCard = (PropertyCard) ctx.getSource();
                        String district = propertyCard.getDistrict();
                        String city = propertyCard.getCity();

                        if (district != null && city != null) {
                            return district + ", " + city;
                        } else if (city != null) {
                            return city;
                        }
                        return district;
                    }).map(src -> src, (dest, value) -> dest.setLocation((String) value));
                });
    }
}
