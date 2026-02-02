// server/middleware/cors.ts
import { defineEventHandler, setHeader } from 'h3'

export default defineEventHandler((event) => {
    // 设置CORS头，允许所有来源
    setHeader(event, 'Access-Control-Allow-Origin', '*')
    setHeader(event, 'Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
    setHeader(event, 'Access-Control-Allow-Headers', 'Content-Type, Authorization')
    
    // 处理 OPTIONS 预检请求
    if (event.node.req.method === 'OPTIONS') {
        event.node.res.statusCode = 200
        event.node.res.end()
    }
})
