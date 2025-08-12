package com.och.calltask.config;


import com.och.calltask.job.CustomerCrowdJob;
import org.quartz.*;
import org.quartz.spi.JobFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * 定时任务配置
 *
 * @author danmo
 * @date 2025/7/2 11:26
 */
@Configuration
public class QuartzJobConfig {

    private static final String CUSTOMER_CROWD_GROUP = "CustomerCrowd";
    private static final String CUSTOMER_CROWD_JOB = "CustomerCrowdJob";

    //每天凌晨2点执行
    private static final String CUSTOMER_CROWD_CRON = "0 0 2 * * ?";

    @Bean
    public JobDetail customerCrowdDetail() {
        return JobBuilder.newJob().ofType(CustomerCrowdJob.class)
                .withIdentity(CUSTOMER_CROWD_JOB, CUSTOMER_CROWD_GROUP)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger customerCrowdTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(customerCrowdDetail())
                .withIdentity(CUSTOMER_CROWD_JOB, CUSTOMER_CROWD_GROUP)
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(CUSTOMER_CROWD_CRON))
                .build();

    }
}
