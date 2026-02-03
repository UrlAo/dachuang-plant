import { defineNuxtConfig } from 'nuxt/config'

export default defineNuxtConfig({
    modules: ['@element-plus/nuxt'],
    devServer: {
        host: '0.0.0.0',
        port: 3000
    }
})