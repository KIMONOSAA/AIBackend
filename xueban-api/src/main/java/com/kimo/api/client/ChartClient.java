package com.kimo.api.client;



import com.kimo.api.dto.ChartDataRequest;
import com.kimo.api.dto.GouZiAdditionalMessages;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "chart",url = "http://localhost:63090/chart",fallbackFactory = ChartClientFactory.class)
public interface ChartClient {

    @PostMapping("/chart/gen/chatdata/async")
    public String genChartData(@RequestBody ChartDataRequest chartData);

    @PostMapping("/gen/couzi/async")
    public String getChartDataForCouZi(@RequestBody GouZiAdditionalMessages chartData);
}
