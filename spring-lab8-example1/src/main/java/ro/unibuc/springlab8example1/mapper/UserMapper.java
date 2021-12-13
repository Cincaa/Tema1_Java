package ro.unibuc.springlab8example1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ro.unibuc.springlab8example1.domain.User;
import ro.unibuc.springlab8example1.dto.UserDto;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "cnp", source = "userDetails.cnp")
    @Mapping(target = "age", source = "userDetails.age")
    @Mapping(target = "otherInformation", source = "userDetails.otherInformation")
    UserDto mapToDto(User user);

    @Mapping(target = "userDetails.cnp", source = "cnp")
    @Mapping(target = "userDetails.age", source = "age")
    @Mapping(target = "userDetails.otherInformation", source = "otherInformation")
    User mapToEntity(UserDto user);
}
