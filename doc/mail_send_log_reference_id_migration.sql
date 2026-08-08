-- ============================================================================
-- mail_send_log: disclosure_id -> reference_id 迁移脚本
-- 适用数据库：intellectual-property (MySQL 8.0)
--
-- 背景：邮件发送记录表原使用 disclosure_id(bigint) 记录专利交底ID，
--      现改为 reference_id(varchar(100))，可同时存 交底ID 或 内部编号(P表关联键)。
--      后续 P 表（专利申请）处理时按 reference_id 关联邮件记录。
--
-- 用法：在你需要升级的数据库上直接执行本文件即可，脚本会自动处理
--      "列仍叫 disclosure_id" 与 "已改名 reference_id 但仍是 bigint" 两种情况。
-- ============================================================================

DROP PROCEDURE IF EXISTS `upgrade_mail_send_log_reference_id`;

DELIMITER $$

CREATE PROCEDURE `upgrade_mail_send_log_reference_id`()
BEGIN
    DECLARE has_old_col INT DEFAULT 0;
    DECLARE col_type VARCHAR(64) DEFAULT NULL;

    -- 判断当前列名
    SELECT COUNT(*) INTO has_old_col
      FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'mail_send_log'
       AND COLUMN_NAME = 'disclosure_id';

    SELECT DATA_TYPE INTO col_type
      FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'mail_send_log'
       AND COLUMN_NAME = 'reference_id';

    -- 1) 列：disclosure_id(bigint) -> reference_id(varchar(100))
    IF has_old_col = 1 THEN
        ALTER TABLE `mail_send_log`
            CHANGE COLUMN `disclosure_id`
            `reference_id` varchar(100) DEFAULT NULL COMMENT '关联ID：交底ID或内部编号(P表关联键)';
    ELSEIF col_type IS NOT NULL AND col_type <> 'varchar' THEN
        -- 已改名但类型仍是 bigint（如之前只执行了 CHANGE 没改类型）
        ALTER TABLE `mail_send_log`
            MODIFY COLUMN `reference_id` varchar(100) DEFAULT NULL COMMENT '关联ID：交底ID或内部编号(P表关联键)';
    END IF;

    -- 2) 关联索引（已存在则跳过）
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
         WHERE TABLE_SCHEMA = DATABASE()
           AND TABLE_NAME = 'mail_send_log'
           AND INDEX_NAME = 'idx_msl_reference_id'
    ) THEN
        ALTER TABLE `mail_send_log` ADD INDEX `idx_msl_reference_id` (`reference_id`);
    END IF;

    -- 3) 回填历史数据：把 reference_id 里的交底数字ID 替换为该交底对应的内部编号(P表关联键)
    --    仅处理纯数字值，避免重复执行时把已回填的内部编号(P2025010等)再次当作ID
    UPDATE `mail_send_log` l
      JOIN `patent_disclosure` d ON d.id = CAST(l.reference_id AS UNSIGNED)
       SET l.reference_id = d.internal_no
     WHERE l.reference_id IS NOT NULL
       AND l.reference_id REGEXP '^[0-9]+$'
       AND d.internal_no IS NOT NULL
       AND d.internal_no != '';
END$$

DELIMITER ;

CALL `upgrade_mail_send_log_reference_id`();

DROP PROCEDURE IF EXISTS `upgrade_mail_send_log_reference_id`;

-- ============================================================================
-- 校验（只查本脚本涉及的列，避免老库没有 business_type/business_ref 时报错）
-- ============================================================================
SELECT id, reference_id, send_status
  FROM `mail_send_log`
 ORDER BY id;

-- 预期结果示例：
--   id   reference_id   send_status
--   13   P2025010       1
--   14   NULL           1
--   15   P2025010       1
