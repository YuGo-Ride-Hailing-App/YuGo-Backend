package org.yugo.backend.YuGo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yugo.backend.YuGo.model.WorkTime;
@Getter @Setter
@NoArgsConstructor
public class WorkTimeOut {
    private Integer id;
    private String start;
    private String end;

    public WorkTimeOut(Integer id, String start, String end) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public WorkTimeOut(WorkTime workTime){
        String endTime=null;
        if (workTime.getEndTime()!=null)
            endTime=workTime.getEndTime().toString();
        this.id=workTime.getId();
        this.start=workTime.getStartTime().toString();
        this.end=endTime;
    }
}
