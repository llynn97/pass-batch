package com.ex.pass.job.statistics;

import com.ex.pass.repository.booking.BookingEntity;
import com.ex.pass.repository.statistics.StatisticsEntity;
import com.ex.pass.repository.statistics.StatisticsRepository;
import com.ex.pass.util.LocalDateTimeUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class MakeStatisticsJobConfig {

    private final int CHUNK_SIZE = 5;


    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory entityManagerFactory;

    private final StatisticsRepository statisticsRepository;

    private final MakeDailyStatisticsTasklet makeDailyStatisticsTasklet;
    private final MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet;



    public MakeStatisticsJobConfig (JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,MakeDailyStatisticsTasklet makeDailyStatisticsTasklet,MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet,EntityManagerFactory entityManagerFactory,StatisticsRepository statisticsRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.makeDailyStatisticsTasklet = makeDailyStatisticsTasklet;
        this.makeWeeklyStatisticsTasklet = makeWeeklyStatisticsTasklet;
        this.entityManagerFactory = entityManagerFactory;
        this.statisticsRepository = statisticsRepository;
    }

    @Bean
    public Job makeStatisticsJob(){
        Flow addStatisticsFlow = new FlowBuilder<Flow>("addStatisticsFlow")
                .start(addStatisticsStep())
                .build();
        Flow makeDailyStatisticsFlow = new FlowBuilder<Flow>("makeDailyStatisticsFlow")
                .start(makeDailyStatisticsStep())
                .build();
        Flow makeWeeklyStatisticsFlow = new FlowBuilder<Flow>("makeWeeklyStatisticsFlow")
                .start(makeWeeklyStatisticsStep())
                .build();
        Flow parallelMakeStatisticsFlow = new FlowBuilder<Flow>("parallelMakeStatisticsFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(makeDailyStatisticsFlow,makeWeeklyStatisticsFlow)
                .build();

        return this.jobBuilderFactory.get("makeStatisticsJob")
                .start(addStatisticsFlow)
                .next(parallelMakeStatisticsFlow)
                .build()
                .build();

    }

    @Bean
    public Step addStatisticsStep(){
        return this.stepBuilderFactory.get("addStatisticsStep")
                .<BookingEntity,BookingEntity>chunk(CHUNK_SIZE) //JpaItemWriter 을 사용하지않고 customize 하기 때문에 output도 BookintEntity
                .reader(addStatisticsItemReader(null,null))
                .writer(addStatisticsItemWriter())
                .build();

    }

    @Bean
    @StepScope   //jobparameter를 사용하려면 scope를 설정해줘야함
    public JpaCursorItemReader<BookingEntity> addStatisticsItemReader(@Value("#{jobParameters[from]}") String fromString, @Value("#{jobParameters[to]}")String toString){
        final LocalDateTime from = LocalDateTimeUtils.parse(fromString);
        final LocalDateTime to = LocalDateTimeUtils.parse(toString);

        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("addStatisticsItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select b from BookingEntity b where b.endedAt between :from and :to")
                .parameterValues(Map.of("from",from,"to",to))
                .build();

    }

    @Bean
    public ItemWriter<BookingEntity> addStatisticsItemWriter(){
        return bookingEntites->{
            Map<LocalDateTime, StatisticsEntity> statisticsEntityMap = new LinkedHashMap<>();
            for(BookingEntity bookingEntity : bookingEntites){
                final LocalDateTime statisticsAt = bookingEntity.getStatisticsAt();
                StatisticsEntity statisticsEntity = statisticsEntityMap.get(statisticsAt);

                if(statisticsEntity == null){
                    statisticsEntityMap.put(statisticsAt,StatisticsEntity.create(bookingEntity));
                }else{
                    statisticsEntity.add(bookingEntity);
                }
            }
            final List<StatisticsEntity> statisticsEntities = new ArrayList<>(statisticsEntityMap.values());
            statisticsRepository.saveAll(statisticsEntities);

        };

    }


    @Bean
    public Step makeDailyStatisticsStep(){
        return this.stepBuilderFactory.get("makeDailyStatisticsStep")
                .tasklet(makeDailyStatisticsTasklet)
                .build();

    }

    @Bean
    public Step makeWeeklyStatisticsStep(){
        return this.stepBuilderFactory.get("makeWeeklyStatisticsStep")
                .tasklet(makeWeeklyStatisticsTasklet)
                .build();

    }


}
