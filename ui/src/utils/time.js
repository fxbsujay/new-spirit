import { padStartTwo } from '@/utils/index.js'

/**
 * 格式化两个时间戳的差值，自动适配天/小时/分钟
 *
 * @param ms 毫秒数
 * @returns {string} 格式示例：
 *   - 不足1小时： "02:03"
 *   - 1小时~24小时："01:02:03"
 *   - 超过24小时："3天 01:02:03"
 */
export const formatTimeDiff = ms => {
    if (!ms || ms < 0) return  ''
    if (ms === 0) return '0'

    const totalSeconds = Math.floor(ms / 1000)
    const totalMinutes = Math.floor(totalSeconds / 60)
    const totalHours = Math.floor(totalMinutes / 60)
    const totalDays = Math.floor(totalHours / 24)

    const timeParts = [totalDays, totalHours % 24, totalMinutes % 60, totalSeconds % 60, ms % 1000]

    if (timeParts[0] >= 1) {
        if (timeParts[1] >= 1) {
            return padStartTwo(timeParts[0]) + '天' + padStartTwo(timeParts[1]) + '小时'
        } else {
            return padStartTwo(timeParts[0]) + '天'
        }
    } else if (timeParts[1] >= 1) {
        return padStartTwo(timeParts[1]) + ':' + padStartTwo(timeParts[2]) + ':' + padStartTwo(timeParts[3])
    } else if (timeParts[2] >= 1) {
        return padStartTwo(timeParts[2]) + ':' + padStartTwo(timeParts[3])
    } else {
        return padStartTwo(timeParts[2]) + ':' + padStartTwo(timeParts[3]) + '.' + padStartTwo(timeParts[4])
    }
}

/**
 * 格式化时间戳距今已经过去多长时间
 *
 * @param timestamp 毫秒时间戳
 * @returns {string} 格式示例：
 *   - 不足1分钟："1秒之前"
 *   - 不足1小时："2分钟之前"、"2分10秒之前"
 *   - 不足1天："1小时之前"、"1小时20分钟之前"
 *   - 超过1天："1天之前"、"1天2小时之前"
 */
export const formatTimeAgo = timestamp => {
    const diff = Date.now() - timestamp
    if (diff <= 0) return '刚刚'

    const totalSeconds = Math.floor(diff / 1000)
    if (totalSeconds < 60) {
        return totalSeconds + '秒之前'
    }

    const totalMinutes = Math.floor(totalSeconds / 60)
    if (totalMinutes < 60) {
        const seconds = totalSeconds % 60
        return totalMinutes + '分钟' + (seconds ? seconds + '秒' : '') + '之前'
    }

    const totalHours = Math.floor(totalMinutes / 60)
    if (totalHours < 24) {
        const minutes = totalMinutes % 60
        return totalHours + '小时' + (minutes ? minutes + '分钟' : '') + '之前'
    }

    const days = Math.floor(totalHours / 24)
    const hours = totalHours % 24
    return days + '天' + (hours ? hours + '小时' : '') + '之前'
}
