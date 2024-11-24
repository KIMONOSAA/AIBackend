//package com.kimo.feignclient;
//
//import com.kimo.model.dto.answer.ChartDataRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.openfeign.FallbackFactory;
//import org.springframework.stereotype.Component;
///**
// * @author Mr.kimo
// */
//@Slf4j
//@Component
//public class ChartClientFactory implements FallbackFactory<ChartClient> {
//    @Override
//    public ChartClient create(Throwable cause) {
//        return new ChartClient() {
//
//            @Override
//            public String genChartData(ChartDataRequest chartData) {
//                return "";
//            }
//        };
//    }
//}
