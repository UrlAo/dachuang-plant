// server/api/history.get.ts
import { defineEventHandler, getQuery } from 'h3'
import { getDb } from '../database/db'

export default defineEventHandler((event) => {
    const query = getQuery(event)
    const deviceId = query.id?.toString()
    const daysStr = query.days?.toString()

    // 校验 deviceId
    if (!deviceId) {
        return {
            success: false,
            error: 'Missing required query parameter: id'
        }
    }

    // 解析 days，默认 7 天
    const days = daysStr ? parseInt(daysStr, 10) : 7
    if (isNaN(days) || days <= 0 || days > 365) {
        return {
            success: false,
            error: 'Invalid "days" parameter. Must be a number between 1 and 365.'
        }
    }

    const db = getDb()

    try {
        // 计算时间戳下限：当前时间 - days * 86400 秒
        const now = Math.floor(Date.now() / 1000)
        const sinceTimestamp = now - days * 24 * 60 * 60

        // 查询该设备最近 N 天的遥测数据，按时间倒序（最新在前）
        const history = db
            .prepare(`
        SELECT *
        FROM telemetry
        WHERE device_id = ?
          AND timestamp >= ?
        ORDER BY timestamp DESC
      `)
            .all(deviceId, sinceTimestamp)

        return {
            success: true,
            device_id: deviceId,
            days,
            count: history.length,
            data: history
        }
    } catch (err) {
        console.error('Fetch history error:', err)
        return {
            success: false,
            error: 'Failed to fetch history data'
        }
    }
})