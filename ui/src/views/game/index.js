import http from '@/utils/http.js'
import router from '@/router/index.js'
import { reactive, ref } from 'vue'

export class GameSocket {

    constructor(code, readMsgHandle) {
        this.code = code
        this.readMsgHandle = readMsgHandle
        this.board = ref([])
        this.game = reactive({ info: {}, white: {}, black: {}, steps: [] })
        this.chatMessages = ref([])
        this.loading = ref(false)
        this.success = ref(false)
        this.refresh()
    }

    /**
     * 刷新
     */
    refresh() {
        this.loading.value = true
        this.success.value = false
        this.chatMessages.value = []
        http.get('/room/info/' + this.code).then(res => {
            Object.assign(this.game, res)
            this.updateBoard()
            this.loading.value = false
            this.success.value = true
            if (!this.isEnd()) {
                this.reconnect()
                this.timerStart()
            }
        }).catch(err => router.replace({ path: '/404', query: { code: this.code } }))
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
        console.log('[B 剩余时间]', this.game.black.remainder, '[W 剩余时间]', this.game.white.remainder)

        // 执行提子更新棋盘
        this.updateBoard()
    }

    /**
     * 从落子记录重放当前棋盘（含提子），返回存活棋子和双方各自提子数
     *
     * @returns { { board: Array<{type: string, x: number, y: number}>, whiteCaptured: number, blackCaptured: number } }
     */
    buildBoard() {
        const board = []
        let whiteCaptured = 0
        let blackCaptured = 0
        this.game.steps.forEach((step, i) => {
            const color = i % 2 === 0 ? 'black' : 'white'
            board.push({ type: color, x: step.x, y: step.y })
            const lastCaptured = this.captureAt(board, step.x, step.y, color)
            if (lastCaptured > 0) {
                // 提掉对方的子，计入当前落子一方的提子数
                if (color === 'black') blackCaptured += lastCaptured
                else whiteCaptured += lastCaptured
            }
        })
        return { board, whiteCaptured, blackCaptured }
    }

    updateBoard() {
        const { board, whiteCaptured, blackCaptured } = this.buildBoard()
        this.board.value = board
        this.game.white.captured = whiteCaptured
        this.game.black.captured = blackCaptured
        console.log('提子数 - 白:', whiteCaptured, '黑:', blackCaptured)
    }

    /**
     * 计时器
     */
    timerStart() {
        if (this.isEnd()) {
            return;
        }
        if (this.game.info.type === 'NONE') {
            this.game.black.remainder = -1
            this.game.white.remainder = -1
            return;
        }
        const size = this.stepLength()
        if (size <= 1) {
            this.game.black.remainder = 0
            this.game.white.remainder = 0
            return
        }
        requestAnimationFrame(() => this.timerStart())
        const player = size % 2 === 0 ? 'black' : 'white'
        if (!this.lastTime) {
            this.lastTime = this.game.steps[size - 1].timestamp
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
        const socket = new WebSocket(`${ window.location.protocol === 'https:' ? 'wss:' : 'ws:' }//${ window.location.host }/api/ws/${ this.code }`)
        const that = this
        socket.onopen = function (event) {
            console.log('[socket open]', event)
            socket.onmessage = function (event) {
                const pck = JSON.parse(event.data)
                console.log('[socket msg]', pck)
                switch (pck.type) {
                    case 'ROOM_STEP':
                        that.updateStep(pck.data)
                        break
                    case 'ROOM_CHAT':
                        that.chatMessages.value.push(pck.data)
                        break
                    case 'ROOM_CHAT_MESSAGE':
                        that.chatMessages.value = pck.data
                        break
                    case 'GAME_END':
                        that.refresh()
                        break
                }
                if (that.readMsgHandle) {
                    that.readMsgHandle(pck)
                }
            }

            that.sendGetChatMessages()
        }
        socket.onclose = function (event) {
            console.log('[socket close]', event)
        }
        this.socket = socket
    }

    /**
     * 是否连接成功
     * @returns {boolean}
     */
    isOpen() {
        return this.success && this.socket && this.socket.readyState === WebSocket.OPEN
    }

    /**
     * 游戏是否已经结束
     * @returns {boolean}
     */
    isEnd() {
        return !!this.game.info.winner
    }

    /**
     * 游戏目前步骤数
     */
    stepLength() {
        return this.game.steps.length
    }

    /**
     * 走棋
     * @param type black 黑棋 white 白棋
     * @param x 横坐标
     * @param y 纵坐标
     */
    sendAddStep(type, x, y) {
        if (this.currentColor() !== type) {
            return;
        }
        // 判气/提子：先判断该位置是否可以落子（在棋盘内、为空、且非"自杀"），通过后再发送
        if (!this.canPlace(x, y)) {
            console.warn('非法落子:', x, y)
            return
        }
        if (this.isOpen() && !this.game.steps.find(step => step.x === x && step.y === y)) {
            this.socket.send(JSON.stringify({
                type: 'ROOM_STEP',
                data: {
                    x,
                    y
                }
            }))
        }
    }


    /**
     * 取消对局
     */
    sendCancel() {
        if (this.isOpen() && this.stepLength() < 2) {
            this.socket.send(JSON.stringify({
                type: 'GAME_SURRENDER',
                data: 'CANCEL'
            }))
        }
    }

    /**
     * 发起求和
     */
    sendPeace() {
        if (this.isOpen() && this.stepLength() > 1) {
            this.socket.send(JSON.stringify({
                type: 'GAME_PEACE'
            }))
        }
    }

    /**
     * 是否同意求和
     * @param {boolean} isAgree 是否同意
     */
    sendReplyPeace(isAgree) {
        if (this.isOpen() && this.stepLength() > 1) {
            this.socket.send(JSON.stringify({
                type: 'GAME_REPLY_PEACE',
                data: isAgree
            }))
        }
    }

    /**
     * 发送认输投降
     */
    sendSurrender() {
        if (this.isOpen() && this.stepLength() > 1) {
            this.socket.send(JSON.stringify({
                type: 'GAME_SURRENDER',
                data: 'SURRENDER'
            }))
        }
    }

    /**
     * 发送聊天消息
     * @param {String} message 消息文本
     */
    sendChat(message) {
        if (message && this.isOpen()) {
            this.socket.send(JSON.stringify({
                type: 'ROOM_CHAT',
                data: message
            }))
        }
    }

    /**
     * 获取聊天消息
     */
    sendGetChatMessages() {
        if (this.isOpen()) {
            this.socket.send(JSON.stringify({
                type: 'ROOM_CHAT_MESSAGE',
            }))
        }
    }

    /**
     * 棋盘边长
     */
    boardSize() {
        return this.game.info.boardSize || 19
    }

    /**
     * 当前落子一方
     */
    currentColor() {
        return this.stepLength() % 2 === 0 ? 'black' : 'white'
    }

    /**
     * 判断坐标是否在棋盘内
     */
    inBoard(x, y) {
        const n = this.boardSize()
        return x >= 0 && y >= 0 && x < n && y < n
    }

    /**
     * 获取某坐标的棋子颜色
     * @param board 棋子数组
     * @param x 横坐标
     * @param y 纵坐标
     * @returns {string|null} 'black' | 'white' | null
     */
    stoneAt(board, x, y) {
        const stone = board.find(s => s.x === x && s.y === y)
        return stone ? stone.type : null
    }

    /**
     * 判气：计算 (x, y) 处棋子的气（与棋子相连、紧邻的空位数量）
     * @param board 棋盘二维数组
     * @param x 横坐标
     * @param y 纵坐标
     * @returns {number} 气数
     */
    countLiberties(board, x, y) {
        const color = this.stoneAt(board, x, y)
        if (!color) return 0
        const groupVisited = new Set()
        const liberties = new Set()
        const queue = [[x, y]]
        groupVisited.add(x + ',' + y)
        const dirs = [[1, 0], [-1, 0], [0, 1], [0, -1]]
        while (queue.length) {
            const [cx, cy] = queue.shift()
            for (const [dx, dy] of dirs) {
                const nx = cx + dx
                const ny = cy + dy
                if (!this.inBoard(nx, ny)) continue
                const cell = this.stoneAt(board, nx, ny)
                if (!cell) {
                    liberties.add(nx + ',' + ny)
                } else if (cell === color && !groupVisited.has(nx + ',' + ny)) {
                    groupVisited.add(nx + ',' + ny)
                    queue.push([nx, ny])
                }
            }
        }
        return liberties.size
    }

    /**
     * 提子：移除落子后四周无气的对方棋子
     * @param board 棋盘二维数组
     * @param x 横坐标
     * @param y 纵坐标
     * @param color 落子颜色
     * @returns {number} 被提掉的子数
     */
    captureAt(board, x, y, color) {
        const opp = color === 'black' ? 'white' : 'black'
        const dirs = [[1, 0], [-1, 0], [0, 1], [0, -1]]
        let captured = 0
        for (const [dx, dy] of dirs) {
            const nx = x + dx
            const ny = y + dy
            if (this.inBoard(nx, ny) && this.stoneAt(board, nx, ny) === opp && this.countLiberties(board, nx, ny) === 0) {
                captured += this.removeGroup(board, nx, ny, opp)
            }
        }
        return captured
    }

    /**
     * 移除一整块棋子（提子内部使用）
     */
    removeGroup(board, x, y, color) {
        const group = new Set()
        const queue = [[x, y]]
        group.add(x + ',' + y)
        const dirs = [[1, 0], [-1, 0], [0, 1], [0, -1]]
        while (queue.length) {
            const [cx, cy] = queue.shift()
            for (const [dx, dy] of dirs) {
                const nx = cx + dx
                const ny = cy + dy
                if (!this.inBoard(nx, ny)) continue
                if (this.stoneAt(board, nx, ny) === color && !group.has(nx + ',' + ny)) {
                    group.add(nx + ',' + ny)
                    queue.push([nx, ny])
                }
            }
        }
        let count = 0
        group.forEach(key => {
            const [sx, sy] = key.split(',').map(Number)
            const idx = board.findIndex(s => s.x === sx && s.y === sy)
            if (idx !== -1) {
                board.splice(idx, 1)
                count++
            }
        })
        return count
    }

    /**
     * 是否可以落子：位置在棋盘内、为空，且不是"自杀"落子（提掉对方后己方仍有气）
     */
    canPlace(x, y) {
        if (!this.inBoard(x, y)) return false
        const color = this.currentColor()
        // 直接从落子记录重放当前棋盘，避免 this.board.value 未同步导致误判
        const { board } = this.buildBoard()
        console.log('[canPlace]', x, y, '落子方=', color, '总步数=', this.stepLength(), '存活子=', board.length, '该点已有子=', this.stoneAt(board, x, y))
        if (this.stoneAt(board, x, y)) return false
        board.push({ type: color, x, y })
        const captured = this.captureAt(board, x, y, color)
        const libs = this.countLiberties(board, x, y)
        console.log('[canPlace] 提子=', captured, '落子后己方气=', libs, '合法=', libs > 0)
        return libs > 0
    }

}

