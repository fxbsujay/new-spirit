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
    const totalSeconds = Math.floor(ms / 1000)
    const totalMinutes = Math.floor(totalSeconds / 60)
    const totalHours = Math.floor(totalMinutes / 60)
    const totalDays = Math.floor(totalHours / 24)

    const timeParts = [totalDays, totalHours % 24, totalMinutes % 60, totalSeconds % 60, ms % 1000 ]

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
