package com.karolsol.nabu.user.internal;

import com.karolsol.nabu.user.UserDto;
import com.karolsol.nabu.user.UserPreferencesDto;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

  UserDto toDto(UserEntity entity);

  UserPreferencesDto toDto(UserPreferences preferences);

  UserPreferences toPreferences(UserPreferencesDto dto);
}
