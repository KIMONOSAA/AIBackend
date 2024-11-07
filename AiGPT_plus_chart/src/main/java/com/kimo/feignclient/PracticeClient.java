//package com.kimo.feignclient;
//
//import com.kimo.common.BaseResponse;
//
//import com.kimo.model.dto.chart.Accuracy;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.List;
//
////GouZiAdditionalMessages gouZiAdditionalMessages = new GouZiAdditionalMessages();
////        gouZiAdditionalMessages.setContent(itemsBase.toString());
////        gouZiAdditionalMessages.setRole("user_id001");
////        gouZiAdditionalMessages.setContent_type("text");
////        log.info("数据库当前所有数据" + itemsBase.toString());
////String chartDataForCouZi = chartClient.getChartDataForCouZi(gouZiAdditionalMessages);
//@FeignClient(value = "practice",url = "http://localhost:63090/practice",fallbackFactory = PracticeClientFactory.class)
//public interface PracticeClient {
//
//    @GetMapping("/get/all/accuracy")
//    public Accuracy addQuestionResultOverall(@RequestParam("accuracyId") Long accuracyId);
//}
