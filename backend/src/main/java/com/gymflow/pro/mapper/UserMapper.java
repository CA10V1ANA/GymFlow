package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.UserResponse;
import com.gymflow.pro.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
