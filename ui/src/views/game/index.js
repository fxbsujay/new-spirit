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
            this.timerStart()
        }).catch(err => {
            console.log(err)
            Object.assign(this.game, { info: {}, white: {}, black: {}, steps: [] })
            this.loading.value = false
            this.success.value = false
        })
    }

    /**
     * 游戏落子更新
     *
     * @param data { { step: { x: number, y: number, timestamp: number }, blackRemainder: number, whiteRemainder: number } }
     */
    updateStep(data) {
        this.game.steps.push(data.step)
        this.game.black.remainder = data.blackRemainder
        this.game.white.remainder = data.whiteRemainder
        console.log('B', this.game.black.remainder, 'W', this.game.white.remainder)
    }

    /**
     * 计时器
     */
    timerStart() {
        requestAnimationFrame(() => this.timerStart())
        const size = this.game.steps.length
        if (this.game.info.type === 'NONE' || size <= 1) {
            return
        }
        const player = size % 2 === 0 ? 'black' : 'white'
        if (!this.lastTime) {
            this.lastTime =  this.game.steps[size - 1].timestamp
        }
        const now = Date.now()
        const time = (now - this.lastTime)
        this.game[player].remainder -= time
        if (this.game[player].remainder < 0) {
            this.game[player].remainder = 0
        }
        this.lastTime = now
    }

    /**
     * 创建连接
     */
    reconnect() {
        if (this.isOpen()) {
            this.socket.close()
        }
        const socket = new WebSocket(`${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/api/ws/${this.code}`)
        const that = this
        socket.onopen = function (event) {
            console.log('socket open', event)
            socket.onmessage = function (event) {
                const msg = JSON.parse(event.data)
                console.log('socket msg', msg)
                switch (msg.type) {
                    case 'GAME_STEP':
                        that.updateStep(msg.data)
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