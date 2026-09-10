import snackbar from '@/components/snackbar/index.js'
import { useUserStore } from '@/stores/user.js'

export const ContentType = {
    form: 'application/x-www-form-urlencoded',
    json: 'application/json;charset=utf-8',
    multipart: 'multipart/form-data'
}

export const Method = {
    GET: 'GET',
    POST: 'POST',
    PUT: 'PUT',
    PATCH: 'PATCH',
    DELETE: 'DELETE'
}

/**
 * 根据路径和参数构建请求URL
 */
export const buildURL = (url, params) => {
    if (!params) {
        return url
    }

    Object.keys(params).forEach((key, index) => {
        const val = params[key]
        if (val === null || typeof val === 'undefined') {
            return
        }
        url += (index === 0 ? '?' : '&') + key + '=' + val
    })

    return url
}

class Http {

    constructor(apiPrefix) {
        this.apiPrefix = apiPrefix
    }

    api(path, options) {
        return new Promise((resolve, reject) => {
            fetch(this.apiPrefix + path, { ...options }).then(res => {
                if (res.status === 200) {
                    const contentType = res.headers.get('content-type')
                    if (contentType && contentType.includes('application/json')) {
                        resolve(res.json())
                    } else {
                        resolve(res.text())
                    }
                } else if (res.status === 500) {
                    const code = parseInt(res.statusText)
                    if (Number.isInteger(parseInt(res.statusText))) {
                        reject({
                            code,
                            message: RestStatus[code],
                        })
                    } else {
                        reject({
                            code: 500,
                            message:'服务器异常',
                        })
                    }
                } else {
                    switch (res.status) {
                        case 400:
                            snackbar.warning('非法操作')
                            break
                        case 401:
                            useUserStore().logout()
                            break
                        case 403:
                            snackbar.warning('请登录后操作')
                            break
                        case 404:
                            snackbar.error('网络异常')
                            break
                    }
                    reject({ code: res.status, message: res.status })
                }

            })
        })
    }

    get(path, params) {
        const options = {
            method: Method.GET,
            headers: {
                'Content-Type': ContentType.form
            }
        }
        return this.api(buildURL(path, params), options)
    }

    post(path, data) {
        const options = {
            method: Method.POST,
            body: data ? JSON.stringify(data) : null,
            headers: {
                'Content-Type': ContentType.json
            }
        }
        return this.api(path, options)
    }
}

const http = new Http('/api')

export default http


const RestStatus = {
    10001: '邮箱已被注册',
    10002: '用户名已被注册',
    10003: '账户不存在',
    10004: '密码错误',
    10005: '账户已被封禁',
    10006: '验证码过期或已失效',
    10007: '验证码错误',
    10008: '验证码已发送',
    20001: '已创建对局',
    20002: '对局不存在',
    20003: '对局已开始',
}
