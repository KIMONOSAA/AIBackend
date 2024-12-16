/*
 Navicat Premium Data Transfer

 Source Server         : ai
 Source Server Type    : MySQL
 Source Server Version : 80024 (8.0.24)
 Source Host           : 192.168.101.132:3306
 Source Schema         : code

 Target Server Type    : MySQL
 Target Server Version : 80024 (8.0.24)
 File Encoding         : 65001

 Date: 02/12/2024 16:52:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dictionary
-- ----------------------------
DROP TABLE IF EXISTS `dictionary`;
CREATE TABLE `dictionary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id标识',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据字典名称',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据字典代码',
  `item_values` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '数据字典项--json格式\r\n  ',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `tb_code_unique`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据字典' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dictionary
-- ----------------------------
INSERT INTO `dictionary` VALUES (13, '对象的审核状态', '002', '[{\"code\":\"002001\",\"desc\":\"审核未通过\"},{\"code\":\"002002\",\"desc\":\"未审核\"},{\"code\":\"002003\",\"desc\":\"审核通过\"}]');
INSERT INTO `dictionary` VALUES (15, '课程审核状态', '202', '[{\"code\":\"202001\",\"desc\":\"审核未通过\"},{\"code\":\"202002\",\"desc\":\"未提交\"},{\"code\":\"202003\",\"desc\":\"已提交\"},{\"code\":\"202004\",\"desc\":\"审核通过\"}]');
INSERT INTO `dictionary` VALUES (16, '课程收费情况', '201', '[{\"code\":\"201000\",\"desc\":\"免费\"},{\"code\":\"201001\",\"desc\":\"收费\"}]');
INSERT INTO `dictionary` VALUES (19, '课程发布状态', '203', '[{\"code\":\"203001\",\"desc\":\"未发布\"},{\"code\":\"203002\",\"desc\":\"已发布\"},{\"code\":\"203003\",\"desc\":\"下线\"}]');
INSERT INTO `dictionary` VALUES (20, '订单交易类型状态', '600', '[{\"code\":\"600001\",\"desc\":\"未支付\"},{\"code\":\"600002\",\"desc\":\"已支付\"},{\"code\":\"600003\",\"desc\":\"已关闭\"},{\"code\":\"600004\",\"desc\":\"已退款\"},{\"code\":\"600005\",\"desc\":\"已完成\"}]');
INSERT INTO `dictionary` VALUES (22, '消息通知状态', '003', '[{\"code\":\"003001\",\"desc\":\"未通知\"},{\"code\":\"003002\",\"desc\":\"成功\"}]');
INSERT INTO `dictionary` VALUES (23, '支付记录交易状态', '601', '[{\"code\":\"601001\",\"desc\":\"未支付\"},{\"code\":\"601002\",\"desc\":\"已支付\"},{\"code\":\"601003\",\"desc\":\"已退款\"}]');
INSERT INTO `dictionary` VALUES (25, '第三方支付渠道编号', '603', '[{\"code\":\"603001\",\"desc\":\"微信支付\"},{\"code\":\"603002\",\"desc\":\"支付宝\"}]');
INSERT INTO `dictionary` VALUES (26, '练习作业状态', '604', '[{\"code\":\"604001\",\"desc\":\"练习中\"},{\"code\":\"604002\",\"desc\":\"练习时间超出\"},{\"code\":\"604003\",\"desc\":\"练习完成\"}]');
INSERT INTO `dictionary` VALUES (28, '用户会员状态', '605', '[{\"code\":\"605001\",\"desc\":\"会员\"},{\"code\":\"605002\",\"desc\":\"超级会员\"},{\"code\":\"605003\",\"desc\":\"普通用户\"}]');
INSERT INTO `dictionary` VALUES (30, '用户角色状态', '606', '[{\"code\":\"700001\",\"desc\":\"USER\"},{\"code\":\"700002\",\"desc\":\"ADMIN\"},{\"code\":\"700003\",\"desc\":\"SUPERUSER\"}]');
INSERT INTO `dictionary` VALUES (31, '用户权限状态', '607', '[{\"code\":\"800001\",\"desc\":\"SUPER_USER_ALL\"}]');
INSERT INTO `dictionary` VALUES (32, '管理权限课程状态', '608', '[{\"code\":\"900001\",\"desc\":\"MANAGER_COURSE_ADD\"},{\"code\":\"900002\",\"desc\":\"MANAGER_COURSE_READ\"},{\"code\":\"900003\",\"desc\":\"MANAGER_COURSE_UPDATE\"},{\"code\":\"900004\",\"desc\":\"MANAGER_COURSE_DELETE\"},{\"code\":\"900006\",\"desc\":\"MANAGER_COURSE_AUDIT\"}]');
INSERT INTO `dictionary` VALUES (33, '管理员权限练习状态', '609', '[{\"code\":\"1000001\",\"desc\":\"MANAGER_PRACTICE_ADD\"},{\"code\":\"1000002\",\"desc\":\"MANAGER_PRACTICE_READ\"},{\"code\":\"1000003\",\"desc\":\"MANAGER_PRACTICE_UPDATE\"},{\"code\":\"1000004\",\"desc\":\"MANAGER_PRACTICE_DELETE\"}]');
INSERT INTO `dictionary` VALUES (34, '管理员视频权限状态', '700', '[{\"code\":\"2000001\",\"desc\":\"MANAGER_VIDEO_ADD\"},{\"code\":\"2000002\",\"desc\":\"MANAGER_VIDEO_READ\"},{\"code\":\"2000003\",\"desc\":\"MANAGER_VIDEO_UPDATE\"},{\"code\":\"2000004\",\"desc\":\"MANAGER_VIDEO_DELETE\"}]');
INSERT INTO `dictionary` VALUES (35, '管理员用户权限状态', '701', '[{\"code\":\"3000001\",\"desc\":\"MANAGER_USER_ADD\"},{\"code\":\"3000002\",\"desc\":\"MANAGER_USER_READ\"},{\"code\":\"3000003\",\"desc\":\"MANAGER_USER_UPDATE\"},{\"code\":\"3000004\",\"desc\":\"MANAGER_USER_DELETE\"},{\"code\":\"3000005\",\"desc\":\"MANAGER_SUPERUSER_ADD\"}]');
INSERT INTO `dictionary` VALUES (36, '管理员权限状态', '702', '[{\"code\":\"114514\",\"desc\":\"PERMISSION_ALL\"}]');

SET FOREIGN_KEY_CHECKS = 1;
