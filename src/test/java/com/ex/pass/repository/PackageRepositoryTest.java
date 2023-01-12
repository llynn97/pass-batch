package com.ex.pass.repository;


import com.ex.pass.repository.packaze.PackageEntity;
import com.ex.pass.repository.packaze.PackageRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
public class PackageRepositoryTest {

    @Autowired
    private PackageRepository packageRepository;

    @Test
    public void test_save(){

        PackageEntity packageEntity=new PackageEntity();
        packageEntity.setPackageName("바디 챌린지 PT 1주");
        packageEntity.setPeriod(84);

        packageRepository.save(packageEntity);

        Assertions.assertNotNull(packageEntity.getPackageSeq());
    }

    @Test
    public void test_findByCreatedAtAfter(){

        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(1);

        PackageEntity packageEntity0 = new PackageEntity();
        packageEntity0.setPackageName("학생 전용 3개월");
        packageEntity0.setPeriod(90);
        packageRepository.save(packageEntity0);

        PackageEntity packageEntity1 = new PackageEntity();
        packageEntity1.setPackageName("학생 전용 6개월");
        packageEntity1.setPeriod(180);
        packageRepository.save(packageEntity1);

        final List<PackageEntity> packageEntities = packageRepository.findByCreatedAtAfter(dateTime, PageRequest.of(0,1, Sort.by("packageSeq").descending()));

        Assertions.assertEquals(1,packageEntities.size());
        Assertions.assertEquals(packageEntity1.getPackageSeq(),packageEntities.get(0).getPackageSeq());
    }

    @Test
    public void test_updateCountAndPeriod(){

        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디프로필 이벤트 4개월");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);

        int updatedCount = packageRepository.updateCountAndPeriod(packageEntity.getPackageSeq(),30,120);

        final PackageEntity updatedPackageEntity = packageRepository.findById(packageEntity.getPackageSeq()).get();

        Assertions.assertEquals(1,updatedCount);
        Assertions.assertEquals(30,updatedPackageEntity.getCount());
        Assertions.assertEquals(120,updatedPackageEntity.getPeriod());

    }
}
