package com.kimo.model.dto.chart;

import lombok.Data;
/**
 * @author Mr.kimo
 */
@Data
public class ChartDataForRecord {

    public ChartDataRequest chartDataRequest;

    private PracticeRecord practiceRecord;
}
