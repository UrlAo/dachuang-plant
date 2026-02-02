// server/api/telemetry/[deviceId].post.ts
import { defineEventHandler, readBody } from 'h3'
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

  // 如果没有body.auto_watering为0代表这次没有自动浇水 要调取上一条记录的自动浇水时间
  // 如果为1则录入服务器当前时间
  const auto_watering_time = body.auto_watering === 0 ? db.prepare(`
    SELECT auto_watering FROM telemetry WHERE device_id = ? ORDER BY timestamp DESC LIMIT 1
  `).get(deviceId)?.auto_watering : Math.floor(Date.now() / 1000)

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
    auto_watering_time
  )

  return { success: true }
})