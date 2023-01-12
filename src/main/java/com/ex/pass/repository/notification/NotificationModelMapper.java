package com.ex.pass.repository.notification;

import com.ex.pass.repository.booking.BookingEntity;
import com.ex.pass.util.LocalDateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface NotificationModelMapper {

    NotificationModelMapper INSTANCE = Mappers.getMapper(NotificationModelMapper.class);

    @Mapping(target="uuid",source = "bookingEntity.userEntity.uuid")
    @Mapping(target = "text",source = "bookingEntity.startedAt",qualifiedByName = "text")
    NotificationEntity toNotificationEntity(BookingEntity bookingEntity,NotificationEvent event);

    @Named("text")
    default String text(LocalDateTime startedAt){
        return String.format("안녕하세요. %s 수업 시작합니다. 수업 전 출석 체크 부탁드립니다. \uD83D\uDE0A", LocalDateTimeUtils.format(startedAt));
    }
}
