import { defineStore } from 'pinia'
import router from '@/router/index.js'
import { useUserStore } from '@/stores/user.js'
export const useSocketStore = defineStore('counter', () => {

  let socket = null
  const userStore = useUserStore()

  const reconnect = () => {
    if (socket) {
      socket.close()
    }
    socket = new WebSocket(`${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/api/ws`)
    socket.onopen = function (event) {
      console.log('WebSocket 连接成功', event)
    }

    socket.onclose = function(event) {
      if (event.wasClean) {
        console.log(`连接已正常关闭，代码=${event.code}，原因=${event.reason}`)
      } else {
        console.log('连接中断')
      }
    }
    socket.onmessage = function (event) {
      const msg = JSON.parse(event.data)
      console.log('socket message', msg)
      switch (msg.type) {
        case 'GAME_START':
          router.push('/' + msg.data)
          userStore.closeWaitGame()
          break
      }
    }
  }

  reconnect()
  const isConnected = () => {
    return socket.readyState === WebSocket.OPEN
  }

  const send = (type, data) => {
    const pck = {
      sender: userStore.user.username,
      type,
      data
    }
    socket.send(JSON.stringify(pck))
  }

  return { socket, reconnect, isConnected, send }
})

