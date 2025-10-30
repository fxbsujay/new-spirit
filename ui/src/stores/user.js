import { reactive } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http.js'
import dayjs from "dayjs";

export const useUserStore = defineStore('user', () => {

  const user = reactive({
    avatar: null,
    email: null,
    isGuest: true,
    nickname: null,
    username: null,
    timestamp: 0
  })

  const refreshInfo = () => {
    http.post("/user/info").then(res => {
      Object.assign(user, res)
      user.timestamp = dayjs().valueOf()
    })
  }

  return { user, refreshInfo }
})
