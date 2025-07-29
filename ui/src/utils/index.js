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