import { reactive } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http.js'
import dayjs from 'dayjs'
import Cookie from 'js-cookie'
import router from '@/router/index.js'

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
    const userIsGuest = Cookie.get('userIsGuest')
    user.isGuest = userIsGuest !== 'false'
    if (!user.isGuest && !user.timestamp) {
      http.post("/user/info").then(res => {
        Object.assign(user, res)
        user.timestamp = dayjs().valueOf()
      })
    }
  }

  const login = () => {
    user.isGuest = false
    Cookie.set('userIsGuest', user.isGuest, { expires: 999 })
    refreshInfo()
    router.push('/')
  }

  const logout = () => {
    Cookie.remove('userIsGuest')
    user.isGuest = true
    http.post('/auth/signout').then(() => {
      if (router.currentRoute.value.path !== '/') {
        router.push('/')
      }
    })
  }

  return { user, refreshInfo, login, logout }
})
