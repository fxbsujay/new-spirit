import snackbar from '@/components/snackbar/index.js'

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

class HttpError extends Error {

    constructor(status, text) {
        super(text)
        this.status = status
    }
}

class Http {

    constructor(apiPrefix) {
        this.apiPrefix = apiPrefix
    }

    api(path, options) {

        return new Promise((resolve, reject) => {
            fetch(this.apiPrefix + path, options).then(res => {

                if (res.status === 200) {
                    const contentType = res.headers.get('content-type')
                    if (contentType.includes('application/json')){
                        return res.json()
                    } else {
                        return res.text()
                    }
                } else {
                    throw new HttpError(res.status, res.statusText)
                }
            })
                .then(res => {
                    if (res.code !== 200) {
                        throw new HttpError(res.code, res.msg)
                    } else {
                        resolve(res)
                    }
                }).catch(err => {
                    if (err.status > 10000) {
                        snackbar.error(err.message)
                    } else {
                        // switch (err.status) {
                        //     case 400:
                        //         snackbar.error('非法操作')
                        //         break
                        //     case 401:
                        //     case 403:
                        //         snackbar.warning('请登录后操作')
                        //         break
                        //     case 404:
                        //         snackbar.error('网络异常')
                        //         break
                        //     case 500:
                        //         snackbar.error('操作失败')
                        //         break
                        // }
                    }
                    reject(err)
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
            body: JSON.stringify(data),
            headers: {
                "Content-Type": ContentType.json
            },
        }
        return this.api(path, options)
    }
}

const http = new Http('/api')

export default http