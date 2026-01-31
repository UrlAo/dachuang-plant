// server/api/register.post.ts
import { defineEventHandler, readBody, setResponseStatus } from 'h3'
import { getDb } from '../database/db'

export default defineEventHandler(async (event) => {
    const body = await readBody(event)
    const { username, password, email } = body

    // 简单校验
    if (!username || !password) {
        setResponseStatus(event, 400)
        return {
            success: false,
            error: 'username and password are required'
        }
    }

    const db = getDb()

    try {
        // 检查用户名是否已存在
        const existingUser = db.prepare('SELECT id FROM users WHERE username = ?').get(username)
        if (existingUser) {
            setResponseStatus(event, 409)
            return {
                success: false,
                error: '该用户名已注册请更换用户名'
            }
        }

        const createdAt = Math.floor(Date.now() / 1000)

        // 插入新用户（密码明文存入 password_hash）
        const result = db.prepare(`
      INSERT INTO users (username, email, password_hash, created_at)
      VALUES (?, ?, ?, ?)
    `).run(username, email, password, createdAt)

        if (result.changes === 0) {
            return { success: false, error: 'Registration failed' }
        }

        // 获取新注册的用户信息
        const newUser = db.prepare('SELECT id, username, email, created_at FROM users WHERE username = ?').get(username)

        return {
            success: true,
            message: 'User registered successfully',
            user: {
                id: newUser.id,
                username: newUser.username,
                email: newUser.email
            }
        }
    } catch (err) {
        console.error('Register error:', err)
        return {
            success: false,
            error: 'Internal server error'
        }
    }
})