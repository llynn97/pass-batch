package com.ex.pass.repository.pass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class PassModelMapperTest {

    @Test
    public void test_toPassEntity(){

        final LocalDateTime now = LocalDateTime.now();
        final String userId = "A1000000";

        BulkPassEntity bulkPassEntity = new BulkPassEntity();
        bulkPassEntity.setPackageSeq(1);
        bulkPassEntity.setUserGroupId("GROUP");
        bulkPassEntity.setStatus(BulkPassStatus.READY);
        bulkPassEntity.setCount(10);
        bulkPassEntity.setStartedAt(now.minusDays(60));
        bulkPassEntity.setEndedAt(now);

        final PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity,userId);


        Assertions.assertEquals(1,passEntity.getPackageSeq());
        Assertions.assertEquals(PassStatus.READY,passEntity.getStatus());
        Assertions.assertEquals(userId,passEntity.getUserId());
    }
}
