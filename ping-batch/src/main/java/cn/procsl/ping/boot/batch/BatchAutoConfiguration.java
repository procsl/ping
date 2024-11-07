package cn.procsl.ping.boot.batch;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AllArgsConstructor
@AutoConfiguration
public class BatchAutoConfiguration {

    @Bean
    public Job footballJob(JobRepository jobRepository) {
//        StepBuilder fetch = new StepBuilder("fetch", jobRepository)
//            .flow(new)
//            .startLimit(1);

//        return new JobBuilder("fetch-login-info", jobRepository)
//            .start()
//
//            .build();
        return null;
    }

    @Bean
    @ConditionalOnMissingBean
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        return jobLauncher;
    }

}
