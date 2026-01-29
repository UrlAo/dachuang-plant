// server/plugins/device-status-monitor.ts
import { getDb } from '../database/db'

let monitorInterval: NodeJS.Timeout | null = null

export default defineNitroPlugin(() => {
    // 仅在非构建阶段运行（避免 build 时启动）
    if (process.env.NITRO_DEV || process.env.NODE_ENV === 'development') {
        startDeviceMonitor()
    } else {
        // 生产环境也启动（可选）
        startDeviceMonitor()
    }
})

function startDeviceMonitor() {
    const CHECK_INTERVAL = 5000 // 每 5 秒检查一次
    const OFFLINE_THRESHOLD = 10000 // 10 秒超时（毫秒）

    monitorInterval = setInterval(() => {
        try {
            const db = getDb()
            const now = Date.now()

            // 查找所有 online 设备
            const onlineDevices = db
                .prepare(`SELECT id, last_seen FROM devices WHERE status = 'online'`)
                .all()

            for (const device of onlineDevices) {
                const lastSeenMs = device.last_seen * 1000 // 转回毫秒
                if (now - lastSeenMs > OFFLINE_THRESHOLD) {
                    // 超时，设为离线
                    db.prepare(`UPDATE devices SET status = 'offline' WHERE id = ?`)
                        .run(device.id)
                    console.log(`[Device Monitor] ${device.id} marked as offline`)
                }
            }
        } catch (err) {
            console.error('[Device Monitor] Error:', err)
        }
    }, CHECK_INTERVAL)

    // 开发环境下，监听进程退出，清理定时器
    const cleanup = () => {
        if (monitorInterval) {
            clearInterval(monitorInterval)
            console.log('[Device Monitor] Stopped')
        }
    }

    process.on('SIGINT', cleanup)
    process.on('SIGTERM', cleanup)
}