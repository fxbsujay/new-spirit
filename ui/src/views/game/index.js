import { reactive, ref } from 'vue'
import http from '@/utils/http.js'

export class GameSocket {

    constructor(code) {
        this.code = code
        this.game = reactive({ info: {}, white: {}, black: {}, steps: [] })
        this.loading = ref(false)
        this.success = ref(false)
        this.refresh()
    }

    refresh() {
        this.loading.value = true
        this.success.value = false
        http.get('/game/info/' + this.code).then(res => {
            Object.assign(this.game, res)
            this.loading.value = false
            this.success.value = true
            this.reconnect()
        }).catch(err => {
            console.log(err)
            Object.assign(this.game, { info: {}, white: {}, black: {}, steps: [] })
            this.loading.value = false
        })
    }

    /**
     * 创建连接
     */
    reconnect() {
        if (this.isOpen()) {
            this.socket.close()
        }
        const socket = new WebSocket(`${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/api/ws/${this.code}`)
        socket.onopen = function (event) {
            console.log('socket open', event)
            socket.onmessage = function (event) {
                const msg = JSON.parse(event.data)
                switch (msg.type) {
                    case 'GAME_STEP':
                        this.game.steps.push(msg.data)
                        break
                }
            }
        }
        socket.onclose = function(event) {
            console.log('socket close', event)
        }
        this.socket = socket
    }

    /**
     * 是否连接成功
     * @returns {boolean}
     */
    isOpen() {
        return this.socket && this.socket.readyState === WebSocket.OPEN
    }

    /**
     * 走棋
     * @param x 横坐标
     * @param y 纵坐标
     */
    addStep(x, y) {
        if (this.success && this.isOpen() && !this.game.steps.find(step => step.x === x && step.y === y)) {
            this.socket.send(JSON.stringify({
                type: 'GAME_STEP',
                data: {
                    code: this.code,
                    x,
                    y
                }
            }))
        }
    }
}