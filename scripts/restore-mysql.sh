#!/bin/bash
# ============================================================
# openCallHub MySQL 恢复脚本
# 用法: ./restore-mysql.sh <备份文件路径>
# 示例: ./restore-mysql.sh ./data/backups/openCallHub_20260602_020000.sql.gz
# ============================================================

set -e

BACKUP_FILE="$1"
if [ -z "$BACKUP_FILE" ]; then
    echo "错误: 请指定备份文件路径"
    echo "用法: $0 <备份文件>"
    exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
    echo "错误: 备份文件不存在: $BACKUP_FILE"
    exit 1
fi

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-123456}"
MYSQL_DATABASE="${MYSQL_DATABASE:-openCallHub}"

echo "[$(date)] 警告: 即将覆盖数据库 $MYSQL_DATABASE"
read -p "确认继续? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo "已取消"
    exit 0
fi

echo "[$(date)] 开始恢复 $MYSQL_DATABASE 从 $BACKUP_FILE ..."

gunzip -c "$BACKUP_FILE" | mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"

echo "[$(date)] 恢复完成"
