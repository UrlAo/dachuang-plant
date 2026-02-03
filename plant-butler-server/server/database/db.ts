// server/database/db.ts
import { join } from 'node:path'
import Database from 'better-sqlite3'

let dbInstance: Database.Database | null = null

export function getDb() {
    if (!dbInstance) {
        const dbPath = join(process.cwd(), 'data.db')
        dbInstance = new Database(dbPath)
    }
    return dbInstance
}