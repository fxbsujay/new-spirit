import { defineStore } from 'pinia'

export const useSocketStore = defineStore('counter', () => {

  const socket = new WebSocket(`${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/api/ws`)

  socket.onopen = function (event) {
    console.log('WebSocket 连接成功', event);
  }
  
  socket.onclose = function(event) {
    if (event.wasClean) {
      console.log(`连接已正常关闭，代码=${event.code}，原因=${event.reason}`);
    } else {
      console.log('连接中断');
    }
  }

  socket.onmessage = function (event) {
    console.log(event)
  }

  const isConnected = () => {
    return socket.readyState === WebSocket.OPEN
  }
  return { socket, isConnected }
})

