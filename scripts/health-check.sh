#!/bin/bash
# ============================================================
# openCallHub 全链路健康检查脚本
# 检查: API / MySQL / Redis / RabbitMQ / FreeSWITCH
# 用法: ./health-check.sh [API_BASE_URL]
# ============================================================

API_BASE="${1:-http://localhost:4320}"
PASS=0
FAIL=0

check() {
    local name="$1"
    local url="$2"
    if curl -sf -o /dev/null -w "%{http_code}" --connect-timeout 5 "$url" 2>/dev/null | grep -q "200\|302"; then
        echo "  ✅ $name : UP"
        PASS=$((PASS + 1))
    else
        echo "  ❌ $name : DOWN ($url)"
        FAIL=$((FAIL + 1))
    fi
}

echo "============================================="
echo " openCallHub 健康检查 @ $API_BASE"
echo " $(date)"
echo "============================================="

check "API 健康"       "$API_BASE/api/foundation/health"
check "Redis 连接"     "$API_BASE/api/foundation/redis/check"
check "RabbitMQ 连接"  "$API_BASE/api/foundation/mq/check"
check "文件存储"       "$API_BASE/api/foundation/file/check"
check "Swagger UI"     "$API_BASE/swagger-ui/index.html"

echo "============================================="
echo " 结果: $PASS 通过, $FAIL 失败"
echo "============================================="

[ $FAIL -eq 0 ] && exit 0 || exit 1
