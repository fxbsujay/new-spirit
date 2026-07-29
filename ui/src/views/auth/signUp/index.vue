<script setup>
import http from '@/utils/http.js'
import { passwordStrength } from '@/utils/index.js'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import Icon from "@/components/icon/Icon.vue";

const rules = {
    username: {
        value: [
            value => !value || !/^[A-Za-z][a-zA-Z0-9]{1,19}/.test(value) ? '请输入以英文字母开头，2-20位字母或数字': true,
        ],
        message: ''
    },
    password: [
        value => !value || !/^[a-zA-Z0-9@!$^.*_%]{6,30}$/.test(value) ? '请输入6-30位字母，数字或以下@!$^.*_%合法符号': true,
    ],
    email: {
        value: [
            value => !value ? '请输入电子邮箱': true
        ],
        message: ''
    }
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
const isCodeError = ref(false)
const passwordReveal = ref(false)

const submitHandle = async (event) => {
    const { valid } = await event
    if (valid) {
        if (stage.value) {
            sendCodeHandler()
        } else {
            loading.value = true
            http.post('/auth/signup', formState).then(() => {
                router.push({ path: '/sign-up/success', params: { username: formState.username } })
            }).catch(err => {
              loading.value = false
              if (err.code === '10007') {
                isCodeError.value = true
              }
            })
        }
    }
}

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
    }).catch(err => {
        outTime.value = 0
        loading.value = false
        if (err.code === '10001') {
            rules.email.message = err.message
        } else if (err.code === '10002') {
            rules.username.message = err.message
        }
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
      <v-form class="form" validate-on="blur" @submit.prevent="submitHandle">

        <div v-if="stage">
          <h2 class="title">注册</h2>

          <label class="text-label-large">用户名</label>
          <v-text-field
              :readonly="loading"
              density="comfortable"
              v-model="formState.username"
              :rules="rules.username.value"
              :error-messages="rules.username.message"
              variant="outlined"
              @input="v => rules.username.message = ''"
              class="mt-2"
          />
          <label class="text-label-large">密码</label>
          <v-text-field
              @click:append-inner="passwordReveal = !passwordReveal"
              :append-inner-icon="passwordReveal ? 'custom:eye' : 'custom:eye-off'"
              :readonly="loading"
              :type="passwordReveal ? 'text' : 'password'"
              density="comfortable"
              v-model="formState.password"
              :rules="rules.password"
              @input="passwordInputHandler"
              variant="outlined"
              class="mt-2"
          />
          <label class="text-label-large">密码强度</label>
          <div class="password-complexity-meter mt-3 mb-6">
            <span :class="strength > 0 ? 'action' : ''"></span>
            <span :class="strength > 1 ? 'action' : ''"></span>
            <span :class="strength > 2 ? 'action' : ''"></span>
            <span :class="strength > 3 ? 'action' : ''"></span>
          </div>
          <label class="text-label-large">电子邮箱</label>
          <v-text-field
              :readonly="loading"
              density="comfortable"
              v-model="formState.email"
              :rules="rules.email.value"
              :error-messages="rules.email.message"
              @input="v => rules.email.message = ''"
              variant="outlined"
              class="mt-2 mb-2"
          />
        </div>
        <div v-else>
          <v-alert
              v-if="isCodeError"
              class="mb-6"
              variant="tonal"
              density="compact"
              text="验证码错误!"
              type="warning" >
            <template #prepend>
              <v-icon  icon="custom:eye"/>
            </template>
          </v-alert>
          <h3 class="text-title-large mt-0 mb-2">邮箱验证</h3>
          <div class="text-body-medium font-weight-light">
            发送验证码到邮箱 <span class="font-weight-black text-primary">{{ formState.email }}</span>
          </div>

          <v-otp-input
              v-model="formState.code"
              length="5"
              :loading="loading"
              :pattern="/[A-Z0-9]/"
              class="mt-3 ms-n2"
              variant="underlined"
              @input="v => isCodeError = false"
          ></v-otp-input>

          <div class="mb-8 text-body-medium font-weight-light d-flex align-center justify-space-between">
            <span >没有收到 <strong>验证码</strong>?</span>
            <v-btn :loading="loading" color="blue-darken-4" size="small" variant="text" @click="resendCodeHandler">
              {{ outTime ? `${ outTime } 秒后可重新发送` : '重新发送' }}
            </v-btn>
          </div>

        </div>
        <v-btn :disabled="(!stage && formState.code < 5) || isCodeError" :loading="loading" class="mt-4 mb-2" type="submit" block color="black" size="large">
          {{ stage ? '提交' : '验证' }}
        </v-btn>
      </v-form>
    </div>
  </div>
</template>

<style lang="sass" scoped>
@use 'index.sass'
</style>

