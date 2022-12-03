package it.fabioformosa.quartzmanager.api.jobs;

import it.fabioformosa.quartzmanager.api.dto.TriggerFiredBundleDTO;
import it.fabioformosa.quartzmanager.api.jobs.entities.LogRecord;
import it.fabioformosa.quartzmanager.api.websockets.WebhookSender;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.*;

import static org.mockito.ArgumentMatchers.any;

class SampleJobTest {

  @InjectMocks
  private SampleJob sampleJob;

  @Mock
  private WebhookSender<TriggerFiredBundleDTO> webSocketProgressNotifier;
  @Mock
  private WebhookSender<LogRecord> webSocketLogsNotifier;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void givenASampleJob_whenTheJobIsExecuted_thenTheWebhookSendersAreCalled() {
    JobExecutionContext jobExecutionContext = Mockito.mock(JobExecutionContext.class);

    ScheduleBuilder schedulerBuilder = SimpleScheduleBuilder.simpleSchedule()
      .withRepeatCount(5)
      .withIntervalInMilliseconds(1000L);
    JobDetail jobDetail = JobBuilder
      .newJob(SampleJob.class).withIdentity(JobKey.jobKey("test-job"))
      .build();
    Trigger trigger = TriggerBuilder.newTrigger()
      .forJob(jobDetail)
      .withSchedule(schedulerBuilder)
      .build();
    Mockito.when(jobExecutionContext.getTrigger()).thenReturn(trigger);
    Mockito.when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);

    sampleJob.execute(jobExecutionContext);
    Mockito.verify(webSocketLogsNotifier).send(any(LogRecord.class));
    Mockito.verify(webSocketProgressNotifier).send(any(TriggerFiredBundleDTO.class));
  }

  @Test
  void givenASampleJob_whenTheDoItMethodIsCalled_thenALogRecordIsReturned() {
    JobExecutionContext jobExecutionContext = Mockito.mock(JobExecutionContext.class);
    LogRecord logRecord = sampleJob.doIt(jobExecutionContext);
    Assertions.assertThat(logRecord.getMessage()).isEqualTo("Hello!");
    Assertions.assertThat(logRecord.getType()).isEqualTo(LogRecord.LogType.INFO);
  }

}
