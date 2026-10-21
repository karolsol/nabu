package com.karolsol.nabu.user;

import com.karolsol.nabu.user.internal.UserEntity;
import com.karolsol.nabu.user.internal.UserPreferences;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserDto toDto(UserEntity entity);

    UserPreferencesDto toDto(UserPreferences preferences);

    UserPreferences toPreferences(UserPreferencesDto dto);
}
