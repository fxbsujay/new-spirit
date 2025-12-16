import { defineStore } from 'pinia'
import router from '@/router/index.js'
import { useUserStore } from '@/stores/user.js'
import { ref } from 'vue'

export const useSocketStore = defineStore('counter', () => {

  let socket = null
  const open = ref(false)
  const userStore = useUserStore()

  const reconnect = () => {
    if (socket) {
      socket.close()
    }

    open.value = false
    socket = new WebSocket(`${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/api/ws`)
    socket.onopen = function (event) {
      open.value = true
      socket.onmessage = function (event) {
        const msg = JSON.parse(event.data)
        switch (msg.type) {
          case 'GAME_START':
            router.push('/' + msg.data)
            userStore.closeWaitGame()
            break
        }
      }
    }
    socket.onclose = function(event) {
      open.value = false
      console.log('----', event)
    }
  }



  return { socket, open, reconnect }
})

