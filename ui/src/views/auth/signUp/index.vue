<script setup>
import http from '@/utils/http.js'
import { debounce, passwordStrength } from '@/utils/index.js'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const rules = {
    username: [
        value => !value || !/^[A-Za-z][a-zA-Z0-9]{1,19}/.test(value) ? '请输入以英文字母开头，2-20位字母或数字': true,
    ],
    password: [
        value => !value || !/^[a-zA-Z0-9@!$^.*_%]{6,30}$/.test(value) ? '请输入6-30位字母，数字或以下@!$^.*_%合法符号': true,
    ],
    email: [
        value => !value ? '请输入电子邮箱': true
    ]
}

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
    if (loading.value) {
        return
    }
    if (stage.value) {
        sendCodeHandler()
    } else {
        loading.value = true
        http.post('/auth/signup', formState).then(() => {
            router.push({ path: '/sign-up/success', params: { username: formState.username } })
        }).catch(() => loading.value = false)
    }
})

const sendCodeHandler = () => {
    loading.value = true

    http.post('/auth/signup/code', formState).then(() => {
        stage.value = false
        loading.value = false
        outTime.value = 60
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
      <v-form class="form" validate-on="blur" @submit.prevent="submitHandle">
        <div v-if="stage">
          <v-text-field
              :readonly="loading"
              density="comfortable"
              v-model="formState.username"
              :rules="rules.username"
              label="用户名"
              variant="outlined"
              class="mb-2"
          />
          <v-text-field
              @click:append-inner="passwordReveal = !passwordReveal"
              :append-inner-icon="passwordReveal ? 'custom:eye' : 'custom:eye-off'"
              :readonly="loading"
              :type="passwordReveal ? 'text' : 'password'"
              density="comfortable"
              v-model="formState.password"
              :rules="rules.password"
              variant="outlined"
              label="密码"
              class="mb-2"
          />
          <div class="form-group password-complexity">
            <label class="form-help">密码强度</label>
            <div class="password-complexity-meter">
              <span :class="strength > 0 ? 'action' : ''"></span>
              <span :class="strength > 1 ? 'action' : ''"></span>
              <span :class="strength > 2 ? 'action' : ''"></span>
              <span :class="strength > 3 ? 'action' : ''"></span>
            </div>
          </div>
          <v-text-field
              :readonly="loading"
              density="comfortable"
              v-model="formState.username"
              :rules="rules.username"
              label="电子邮箱"
              variant="outlined"
              class="mb-2"
          />
          <div class="form-group">
            <div class="border-input-wrap">
              <label class="label">电子邮箱</label>
              <input class="input" :disabled="loading" type="email" v-model="formState.email" required/>
              <p class="form-help">仅用于重置密码</p>
            </div>
          </div>
        </div>
        <div class="form-group" v-else>
          <div class="border-input-wrap">
            <label class="label">验证码</label>
            <input class="input" :disabled="loading" v-model="formState.code" required pattern="[A-Z0-9]{5}" title="5位字母或数字"/>
            <p class="form-help">
              验证码已发送到您的邮箱
              <a style="float: right" class="form-help" :class="!outTime ? 'code-help' : ''" @click="resendCodeHandler">
                {{ outTime ? `${ outTime } 秒后可重新发送` : '重新发送' }}
              </a>
            </p>
          </div>
        </div>
        <button type="submit" class="black button">提交</button>
      </v-form>
    </div>
  </div>
</template>

<style lang="less" scoped>
@import './index.less';
</style>

