package it.fabioformosa.quartzmanager.converters;

import it.fabioformosa.metamorphosis.core.converters.AbstractBaseConverter;
import it.fabioformosa.quartzmanager.dto.JobKeyDTO;
import it.fabioformosa.quartzmanager.dto.TriggerDTO;
import it.fabioformosa.quartzmanager.dto.TriggerKeyDTO;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

@Component
public class TriggerToTriggerDTO<S extends Trigger, T extends TriggerDTO> extends AbstractBaseConverter<S, T> {

  @Override
  protected void convert(S source, T target) {
    TriggerKey triggerKey = source.getKey();
    TriggerKeyDTO triggerKeyDTO = conversionService.convert(triggerKey, TriggerKeyDTO.class);
    target.setTriggerKeyDTO(triggerKeyDTO);

    target.setStartTime(source.getStartTime());
    target.setDescription(source.getDescription());
    target.setEndTime(source.getEndTime());
    target.setFinalFireTime(source.getFinalFireTime());
    target.setMisfireInstruction(source.getMisfireInstruction());
    target.setNextFireTime(source.getNextFireTime());
    target.setPriority(source.getPriority());
    target.setMayFireAgain(source.mayFireAgain());

    JobKey jobKey = source.getJobKey();
    JobKeyDTO jobKeyDTO = conversionService.convert(jobKey, JobKeyDTO.class);
    target.setJobKeyDTO(jobKeyDTO);
  }

  @Override
  protected T createOrRetrieveTarget(S source) {
    return (T) new TriggerDTO();
  }

}
