// server/api/telemetry/[deviceId].post.ts
import { defineEventHandler, readBody } from '#imports'
import { getDb } from '../../database/db'

export default defineEventHandler(async (event) => {
    const deviceId = event.context.params?.deviceId as string
    const body = await readBody(event)

    // 简单验证 secret（实际应查数据库比对）
    if (body.secret !== 'secret') {
        return { error: 'Invalid secret' }
    }

    const db = getDb()

    // 更新设备状态
    db.prepare(`
    UPDATE devices SET status = 'online', last_seen = ? WHERE id = ?
  `).run(Math.floor(Date.now() / 1000), deviceId)

    // 插入遥测数据
    db.prepare(`
    INSERT INTO telemetry (
      device_id, timestamp, soil_humidity, temperature,
      light_intensity, air_humidity, auto_watering
    ) VALUES (?, ?, ?, ?, ?, ?, ?)
  `).run(
        deviceId,
        Math.floor(Date.now() / 1000),
        body.soil_humidity || null,
        body.temperature || null,
        body.light_intensity || null,
        body.air_humidity || null,
        body.auto_watering || null
    )

    return { success: true }
})