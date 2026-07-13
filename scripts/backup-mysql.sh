#!/bin/bash
# ============================================================
# openCallHub MySQL 备份脚本
# 用法: ./backup-mysql.sh
# 建议 crontab: 0 2 * * * /path/to/backup-mysql.sh
# ============================================================

set -e

BACKUP_DIR="${BACKUP_DIR:-./data/backups}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-123456}"
MYSQL_DATABASE="${MYSQL_DATABASE:-openCallHub}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/${MYSQL_DATABASE}_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "[$(date)] 开始备份 $MYSQL_DATABASE ..."

mysqldump -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --set-gtid-purged=OFF \
    "$MYSQL_DATABASE" | gzip > "$BACKUP_FILE"

echo "[$(date)] 备份完成: $BACKUP_FILE ($(du -h "$BACKUP_FILE" | cut -f1))"

# 删除超过保留期的备份
find "$BACKUP_DIR" -name "${MYSQL_DATABASE}_*.sql.gz" -mtime +$RETENTION_DAYS -delete
echo "[$(date)] 清理超过 ${RETENTION_DAYS} 天的旧备份完成"
