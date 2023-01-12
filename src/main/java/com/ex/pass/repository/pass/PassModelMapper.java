package com.ex.pass.repository.pass;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

// 일치하지 않은 필드는 무시
@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PassModelMapper {

    PassModelMapper INSTANCE = Mappers.getMapper(PassModelMapper.class);

    @Mapping(target="status",qualifiedByName = "defaultStatus")
    @Mapping(target="remainingCount",source = "bulkPassEntity.count")
    PassEntity toPassEntity(BulkPassEntity bulkPassEntity,String userId);

    @Named("defaultStatus")
    default PassStatus status(BulkPassStatus status){
        return PassStatus.READY;
    }
}
