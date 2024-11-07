package com.kimo.model.dto;

import com.kimo.model.po.PracticeRecord;
import lombok.Data;

@Data
public class ChartDataForRecord {

    public ChartDataRequest chartDataRequest;

    private PracticeRecordDto practiceRecord;
}
