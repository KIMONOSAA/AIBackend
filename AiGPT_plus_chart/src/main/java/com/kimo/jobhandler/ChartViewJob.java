//package com.kimo.jobhandler;
//
//import com.kimo.common.BaseResponse;
//import com.kimo.constant.ChartConstant;
//import com.kimo.domain.GouZiAdditionalMessages;
//import com.kimo.feignclient.PracticeClient;
//
//import com.kimo.model.dto.chart.Accuracy;
//
//import com.kimo.model.dto.po.AccuracyChart;
//import com.kimo.utils.IdWorkerUtils;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//import static com.kimo.constant.RedisConstant.MAX_INSERT_REDIS;
//
//@Slf4j
//@Component
//public class ChartViewJob {
//
//
//    @Autowired
//    private PracticeClient practiceClient;
//
//
//    @XxlJob("ChartViewJob")
//    public void getChartViewJob() {
////        List<Accuracy> listBaseResponse = practiceClient.addQuestionResultOverall();
////        GouZiAdditionalMessages gouZiAdditionalMessages = new GouZiAdditionalMessages();
////        gouZiAdditionalMessages.setContent(listBaseResponse.toString());
////        gouZiAdditionalMessages.setRole("user_id001");
////        gouZiAdditionalMessages.setContent_type("text");
////        log.info("数据库当前所有数据" + listBaseResponse.toString());
////        AccuracyChart accuracyChart = new AccuracyChart();
////        accuracyChart.setId(IdWorkerUtils.getInstance().nextId());
////        accuracyChart.setStatus(ChartConstant.IS_wait);
////        accuracyChart.se
//
//    }
//}
