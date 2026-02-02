// server/api/command.post.ts
import { defineEventHandler, readBody } from 'h3'
import { getDb } from '../database/db'

export default defineEventHandler(async (event) => {
    const body = await readBody(event)
    const { deviceId, command, duration } = body

    const db = getDb()
    db.prepare(`
    INSERT INTO commands (device_id, command, params, created_at)
    VALUES (?, ?, ?, ?)
  `).run(
        deviceId,
        command,
        JSON.stringify({ duration }),
        Math.floor(Date.now() / 1000)
    )

    return { success: true, message: 'Command queued' }
})