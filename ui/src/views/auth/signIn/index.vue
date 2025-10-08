<script setup>
import { reactive, ref } from 'vue'
import http from '@/utils/http.js'
import {useUserStore} from '@/stores/user.js'
import router from '@/router/index.js'

const formState = reactive({
  username: '',
  password: ''
})
const passwordReveal = ref(false)
const userStore = useUserStore()

const submitHandle = () => {
  http.post("/auth/signin", formState).then(() => {
    userStore.refreshInfo()
    router.push('/')
  }).catch(err => {
    console.log(err)
  })
}
</script>

<template>
  <div class="content-box">
    <div class="card form-wrap">
      <h2 class="title">登录</h2>
      <form class="form" @submit.prevent="submitHandle">
        <div class="form-group">
          <div class="border-input-wrap">
            <label class="label" >
              用户名 或 邮箱
            </label>
            <input
                class="input"
                required
                pattern="[a-zA-Z0-9@.]{2,20}"
                title="2-20位字母或数字"
                v-model="formState.username"
            />
          </div>
        </div>
        <div class="form-group">
          <div class="border-input-wrap">
            <label class="label" >
              密码
            </label>
            <div class="password-reveal">
              <input
                  class="input"
                  required
                  pattern="[a-zA-Z0-9@!$^.*_%]{6,30}"
                  title="6-30位字，数字或以下@!$^.*_%合法符号"
                  v-model="formState.password"
                  :type="passwordReveal ? 'input' : 'password'"
              />
              <Icon
                  class="reveal-icon"
                  size="1rem"
                  :name="passwordReveal ? 'eye-outline' : 'eye-off-outline'"
                  @click="passwordReveal = !passwordReveal"
              />
            </div>
          </div>
        </div>
        <button type="submit" class="primary button">登录</button>
        <div class="alternative">
          <RouterLink to="">重置密码</RouterLink>
          <RouterLink to="">邮箱登录</RouterLink>
        </div>
      </form>
    </div>
    <div class="card signup-wrap">
      <div class="title">没有 Spirit 账户?</div>
      <RouterLink to="/sign-up">
        <button class="border button">创建账户</button>
      </RouterLink>
      <ul class="links">
        <li><span>© Spirit</span></li>
        <li><RouterLink to="">使用条款</RouterLink></li>
        <li><RouterLink to="">隐私协议</RouterLink></li>
      </ul>
    </div>
  </div>
</template>
<style lang="less" scoped>
@import './index.less';
</style>

