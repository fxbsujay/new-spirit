<script setup>
import { useUserStore } from '@/stores/user.js'
import http from '@/utils/http.js'
import { reactive, ref } from 'vue'

const formState = reactive({
    username: '',
    password: ''
})
const loading = ref(false)
const passwordReveal = ref(false)
const userStore = useUserStore()

const submitHandle = () => {
    loading.value = true
    http.post('/auth/signin', formState).then(() => {
        userStore.login()
        loading.value = false
    }).catch(err => {
        console.log(err)
        loading.value = false
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
            <label class="label">
              用户名 或 邮箱
            </label>
            <input
                class="input"
                required
                pattern="^[a-zA-Z0-9@!._-+]{2,20}$"
                title="请输入2-20位字母开头的字母数字，或有效的邮箱地址"
                v-model="formState.username"
            />
          </div>
        </div>
        <div class="form-group">
          <div class="border-input-wrap">
            <label class="label">
              密码
            </label>
            <div class="password-reveal">
              <input
                  class="input"
                  required
                  pattern="^[a-zA-Z0-9@!$^.*_%]{6,30}$"
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
        <button type="submit" class="black button">登录</button>
        <q-btn style="width: 100%;background-color: #312D2A" padding="10px" :loading="loading" size="1rem" color="with" label="登录" type="submit">
          <template #loading>
            <q-spinner-facebook/>
          </template>
        </q-btn>
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
        <li>
          <RouterLink to="">使用条款</RouterLink>
        </li>
        <li>
          <RouterLink to="">隐私协议</RouterLink>
        </li>
      </ul>
    </div>
  </div>
</template>
<style lang="less" scoped>
@import './index.less';
</style>

