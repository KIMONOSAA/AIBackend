//package com.kimo.amqp;
//
//import com.kimo.constant.RedisConstant;
//import com.kimo.model.dto.po.Chart;
//import com.kimo.service.ChartService;
//import com.rabbitmq.client.Channel;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.amqp.rabbit.annotation.RabbitHandler;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.amqp.support.AmqpHeaders;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//import static com.kimo.constant.RabbitMQConstant.*;
//
//@Component
//@Slf4j
//public class ChartConsumer {
//
//    @Lazy
//    @Autowired
//    private ChartService chartService;
//
//
//
//
//    @Autowired
//    private RedissonClient redissonClient;
//
//
//    @RabbitHandler
//    @RabbitListener(queues = DIRECT_QUEUE,ackMode = "MANUAL")
//    public void processChart(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
//        long chartId = Long.parseLong(message);
//        RLock lock = redissonClient.getLock(RedisConstant.USER_INFO_ID_PRE + chartId);
//        lock.lock();
//        try {
//            Chart chart = chartService.getById(chartId);
//            if("succeed".equals(chart.getStatus())){
//                log.info("消息体参数 【重复消息】:{}", chart.getId());
//                return;
//            }
//            Chart updateChart = new Chart();
//            updateChart.setId(chartId);
//            updateChart.setStatus("running");
//            boolean b = chartService.updateById(updateChart);
//            if(!b){
//                handleChartUpdateError(chartId,"更新图表成功状态失败");
//            }
//            String result =  chartService.getChartData(buildUserInput(chart));
//
//            String[] splits = result.split("【【【【【");
//            if (splits.length < 3){
//                handleChartUpdateError(chartId,"图表生成格式错误");
//                return;
//            }
//            String genChart = splits[1].trim();
//            String genResult = splits[2].trim();
//            Chart updateChartResult = new Chart();
//            updateChartResult.setId(chartId);
//            updateChartResult.setGenChat(genChart);
//            updateChartResult.setGenResult(genResult);
//            updateChartResult.setStatus("succeed");
//            boolean updateResult = chartService.updateById(updateChartResult);
//            if(!updateResult){
//                handleChartUpdateError(chartId,"更新图表成功状态失败");
//            }
//            channel.basicAck(deliveryTag,false);
//        }catch (Exception e){
//            log.error(e.getMessage());
//            channel.basicNack(deliveryTag,false,true);
//        }finally {
//            lock.unlock();
//        }
//    }
//
//    private String buildUserInput(Chart chart){
//        final String prompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
//                "分析需求：\n" +
//                "{数据分析的需求或者目标}\n" +
//                "原始数据：\n" +
//                "{csv格式的原始数据，用,作为分隔符}\n" +
//                "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
//                "【【【【【\n" +
//                "{前端 Echarts V5 的 option 配置对象的json代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
//                "【【【【【\n" +
//                "{明确的数据分析结论、越详细越好，不要生成多余的注释}";
//        String goal = chart.getGoal();
//        String chartType = chart.getChartType();
//        String resultData = chart.getChartData();
//        StringBuilder userInput = new StringBuilder();
//        userInput.append(prompt).append("\n");
//        userInput.append("分析需求").append("\n");
//        String userGoal = goal;
//        if(StringUtils.isNotBlank(chartType)){
//            userGoal += ", 请使用" + chartType;
//        }
//        userInput.append(userGoal).append("\n");
//        userInput.append("原始数据：").append("\n");
//
//
//        userInput.append(resultData).append("\n");
//        return userInput.toString();
//    }
//
//    private void handleChartUpdateError(long chartId,String execMessage){
//        Chart updateChartResult = new Chart();
//        updateChartResult.setId(chartId);
//        updateChartResult.setStatus("failed");
//        updateChartResult.setExecMessage(execMessage);
//        boolean updateResult = chartService.updateById(updateChartResult);
//        if(!updateResult){
//            log.error("图表状态更改失败");
//        }
//    }
//}
