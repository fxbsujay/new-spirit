import { reactive, ref } from 'vue'
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

  const waitGame = reactive({
    code: '',
    boardSize: 0,
    type: 'SHORT',
    mode: 'CASUAL',
    duration: 0,
    stepDuration: 0,
    username: '',
    nickname: '',
    score: 0,
    timestamp: 0,
  })

  const refreshInfo = () => {
    const userIsGuest = Cookie.get('userIsGuest')
    user.isGuest = userIsGuest !== 'false'
    if (!user.isGuest && !user.timestamp) {
      http.post("/user/info").then(res => {
        Object.assign(user, res)
        user.timestamp = dayjs().valueOf()
        setIsGuestCookie(false)
      })
    }
  }

  const login = () => {
    user.isGuest = false
    setIsGuestCookie(false)
    refreshInfo()
    router.push('/')
  }

  const logout = () => {
    setIsGuestCookie(true)
    user.isGuest = true
    http.post('/auth/signout').then(() => {
      if (router.currentRoute.value.path !== '/') {
        router.push('/')
      }
    })
  }

  const setIsGuestCookie = value => {
    Cookie.set('userIsGuest', value, { expires: 999 })
  }

  const closeWaitGame = () => {
    Object.assign(waitGame, {
      code: '',
      boardSize: 0,
      type: 'SHORT',
      mode: 'CASUAL',
      duration: 0,
      stepDuration: 0,
      timestamp: 0,
    })
  }

  return { user, waitGame, refreshInfo, login, logout, closeWaitGame }
})
