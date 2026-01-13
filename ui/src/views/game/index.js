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
     * @param step
     */
    updateStep(step) {
        this.game.steps.push(step)
        if (this.game.info.type !== 'NONE' && this.game.steps.length > 1) {
            const player = this.game.steps.length % 2 === 0 ? 'white' : 'black'
            this.game[player].remainder += this.game.info.stepDuration
        }
        this.timerStart()
    }

    /**
     * 计时器
     */
    timerStart() {
        const size = this.game.steps.length
        if (this.game.info.type !== 'NONE' && size > 1) {
            const player = size % 2 === 0 ? 'black' : 'white'
            if (this.timer) {
                clearInterval(this.timer)
            }
            const that = this
            let lastTime = that.game.steps[size - 1].timestamp

            const now = Date.now()
            const time = (now - lastTime)
            that.game[player].remainder -= time
            if (that.game[player].remainder < 0) {
                that.game[player].remainder = 0
            }
            lastTime = now
            this.timer = setInterval(() => {
                const now = Date.now()
                const time = (now - lastTime)
                that.game[player].remainder -= time
                if (that.game[player].remainder < 0) {
                    that.game[player].remainder = 0
                }
                lastTime = now
            }, 10)
        } else {
            clearInterval(this.timer)
            this.timer = null
        }
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