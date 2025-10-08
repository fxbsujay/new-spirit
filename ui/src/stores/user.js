import { reactive } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http.js'
import dayjs from "dayjs";

export const useUserStore = defineStore('user', () => {

  const user = reactive({
    avatar: null,
    email: null,
    isGuest: null,
    nickname: null,
    username: null,
    timestamp: 0
  })

  const refreshInfo = () => {
    if (!user.timestamp) {
      // WWNF8X 22222222
      http.post("/auth/info").then(res => {
        Object.assign(user, res)
        user.timestamp = dayjs().valueOf()
        console.log(user)
      })
    }
  }
  return { user, refreshInfo }
})
