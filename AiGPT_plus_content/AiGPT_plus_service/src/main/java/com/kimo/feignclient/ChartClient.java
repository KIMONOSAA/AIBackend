package com.kimo.feignclient;


import com.kimo.model.dto.answer.ChartDataRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * @author Mr.kimo
 */
@FeignClient(value = "chart",url = "http://localhost:63090/chart",fallbackFactory = ChartClientFactory.class)
public interface ChartClient {

    @PostMapping("/chart/gen/chatdata/async")
    public String genChartData(@RequestBody ChartDataRequest chartData);
}
