package com.se100.bds.mappers;

import com.se100.bds.dtos.responses.user.CustomerResponse;
import com.se100.bds.dtos.responses.user.PropertyOwnerResponse;
import com.se100.bds.dtos.responses.user.SalesAgentResponse;
import com.se100.bds.dtos.responses.user.UserResponse;
import com.se100.bds.models.entities.user.User;
import com.se100.bds.utils.Constants;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper extends BaseMapper {

    @Autowired
    public UserMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        modelMapper.typeMap(User.class, UserResponse.class)
                .addMappings(mapper -> {

                    // Map profile based on role
                    mapper.using(ctx -> {
                        User user = (User) ctx.getSource();
                        if (user.getRole() == Constants.RoleEnum.CUSTOMER && user.getCustomer() != null) {
                            return modelMapper.map(user.getCustomer(), CustomerResponse.class);
                        } else if (user.getRole() == Constants.RoleEnum.PROPERTY_OWNER && user.getPropertyOwner() != null) {
                            return modelMapper.map(user.getPropertyOwner(), PropertyOwnerResponse.class);
                        } else if (user.getRole() == Constants.RoleEnum.SALESAGENT && user.getSaleAgent() != null) {
                            return modelMapper.map(user.getSaleAgent(), SalesAgentResponse.class);
                        }
                        return null;
                    }).map(src -> src, UserResponse::setProfile);
                });
    }
}
