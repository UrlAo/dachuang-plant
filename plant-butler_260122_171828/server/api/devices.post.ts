// server/api/devices.post.ts
import { defineEventHandler, readBody } from 'h3'
import { getDb } from '../database/db'

export default defineEventHandler(async (event) => {
    const body = await readBody(event)
    const { user_id, name, secret } = body

    if (!user_id || !name || !secret) {
        return {
            error: 'Missing required fields: user_id, name, secret'
        }
    }

    const db = getDb()

    try {
        // 获取当前最大的 id（作为整数）
        const maxIdRow = db.prepare(`
      SELECT MAX(CAST(id AS INTEGER)) as max_id FROM devices
    `).get()

        const nextIdNum = (maxIdRow?.max_id || 0) + 1

        // 限制最大设备数为 9999（可选）
        if (nextIdNum > 9999) {
            return {
                error: 'Device limit reached (max 9999 devices)'
            }
        }

        // 格式化为 4 位字符串，如 5 → "0005"
        const deviceId = nextIdNum.toString().padStart(4, '0')

        const createdAt = Math.floor(Date.now() / 1000)

        // 插入新设备
        const result = db.prepare(`
      INSERT INTO devices (id, user_id, name, secret, status, created_at)
      VALUES (?, ?, ?, ?, 'offline', ?)
    `).run(deviceId, user_id, name, secret, createdAt)

        if (result.changes === 0) {
            return { error: 'Failed to register device' }
        }

        return {
            success: true,
            device_id: deviceId,
            message: 'Device registered successfully'
        }
    } catch (err) {
        console.error('Register device error:', err)
        return {
            error: 'Internal server error',
        }
    }
})