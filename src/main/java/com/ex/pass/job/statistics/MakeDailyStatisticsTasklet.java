package com.ex.pass.job.statistics;

import com.ex.pass.repository.statistics.AggregatedStatistics;
import com.ex.pass.repository.statistics.StatisticsRepository;
import com.ex.pass.util.CustomCSVWriter;
import com.ex.pass.util.LocalDateTimeUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@StepScope
public class MakeDailyStatisticsTasklet implements Tasklet {

    @Value("#{jobParameter[from]}")
    private String fromString;

    @Value("#{jobParameter[to]}")
    private String toString;

    private final StatisticsRepository statisticsRepository;

    public MakeDailyStatisticsTasklet(StatisticsRepository statisticsRepository){
        this.statisticsRepository = statisticsRepository;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final LocalDateTime from = LocalDateTimeUtils.parse(fromString);
        final LocalDateTime to = LocalDateTimeUtils.parse(toString);
        List<AggregatedStatistics> statisticsList = statisticsRepository.findByStatisticsAtBetweenAndGroupBy(from,to);
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"statisticsAt","allCount","attendedCount","cancelledCount"});
        for(AggregatedStatistics statistics : statisticsList){
            data.add(new String[]{
                    LocalDateTimeUtils.format(statistics.getStatisticsAt()),
                    String.valueOf(statistics.getAllCount()),
                    String.valueOf(statistics.getAttendedCount()),
                    String.valueOf(statistics.getCancelledCount())
            });

        }
        CustomCSVWriter.write("daily_statistics_" + LocalDateTimeUtils.format(from, LocalDateTimeUtils.YYYY_MM_DD) + ".csv", data);

        return RepeatStatus.FINISHED;
    }
}
