// server/database/init.ts
import { join } from 'node:path'
import Database from 'better-sqlite3'

const dbPath = join(process.cwd(), 'data.db')
const db = new Database(dbPath)

// 创建表（无外键）
db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT,
    email TEXT,
    password_hash TEXT,
    created_at INTEGER
  );

  CREATE TABLE IF NOT EXISTS devices (
    id TEXT PRIMARY KEY,
    user_id INTEGER,
    name TEXT,
    secret TEXT,
    status TEXT DEFAULT 'offline',
    last_seen INTEGER,
    location TEXT,
    created_at INTEGER
  );

  CREATE TABLE IF NOT EXISTS telemetry (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id TEXT,
    timestamp INTEGER,
    soil_humidity REAL,
    temperature REAL,
    light_intensity INTEGER,
    air_humidity REAL,
    auto_watering INTEGER
  );

  CREATE TABLE IF NOT EXISTS commands (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id TEXT,
    command TEXT,
    params TEXT,
    status TEXT DEFAULT 'pending',
    created_at INTEGER,
    executed_at INTEGER
  );
`)

// 插入示例设备（方便测试）
db.prepare('INSERT OR IGNORE INTO devices (id, name, secret, created_at) VALUES (?, ?, ?, ?)')
    .run('0001', '阳台花盆', 'secret', Math.floor(Date.now() / 1000))
db.prepare('INSERT OR IGNORE INTO devices (id, name, secret, created_at) VALUES (?, ?, ?, ?)')
    .run('0002', '厨房花盆', 'secret', Math.floor(Date.now() / 1000))

db.prepare('INSERT OR IGNORE INTO users (username, email, password_hash, created_at) VALUES (?, ?, ?, ?)')
    .run('admin', '<EMAIL>', '123456', Math.floor(Date.now() / 1000))

// 上一次自动浇水为1代表空记录
db.prepare('INSERT OR IGNORE INTO telemetry (device_id, timestamp, soil_humidity, temperature, light_intensity, air_humidity, auto_watering) VALUES (?, ?, ?, ?, ?, ?, ?)')
    .run('0001', Math.floor(Date.now() / 1000), 25, 25, 1000, 50, 1)

console.log('✅ SQLite initialized at', dbPath)
db.close()