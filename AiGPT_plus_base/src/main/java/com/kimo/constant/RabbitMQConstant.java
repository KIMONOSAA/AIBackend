package com.kimo.constant;

import javax.swing.plaf.PanelUI;

/**
 * @author Mr.kimo
 */
public interface RabbitMQConstant {
    /**
     * direct 交换机
     */
    public static final String DIRECT_EXCHANGE = "chart1_exchange";

    public static final String DIRECT_QUEUE = "chart1_queue";

    public static final String DIRECT_ROUTING_KEY = "chart";


    /**
     * direct 交换机
     */
    public static final  String PRACTICE_DIRECT_EXCHANGE = "practice1_chart_exchange";

    public static final  String PRACTICE_DIRECT_QUEUE = "practice1_chart_queue";

    public static final String PRACTICE_DIRECT_ROUTING_KEY = "practice1_chart";


    /**
     * direct 交换机
     */
    public static final String ACCURACY_DIRECT_EXCHANGE = "accuracy_chart_exchange";

    public static final String ACCURACY_DIRECT_QUEUE = "accuracy_chart_queue";

    public static final String ACCURACY_DIRECT_ROUTING_KEY = "accuracy_chart";

    public static final long BI_MODEL_ID = 1659171950288818178L;


    public static final String ORDER_EXCHANGE = "order_exchange";

    public static final String ORDER_QUEUE = "order_queue";

    public static final String ORDER_ROUTING_KEY = "order_create";

    public static final String DEAD_LETTER_EXCHANGE = "order_ttl_exchange";
    public static final String DEAD_LETTER_QUEUE = "order_ttl_queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "order_ttl_create";



    //交换机
    public static final String PAYNOTIFY_EXCHANGE_FANOUT = "paynotify_exchange_fanout";
    //支付结果通知消息类型
    public static final String MESSAGE_TYPE = "payresult_notify";
    //支付通知队列
    public static final String PAYNOTIFY_QUEUE = "paynotify_queue";




}
