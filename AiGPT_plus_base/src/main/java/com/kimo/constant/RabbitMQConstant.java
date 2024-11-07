package com.kimo.constant;

/**
 * @author Mr.kimo
 */
public interface RabbitMQConstant {
    /**
     * direct 交换机
     */
    String DIRECT_EXCHANGE = "chart1_exchange";

    String DIRECT_QUEUE = "chart1_queue";

    String DIRECT_ROUTING_KEY = "chart";


    /**
     * direct 交换机
     */
    String PRACTICE_DIRECT_EXCHANGE = "practice_chart_exchange";

    String PRACTICE_DIRECT_QUEUE = "practice_chart_queue";

    String PRACTICE_DIRECT_ROUTING_KEY = "practice_chart";


    /**
     * direct 交换机
     */
    String ACCURACY_DIRECT_EXCHANGE = "accuracy_chart_exchange";

    String ACCURACY_DIRECT_QUEUE = "accuracy_chart_queue";

    String ACCURACY_DIRECT_ROUTING_KEY = "accuracy_chart";

    long BI_MODEL_ID = 1659171950288818178L;

    //交换机
    String PAYNOTIFY_EXCHANGE_FANOUT = "paynotify_exchange_fanout";
    //支付结果通知消息类型
    String MESSAGE_TYPE = "payresult_notify";
    //支付通知队列
    String PAYNOTIFY_QUEUE = "paynotify_queue";
}
