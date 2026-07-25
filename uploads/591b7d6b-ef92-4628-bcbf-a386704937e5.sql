/*
 Navicat Premium Dump SQL

 Source Server         : 陈创
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:2006
 Source Schema         : intellectual-property

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 26/07/2026 00:45:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int NULL DEFAULT 0 COMMENT '显示顺序',
  `url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#' COMMENT '请求地址',
  `target` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '打开方式（menuItem页签 menuBlank新窗口）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `is_refresh` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1' COMMENT '是否刷新（0刷新 1不刷新）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2000 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, '#', '', 'M', '0', '1', NULL, 'Setting', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '专利交底管理', 0, 2, '#', '', 'M', '0', '1', NULL, 'Document', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '专利交底管理目录');
INSERT INTO `sys_menu` VALUES (3, '专利业务管理', 0, 3, '#', '', 'M', '0', '1', NULL, 'FolderOpened', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '专利业务管理目录');
INSERT INTO `sys_menu` VALUES (11, '用户管理', 1, 1, '/system/user', '', 'C', '0', '1', 'system:user:list', 'User', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '用户管理菜单');
INSERT INTO `sys_menu` VALUES (12, '用户角色管理', 1, 2, '/system/user-role', '', 'C', '0', '1', 'system:userRole:list', 'Avatar', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '用户角色管理菜单');
INSERT INTO `sys_menu` VALUES (21, '专利交底', 2, 1, '/patent/disclosure', '', 'C', '0', '1', 'patent:disclosure:list', 'DocumentChecked', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '专利交底列表');
INSERT INTO `sys_menu` VALUES (31, '新申请', 3, 1, '/patent/new-application', '', 'C', '0', '1', 'patent:newApplication:list', 'DocumentAdd', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '新申请列表');
INSERT INTO `sys_menu` VALUES (32, '补漏', 3, 2, '/patent/supplementary', '', 'C', '0', '1', 'patent:supplementary:list', 'CirclePlus', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '补漏列表');
INSERT INTO `sys_menu` VALUES (33, 'PCT', 3, 3, '/patent/pct', '', 'C', '0', '1', 'patent:pct:list', 'Link', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', 'PCT列表');
INSERT INTO `sys_menu` VALUES (34, '中间著变', 3, 4, '/patent/intermediate-change', '', 'C', '0', '1', 'patent:intermediateChange:list', 'Edit', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '中间著变列表');
INSERT INTO `sys_menu` VALUES (35, '复审无效', 3, 5, '/patent/reexamination', '', 'C', '0', '1', 'patent:reexamination:list', 'Warning', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '复审无效列表');
INSERT INTO `sys_menu` VALUES (211, '专利交底查询', 21, 1, '#', '', 'F', '0', '1', 'patent:disclosure:query', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '查询按钮');
INSERT INTO `sys_menu` VALUES (212, '专利交底新增', 21, 2, '#', '', 'F', '0', '1', 'patent:disclosure:add', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '新增按钮');
INSERT INTO `sys_menu` VALUES (213, '专利交底修改', 21, 3, '#', '', 'F', '0', '1', 'patent:disclosure:edit', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '修改按钮');
INSERT INTO `sys_menu` VALUES (214, '专利交底删除', 21, 4, '#', '', 'F', '0', '1', 'patent:disclosure:delete', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', '删除按钮');
INSERT INTO `sys_menu` VALUES (311, '新申请查询', 31, 1, '#', '', 'F', '0', '1', 'patent:newApplication:query', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (312, '新申请新增', 31, 2, '#', '', 'F', '0', '1', 'patent:newApplication:add', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (313, '新申请修改', 31, 3, '#', '', 'F', '0', '1', 'patent:newApplication:edit', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (314, '新申请删除', 31, 4, '#', '', 'F', '0', '1', 'patent:newApplication:delete', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (321, '补漏查询', 32, 1, '#', '', 'F', '0', '1', 'patent:supplementary:query', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (322, '补漏新增', 32, 2, '#', '', 'F', '0', '1', 'patent:supplementary:add', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (323, '补漏修改', 32, 3, '#', '', 'F', '0', '1', 'patent:supplementary:edit', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (324, '补漏删除', 32, 4, '#', '', 'F', '0', '1', 'patent:supplementary:delete', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (331, 'PCT查询', 33, 1, '#', '', 'F', '0', '1', 'patent:pct:query', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (332, 'PCT新增', 33, 2, '#', '', 'F', '0', '1', 'patent:pct:add', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (333, 'PCT修改', 33, 3, '#', '', 'F', '0', '1', 'patent:pct:edit', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (334, 'PCT删除', 33, 4, '#', '', 'F', '0', '1', 'patent:pct:delete', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (341, '中间著变查询', 34, 1, '#', '', 'F', '0', '1', 'patent:intermediateChange:query', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (342, '中间著变新增', 34, 2, '#', '', 'F', '0', '1', 'patent:intermediateChange:add', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (343, '中间著变修改', 34, 3, '#', '', 'F', '0', '1', 'patent:intermediateChange:edit', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (344, '中间著变删除', 34, 4, '#', '', 'F', '0', '1', 'patent:intermediateChange:delete', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (351, '复审无效查询', 35, 1, '#', '', 'F', '0', '1', 'patent:reexamination:query', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (352, '复审无效新增', 35, 2, '#', '', 'F', '0', '1', 'patent:reexamination:add', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (353, '复审无效修改', 35, 3, '#', '', 'F', '0', '1', 'patent:reexamination:edit', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);
INSERT INTO `sys_menu` VALUES (354, '复审无效删除', 35, 4, '#', '', 'F', '0', '1', 'patent:reexamination:delete', '#', 'admin', '2026-07-25 18:33:15', 'admin', '2026-07-25 18:33:15', NULL);

SET FOREIGN_KEY_CHECKS = 1;
