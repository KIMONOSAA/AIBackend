package com.kimo.common;

/**
 * 自定义错误码
 *
 * @author Mr.kimo

 * 

 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    EMAIL_ERROR(40002, "电子邮件发送失败"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    USER_IS_NOT(40200,"用户不存在"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    TOO_MANY_REQUEST(42900,"请求频繁,请稍后重试"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败"),
    FILE_FOUND_ERROR(50002, "文件上传失败"),
    UPLOAD_PICTURE_FOUND(60016,"课程图片不能为空"),
    SYSTEM_FILE_ERROR(50003,"上传文件到文件系统失败"),
    FILE_MERGE_CHUNKS_ERROR(50005,"文件合并失败"),
    SESSION_NOT_FOUND_ERROR(50004,"没有这个会话"),
    FILE_DOWNLOAD_MERGE_CHUNKS_ERROR(50003,"下载文件合并后失败"),
    FILE_READER_URL_ERROR(50003,"文件读取失败"),
    VIDEO_FOUND_ERROR(50006,"视频集合不存在"),
    COURSE_NAME_NOT_FOUND_ERROR(60001,"课程名不能为空"),
    COURSE_TAGS_NOT_FOUND_ERROR(60002,"课程分类不能为空"),
    COURSE_GRADE_NOT_FOUND_ERROR(60003,"课程等级不能为空"),
    COURSE_TEACH_MODE_NOT_FOUND_ERROR(60004,"课程教育模式不能为空"),
    COURSE_USER_NOT_FOUND_ERROR(60005,"用户不能不能为空"),
    COURSE_CHARGE_NOT_FOUND_ERROR(60006,"收费规则不能为空"),
    COURSE_PRICE_NOT_FOUND_ERROR(60007,"课程为收费价格不能为空且必须大于0"),
    ADD_COURSE_INFO_ERROR(60008,"新增课程基本信息失败"),
    SAVE_MARKET_ERROR(60009,"保存课程营销信息失败"),
    COURSE_NOT_FOUND(60010,"课程不存在"),
    COURSE_AUDIT_IS_NOT_ERROR(60011,"请先提交课程审核，审核通过才可以发布"),
    COURSE_COMMIT_NOT_ERROR(60012,"不允许提交其它机构或其他管理员的课程"),
    COURSE_AUDIT_STATUS_ERROR(60013,"操作失败，课程审核通过方可发布。"),
    COURSE_PUBLIC_FOUND_ERROR(60014,"课程预发布数据为空"),
    COURSE_STATUS_AUDIT_ERROR(60015,"审核状态为已提交不允许再次提交"),
    TEACH_PLAN_ERROR(60016,"提交失败，还没有添加课程计划"),
    RECORD_NOT_FOUND(70001,"记录id不能为空"),
    EXAM_TIMEOUT(70002,"考试时间已过期，禁止考试"),
    EXAM_NOT_FOUNT(70003,"查找不到你的考试"),
    IS_NOT_MEMBER(80001,"此课程需要会员才能观看,你当前暂不是会员用户"),
    JSON_PROCESSING_EXCEPTION(70004,"JSON解析错误"),
    ORDER_ADD_NOT_ERROR(90001,"订单创建失败"),
    ORDER_NOT_FOUNT(90002,"订单不存在"),
    ORDER_EXIST(90003,"订单已存在"),
    ADD_PAYRECORD_ERROR(90004,"添加支付记录失败"),
    PAYRECORD_NOT_FOUND(90005,"支付记录不存在"),
    TRADING_NOT_FOUND(90006,"交易不存在"),
    PAY_ERROR(90008,"支付失败"),
    POINT_IS_NOT(90007,"积分不足,请到其他渠道获取,才能与AI问答"),
    PERMISSION_NOT_FOUND(100001,"无权限"),
    PAID_SUCCESS_NO_NEED_TO_REPAY(90006,"已支付，无需重新支付"),
    GENERATING_THE_QR_CODE_ERROR(90005,"生成二维码错误"),
    NOT_PERMISSIONS(100002,"无权限"),
    POINT_NOT_FOUNT(100011,"积分不足"),
    FETCH_COUZI_ERROR(100003,"扣子错误"),
    ADD_DATABASE_ERROR(100004,"添加数据错误"),
    SOURCE_VIDEO_NOT_FOUND(100005,"源视频文件不存在，请检查源视频路径"),
    VIDEO_PATH_FOUND(100006,"转换后的视频路径为空，请检查转换后的视频存放路径是否正确"),
    NEW_VIDEO_FILE_ERROR(10007,"视频转换时新建输出文件失败"),
    VIDEO_NOT_FORMAT(10008,"无法解析的视频格式"),
    VIDEO_CONVERSION_ERROR(10009,"视频转换视频"),
    FILE_CHUNK_ERROR(50007, "上传分块失败"),
    INVALID_INPUT_ERROR(50008, "无效的输入"),
    UPDATE_FAILED(50008, "更新失败"),
    INVALID_SESSION_ERROR(50009, "无效的会话id"),
    INVALID_USER_ERROR(50010, "无效的用户id"),
    DATABASE_UPDATE_ERROR(50011, "无法更新 AI Master 数据"),
    DATABASE_INSERT_ERROR(50012, "无法插入 AIMasterData"),
    INVALID_SORT_FIELD(50013, "无效数据");


    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
