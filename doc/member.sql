/*
 Navicat Premium Data Transfer

 Source Server         : ai
 Source Server Type    : MySQL
 Source Server Version : 80024 (8.0.24)
 Source Host           : 192.168.101.132:3306
 Source Schema         : member

 Target Server Type    : MySQL
 Target Server Version : 80024 (8.0.24)
 File Encoding         : 65001

 Date: 02/12/2024 16:52:40
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for mq_message
-- ----------------------------
DROP TABLE IF EXISTS `mq_message`;
CREATE TABLE `mq_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息id',
  `message_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '消息类型代码: course_publish ,  media_test',
  `business_key1` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `business_key2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `business_key3` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `execute_num` int UNSIGNED NULL DEFAULT 0 COMMENT '通知次数',
  `state` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '处理状态，0:初始，1:成功',
  `returnfailure_date` datetime NULL DEFAULT NULL COMMENT '回复失败时间',
  `returnsuccess_date` datetime NULL DEFAULT NULL COMMENT '回复成功时间',
  `returnfailure_msg` varchar(2048) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '回复失败内容',
  `execute_date` datetime NULL DEFAULT NULL COMMENT '最近通知时间',
  `stage_state1` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '阶段1处理状态, 0:初始，1:成功',
  `stage_state2` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '阶段2处理状态, 0:初始，1:成功',
  `stage_state3` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '阶段3处理状态, 0:初始，1:成功',
  `stage_state4` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '阶段4处理状态, 0:初始，1:成功',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mq_message
-- ----------------------------
INSERT INTO `mq_message` VALUES (39, 'payresult_notify', '10', '605001', '1861728079559299073', 0, '0', NULL, NULL, NULL, NULL, '0', '0', '0', '0');
INSERT INTO `mq_message` VALUES (40, 'payresult_notify', '10', '605001', '1862018866981470209', 0, '0', NULL, NULL, NULL, NULL, '0', '0', '0', '0');
INSERT INTO `mq_message` VALUES (41, 'payresult_notify', '10', '605001', '1862078880328810498', 0, '0', NULL, NULL, NULL, NULL, '0', '0', '0', '0');

-- ----------------------------
-- Table structure for mq_message_history
-- ----------------------------
DROP TABLE IF EXISTS `mq_message_history`;
CREATE TABLE `mq_message_history`  (
  `id` bigint NOT NULL COMMENT '消息id',
  `message_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '消息类型代码',
  `business_key1` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `business_key2` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `business_key3` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '关联业务信息',
  `execute_num` int UNSIGNED NULL DEFAULT NULL COMMENT '通知次数',
  `state` int(10) UNSIGNED ZEROFILL NULL DEFAULT NULL COMMENT '处理状态，0:初始，1:成功，2:失败',
  `returnfailure_date` datetime NULL DEFAULT NULL COMMENT '回复失败时间',
  `returnsuccess_date` datetime NULL DEFAULT NULL COMMENT '回复成功时间',
  `returnfailure_msg` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '回复失败内容',
  `execute_date` datetime NULL DEFAULT NULL COMMENT '最近通知时间',
  `stage_state1` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stage_state2` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stage_state3` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stage_state4` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of mq_message_history
-- ----------------------------
INSERT INTO `mq_message_history` VALUES (22, 'payresult_notify', '10', '605001', '1860962119500017666', 0, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', '0');
INSERT INTO `mq_message_history` VALUES (23, 'payresult_notify', '10', '605001', '1860962119500017666', 0, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', '0');
INSERT INTO `mq_message_history` VALUES (24, 'payresult_notify', '10', '605001', '1861728079559299073', 0, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', '0');
INSERT INTO `mq_message_history` VALUES (28, 'payresult_notify', '10', '605001', '1861728079559299073', 0, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', '0');
INSERT INTO `mq_message_history` VALUES (30, 'payresult_notify', '10', NULL, '1861728079559299073', 0, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', '0');
INSERT INTO `mq_message_history` VALUES (31, 'payresult_notify', '10', NULL, '1861728079559299073', 0, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', '0');
INSERT INTO `mq_message_history` VALUES (37, 'payresult_notify', '10', '605001', '1861728079559299073', 0, NULL, NULL, NULL, NULL, NULL, '0', '0', '0', '0');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL COMMENT '订单号',
  `total_price` float(8, 2) NOT NULL COMMENT '总价',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '交易状态',
  `user_id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户id',
  `order_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单类型',
  `order_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单名称',
  `order_descrip` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单描述',
  `order_detail` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '订单明细json',
  `out_business_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '外部系统业务id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1856630927839117312, 199.00, '2024-11-13 17:31:22', '600001', '1854934298709209090', '605001', '普通会员', '普通会员', '[{\"shopId\":\"605001\",\"shopType\":\"开通会员\",\"shopName\":\"会员\",\"shopPrice\":\"199\",\"shopDetail\":{\"收费视频\":\"解锁\",\"AI问答\":\"无限\"}}]', '10');
INSERT INTO `orders` VALUES (1859049579027673088, 199.00, '2024-11-20 09:42:14', '600001', '1854545533041926145', '605001', '普通会员', '普通会员', '[{\"shopId\":\"605001\",\"shopType\":\"开通会员\",\"shopName\":\"会员\",\"shopPrice\":\"199\",\"shopDetail\":{\"收费视频\":\"解锁\",\"AI问答\":\"无限\"}}]', '10');
INSERT INTO `orders` VALUES (1861010172689276928, 199.00, '2024-11-25 19:32:56', '600002', '1860962119500017666', '605001', '普通会员', '普通会员', '[{\"shopId\":\"605001\",\"shopType\":\"开通会员\",\"shopName\":\"会员\",\"shopPrice\":\"199\",\"shopDetail\":{\"收费视频\":\"解锁\",\"AI问答\":\"无限\"}}]', '10');
INSERT INTO `orders` VALUES (1861798282695098368, 199.00, '2024-11-27 23:44:36', '600002', '1861728079559299073', '605001', '普通会员', '普通会员', '[{\"shopId\":\"605001\",\"shopType\":\"开通会员\",\"shopName\":\"会员\",\"shopPrice\":\"199\",\"shopDetail\":{\"收费视频\":\"解锁\",\"AI问答\":\"无限\"}}]', '10');

-- ----------------------------
-- Table structure for orders_detail
-- ----------------------------
DROP TABLE IF EXISTS `orders_detail`;
CREATE TABLE `orders_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `order_id` bigint NOT NULL COMMENT '订单id',
  `shop_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '产品id',
  `shop_type` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '产品类型',
  `shop_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产品名称',
  `shop_price` float(10, 2) NOT NULL COMMENT '订单描述',
  `shop_detail` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '产品明细json',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders_detail
-- ----------------------------
INSERT INTO `orders_detail` VALUES (73, '2024-11-13 09:31:22', 1856630927839117312, '605001', '开通会员', '会员', 199.00, '{\"收费视频\":\"解锁\",\"AI问答\":\"无限\"}');
INSERT INTO `orders_detail` VALUES (75, '2024-11-20 01:42:15', 1859049579027673088, '605001', '开通会员', '会员', 199.00, '{\"收费视频\":\"解锁\",\"AI问答\":\"无限\"}');
INSERT INTO `orders_detail` VALUES (79, '2024-11-25 11:32:56', 1861010172689276928, '605001', '开通会员', '会员', 199.00, '{\"收费视频\":\"解锁\",\"AI问答\":\"无限\"}');
INSERT INTO `orders_detail` VALUES (98, '2024-11-27 15:44:37', 1861798282695098368, '605001', '开通会员', '会员', 199.00, '{\"收费视频\":\"解锁\",\"AI问答\":\"无限\"}');

-- ----------------------------
-- Table structure for orders_record
-- ----------------------------
DROP TABLE IF EXISTS `orders_record`;
CREATE TABLE `orders_record`  (
  `id` bigint NOT NULL COMMENT '支付记录号',
  `pay_no` bigint NOT NULL COMMENT '本系统支付交易号',
  `out_pay_no` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第三方支付交易流水号',
  `out_pay_channel` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '第三方支付交易流水号',
  `order_id` bigint NOT NULL COMMENT '订单号',
  `total_price` bigint NOT NULL COMMENT '订单总价单位分',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支付状态',
  `pay_success_time` datetime NULL DEFAULT NULL COMMENT '支付成功时间',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `order_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '订单名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `orders_unioue_id`(`order_id` ASC) USING BTREE COMMENT '外部系统的业务id',
  INDEX `orders_record_out_pay_no_index`(`out_pay_no` ASC) USING BTREE COMMENT '第三方支付订单号',
  INDEX `orders_record__index2`(`pay_no` ASC) USING BTREE COMMENT '本系统支付号'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders_record
-- ----------------------------
INSERT INTO `orders_record` VALUES (1856630927938154498, 1856630927960752128, NULL, NULL, 1856630927839117312, 199, '2024-11-13 17:31:22', '601001', NULL, 1854934298709209090, '普通会员');
INSERT INTO `orders_record` VALUES (1859049579740196866, 1859049579749093376, NULL, NULL, 1859049579027673088, 199, '2024-11-20 09:42:14', '601001', NULL, 1854545533041926145, '普通会员');
INSERT INTO `orders_record` VALUES (1861010173766709249, 1861010173767213056, '2024112522001420320504699823', 'Alipay', 1861010172689276928, 199, '2024-11-25 19:32:56', '601002', '2024-11-25 19:33:58', 1860962119500017666, '普通会员');
INSERT INTO `orders_record` VALUES (1861798283999997953, 1861798284007915520, '2024112722001420320504715542', 'Alipay', 1861798282695098368, 199, '2024-11-27 23:44:36', '601002', '2024-11-27 23:45:03', 1861728079559299073, '普通会员');

SET FOREIGN_KEY_CHECKS = 1;
