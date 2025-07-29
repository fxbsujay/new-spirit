<script setup>
import { reactive, ref } from 'vue'
import http from '@/utils/http.js'
import { debounce, passwordStrength } from '@/utils/index.js'
import { useRouter } from 'vue-router'

const formState = reactive({
  username: '',
  password: '',
  email: '',
  code: ''
})

const stage = ref(true)
const loading = ref(false)
const interval = ref()
const outTime = ref(0)
const strength = ref(0)
const router = useRouter()
const passwordReveal = ref(false)

const submitHandle = debounce(() => {
  if (stage.value) {
    sendCodeHandler()
  } else {
    loading.value = true
    http.post("/auth/signup", formState).then(() => {
      router.push({ path: '/sign-up/success', params: { username: formState.username } })
    }).catch(() => loading.value = false)
  }
})

const sendCodeHandler = () => {
  loading.value = true

  http.post("/auth/signup/code", formState).then(() => {
    stage.value = false
    loading.value = false
    outTime.value  = 60
    interval.value = setInterval(() => {
      outTime.value--
      if (outTime.value <= 0) {
        clearInterval(interval.value)
        interval.value = null
      }
    }, 1000)
  }).catch(() => {
    outTime.value = 0
    loading.value = false
  })
}

const resendCodeHandler = () => {
  if (outTime.value || loading.value) {
    return
  }
  sendCodeHandler()
}

const passwordInputHandler = (event) => {
  strength.value = passwordStrength(event.target.value)
}

</script>
<template>
  <div class="content-box">
    <div class="card form-wrap">
      <h2 class="title">注册</h2>
      <form class="form" @submit.prevent="submitHandle">
        <div v-if="stage">
          <div class="form-group">
            <div class="border-input-wrap">
              <label class="label" >
                用户名
              </label>
              <input
                  class="input"
                  required
                  pattern="[a-zA-Z0-9]{2,20}"
                  title="2-20位字母或数字"
                  v-model="formState.username"
                  :disabled="loading"
              />
              <p class="form-help">请务必选择一个和谐的用户名，用户名设置后无法更改，并且不合规的用户名会导致账户被封禁！</p>
            </div>
          </div>
          <div class="form-group">
            <div class="border-input-wrap">
              <label class="label" >密码</label>
              <div class="password-reveal">
                <input
                    class="input"
                    required
                    pattern="[a-zA-Z0-9@!$^.*_%]{6,30}"
                    title="6-30位字母，数字或以下@!$^.*_%合法符号"
                    v-model="formState.password"
                    :disabled="loading"
                    :type="passwordReveal ? 'input' : 'password'"
                    @input="passwordInputHandler"
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
          <div class="form-group password-complexity">
            <label class="form-help">密码强度</label>
            <div class="password-complexity-meter">
              <span :class="strength > 0 ? 'action' : ''"></span>
              <span :class="strength > 1 ? 'action' : ''"></span>
              <span :class="strength > 2 ? 'action' : ''"></span>
              <span :class="strength > 3 ? 'action' : ''"></span>
            </div>
          </div>
          <div class="form-group">
            <div class="border-input-wrap">
              <label class="label">电子邮箱</label>
              <input class="input" :disabled="loading" type="email" v-model="formState.email" required />
              <p class="form-help">仅用于重置密码</p>
            </div>
          </div>
        </div>
        <div class="form-group" v-else>
          <div class="border-input-wrap">
            <label class="label" >验证码</label>
            <input class="input" :disabled="loading" v-model="formState.code" required pattern="[A-Z0-9]{5}"  title="5位字母或数字"/>
            <p class="form-help">
              验证码已发送到您的邮箱
              <a style="float: right" class="form-help" :class="!outTime ? 'code-help' : ''" @click="resendCodeHandler">
                {{ outTime ? `${outTime} 秒后可重新发送` : '重新发送' }}
              </a>
            </p>
          </div>
        </div>
        <button type="submit" :disabled="loading" class="primary button">提交</button>
      </form>
    </div>
  </div>
</template>

<style lang="less" scoped>
@import './index.less';
</style>

