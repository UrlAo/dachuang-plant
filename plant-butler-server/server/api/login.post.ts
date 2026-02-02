// server/api/login.post.ts
import { defineEventHandler, readBody, setResponseStatus } from 'h3'
import { getDb } from '../database/db'

export default defineEventHandler(async (event) => {
    const body = await readBody(event)
    const { username, password } = body

    if (!username || !password) {
        setResponseStatus(event, 400)
        return {
            success: false,
            error: 'Username and password are required'
        }
    }

    const db = getDb()

    try {
        // 查询用户
        const user = db.prepare(`
      SELECT id, username, email, password_hash FROM users WHERE username = ?
    `).get(username)

        if (!user) {
            setResponseStatus(event, 401)
            return {
                success: false,
                error: '用户名不存在'
            }
        }

        // 比对明文密码（⚠️ 仅开发用！）
        if (user.password_hash !== password) {
            setResponseStatus(event, 401)
            return {
                success: false,
                error: '您的密码不正确'
            }
        }

        // 登录成功（这里不实现 token，只返回用户信息）
        return {
            success: true,
            message: 'Login successful',
            user: {
                id: user.id,
                username: user.username,
                email: user.email
            }
        }
    } catch (err) {
        console.error('Login error:', err)
        return {
            success: false,
            error: 'Internal server error'
        }
    }
})