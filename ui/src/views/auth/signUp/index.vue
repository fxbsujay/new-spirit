<script setup>
import http from '@/utils/http.js'
import { passwordStrength } from '@/utils/index.js'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

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

const stage = ref(false)
const loading = ref(false)
const interval = ref()
const outTime = ref(0)
const strength = ref(0)
const router = useRouter()
const passwordReveal = ref(false)

const submitHandle = async (event) => {
    const { valid } = await event

    if (valid) {
        console.log('----------')
        if (stage.value) {
            sendCodeHandler()
        } else {
            loading.value = true
            http.post('/auth/signup', formState).then(() => {
                router.push({ path: '/sign-up/success', params: { username: formState.username } })
            }).catch(() => loading.value = false)
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
              rounded="0"
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
              rounded="0"
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
              rounded="0"
              variant="outlined"
              class="mt-2 mb-2"
          />
        </div>
        <div class="mb-10" v-else>
          <h3 class="text-title-large mt-0 mb-1">邮箱验证</h3>
          <div class="text-body-medium font-weight-light">
            Enter the code we just sent to your mobile phone <span class="font-weight-black text-primary">+1 408 555 1212</span>
          </div>

          <v-otp-input class="pa-0" v-model="formState.code" rounded="0" :length="5" :pattern="/[A-Z0-9]/"></v-otp-input>
          <div class="text-body-medium ml-2 mr-2">
            <span>验证码已发送到您的邮箱</span>
            <span :class="outTime ? 'float-right' : 'float-right'">{{ outTime ? `${ outTime } 秒后可重新发送` : '重新发送' }}</span>
          </div>
        </div>

        <v-btn :loading="loading" rounded="0" class="mt-4 mb-2" type="submit" block color="black" size="large">
          提交
        </v-btn>
      </v-form>
    </div>
  </div>
</template>

<style lang="sass" scoped>
@use 'index.sass'
</style>

