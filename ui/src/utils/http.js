import snackbar from '@/components/snackbar/index.js'
import { useUserStore } from '@/stores/user.js'

export const ContentType ={
    form: 'application/x-www-form-urlencoded',
    json: 'application/json;charset=utf-8',
    multipart: 'multipart/form-data'
}

export const Method = {
    GET: 'GET',
    POST: 'POST',
    PUT: 'PUT',
    PATCH: 'PATCH',
    DELETE: 'DELETE',
}

/**
 * 根据路径和参数构建请求URL
 */
export const buildURL = (url, params)=> {
    if (!params){
        return url
    }

    Object.keys(params).forEach((key, index) => {
        const val = params[key]
        if (val === null || typeof val === 'undefined'){
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
            fetch(this.apiPrefix + path, {...options}).then(res => {
                switch (res.status) {
                    case 200:
                        const contentType = res.headers.get('content-type')
                        if (contentType && contentType.includes('application/json')){
                            resolve(res.json())
                        } else {
                            resolve(res.text())
                        }
                        break;
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
                    case 500:
                        if (Number.isInteger(parseInt(res.statusText))) {
                            res.json().then(err => {
                                snackbar.warning(err.message)
                                reject(err)
                            }).catch(() => {
                                reject()
                            })
                        } else {
                            snackbar.error('网络异常')
                            reject()
                        }
                        break
                }
            })
        })
    }

    get(path, params) {
        const options = {
            method: Method.GET,
            headers: {
                "Content-Type": ContentType.form
            },
        }
        return this.api(buildURL(path, params), options)
    }

    post(path, data) {
        const options = {
            method: Method.POST,
            body: data ? JSON.stringify(data) : null,
            headers: {
                "Content-Type": ContentType.json
            },
        }
        return this.api(path, options)
    }
}

const http = new Http('/api')

export default http