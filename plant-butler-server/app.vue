<template>
    <div style="padding: 20px; font-family: Arial">
        <el-container>
            <el-header>
                <h1>🌱 智能浇水系统</h1>
                <p>数据每 5 秒自动刷新</p>
            </el-header>
            <el-main>
                <el-row :gutter="20">
                    <el-col :span="8" v-for="device in devices" :key="device.id">
                        <el-card :body-style="{ padding: '25px' }" shadow="hover">
                            <div slot="header" class="clearfix">
                                <span>{{ device.name }} ({{ device.id }})</span>
                                <el-tag :type="statusTagType(device.status)" effect="dark" style="float: right;">{{
                                    device.status }}</el-tag>
                            </div>
                            <div>
                                <p>最后在线: {{ formatTime(device.last_seen) }}</p>
                                <div v-if="device.telemetry">
                                    <p><i class="el-icon-coin"></i> 空气湿度:
                                        <el-progress :percentage="device.telemetry.air_humidity"
                                            :color="progressColor(device.telemetry.air_humidity)"></el-progress>
                                    </p>
                                    <p><i class="el-icon-coin"></i> 土壤湿度:
                                        <el-progress :percentage="device.telemetry.soil_humidity"
                                            :color="progressColor(device.telemetry.soil_humidity)"></el-progress>
                                    </p>
                                    <p><i class="el-icon-sunny"></i> 温度: {{ device.telemetry.temperature }}°C</p>
                                    <p><i class="el-icon-lightning"></i> 光照: {{ device.telemetry.light_intensity }} lux
                                    </p>
                                    <p><i class="el-icon-water-cup"></i> 上一次浇水: {{
                                        formatTime(device.telemetry.auto_watering) }}</p>
                                </div>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
            </el-main>
        </el-container>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const devices = ref([])

// 获取数据
const fetchData = async () => {
    const res = await $fetch('/api/devices')
    devices.value = res
}

// 每5秒刷新
onMounted(() => {
    fetchData()
    setInterval(fetchData, 5000)
})

// 时间格式化
const formatTime = (timestamp) => {
    if (!timestamp) return '从未上线'
    return new Date(timestamp * 1000).toLocaleString()
}

// 根据设备状态返回标签颜色
const statusTagType = (status) => {
    return status === 'online' ? 'success' : 'info'
}

// 根据土壤湿度返回进度条颜色
const progressColor = (soilHumidity) => {
    if (soilHumidity < 30) return '#F56C6C' // 红色
    if (soilHumidity >= 30 && soilHumidity < 70) return '#E6A23C' // 黄色
    return '#67C23A' // 绿色
}
</script>

<style scoped>
.clearfix:before,
.clearfix:after {
    display: table;
    content: "";
}

.clearfix:after {
    clear: both
}
</style>