/**
 * 防抖动
 * @param func  执行的函数
 * @param delay 多少秒之后执行
 * @param immediate 是否立即执行
 */
export const debounce = (func, delay = 500, immediate = true) => {
    let timeout
    return function () {
        const context = this, args = arguments
        const later = function () {
            timeout = null
            if (!immediate) func.apply(context, args)
        }
        const callNow = immediate && !timeout
        clearTimeout(timeout)
        timeout = setTimeout(later, delay)
        if (callNow) func.apply(context, args)
    }
}

/**
 * 节流
 *
 * @param func {function} 执行的函数
 * @param delay {number} 多少秒之内执行一次
 */
export const throttle = (func, delay) => {
    let prev = Date.now()
    return function () {
        const context = this
        const args = arguments
        const now = Date.now()
        if (now - prev >= delay) {
            func.apply(context, args)
            prev = Date.now()
        }
    }
}

/**
 * 检测密码强度
 * @param password ｛string｝ 密码
 * @returns {number} 强度 0 - 4
 */
export const passwordStrength = password => {

    if (!password || password.length <= 0) {
        return 0
    }
    let strength = 0
    if (password.length > 10) {
        strength++
    }
    if (/\d/.test(password)) {
        strength++
    }

    if (/[a-zA-Z]/.test(password)) {
        strength++
    }

    if (/[@!$^.*_%]/.test(password)) {
        strength++
    }
    return strength
}

/**
 * 格式化毫秒数
 * @param ms 毫秒数
 */
export const formatTime = ms => {
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

export const padStartTwo = num => {
    return num.toFixed(0).padStart(2, '0')
}
