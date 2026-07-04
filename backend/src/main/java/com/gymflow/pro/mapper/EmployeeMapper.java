package com.gymflow.pro.mapper;

import com.gymflow.pro.dto.response.EmployeeResponse;
import com.gymflow.pro.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface EmployeeMapper {

    @Mapping(target = "user", source = "user")
    EmployeeResponse toResponse(Employee employee);
}
