package com.kimo.api.client;


import com.kimo.api.dto.ChartDataRequest;
import com.kimo.api.dto.GouZiAdditionalMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChartClientFactory implements FallbackFactory<ChartClient> {
    @Override
    public ChartClient create(Throwable cause) {
        return new ChartClient() {


            @Override
            public String genChartData(ChartDataRequest chartData) {
                return "";
            }

            @Override
            public String getChartDataForCouZi(GouZiAdditionalMessages chartData) {
                return "";
            }
        };
    }
}
