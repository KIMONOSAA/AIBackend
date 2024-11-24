//package com.kimo.feignclient;
//
//import com.kimo.model.dto.ChartDataRequest;
//import com.kimo.model.dto.GouZiAdditionalMessages;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.openfeign.FallbackFactory;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class ChartClientFactory implements FallbackFactory<ChartClient> {
//    @Override
//    public ChartClient create(Throwable cause) {
//        return new ChartClient() {
//
//
//            @Override
//            public String genChartData(ChartDataRequest chartData) {
//                return "";
//            }
//
//            @Override
//            public String getChartDataForCouZi(GouZiAdditionalMessages chartData) {
//                return "";
//            }
//        };
//    }
//}
