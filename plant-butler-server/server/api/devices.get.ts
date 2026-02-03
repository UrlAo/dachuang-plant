// server/api/devices.get.ts
import { defineEventHandler, getQuery } from 'h3'
import { getDb } from '../database/db'

export default defineEventHandler((event) => {
    const query = getQuery(event)
    const userId = query.user_id?.toString()
    const db = getDb()

    // 获取每个设备的最新遥测数据（纯 SQL）
    const latestTelemetry = db.prepare(`
    SELECT t.*
    FROM (
      SELECT *,
             ROW_NUMBER() OVER (PARTITION BY device_id ORDER BY timestamp DESC) as rn
      FROM telemetry
    ) t
    WHERE t.rn = 1
  `).all()

    // 根据 user_id 过滤设备
    let devices
    if (userId) {
        devices = db.prepare(`
            SELECT id, name, status, last_seen FROM devices WHERE user_id = ?
        `).all(userId)
    } else {
        devices = db.prepare(`
            SELECT id, name, status, last_seen FROM devices
        `).all()
    }

    // 合并（现在 latestTelemetry 已经是"每个设备最新"）
    const telemetryMap = new Map(
        latestTelemetry.map(t => [t.device_id, t])
    )

    return devices.map(d => ({
        ...d,
        telemetry: telemetryMap.get(d.id) || null
    }))
})