<script setup lang="ts">
import { reactive, ref, useTemplateRef } from 'vue'
import http from '@/utils/http.js'
import { useUserStore } from '@/stores/user'
import snackbar from '@/components/snackbar/index.js'

const store = useUserStore()
const formState = reactive({
    password: '',
    email: '',
    code: ''
})
formState.email = store.user.email
const formEmail = useTemplateRef('form-email')
const passwordReveal = ref(false)
const loading = ref(false)
const success = ref(false)
const sender = ref(0)

const submitHandle = () => {
    if (loading.value) {
        return
    }
    if (success.value) {
        Object.assign(formState, {
            password: '',
            email: '',
            code: ''
        })
        success.value = false
        return
    }
    loading.value = true
    http.post('/account/email', formState).then(() => {
        loading.value = false
        success.value = true
        sender.value = 0
    }).catch(() => loading.value = false)
}

const sendCode = () => {
    if (loading.value) {
        return
    }
    if (!formEmail.value.reportValidity()) {
        return
    }
    if (formState.email === store.user.email) {
        snackbar.warning('邮箱地址不能与原邮箱地址相同')
        return
    }

    loading.value = true
    http.post('/account/send/code', { email: formState.email }).then(() => {
        snackbar.success('发送成功')
        loading.value = false
        sender.value = 60
        const timer = setInterval(() => {
            sender.value--
            if (sender.value <= 0) {
                clearInterval(timer)
            }
        }, 1000)
    }).catch(() => loading.value = false)
}

</script>

<template>
  <form class="form" @submit.prevent="submitHandle" autocomplete="off">
    <div class="success-tip" v-if="success">
      <Icon name="check-bold" color="#fff" size="2rem"/>
      <span>操作成功</span>
    </div>
    <div class="form-group">
      <div class="border-input-wrap">
        <label class="label">密码</label>
        <div class="password-reveal">
          <input
              class="input"
              required
              pattern="[a-zA-Z0-9@!$^.*_%]{6,30}"
              title="6-30位字母，数字或以下@!$^.*_%合法符号"
              v-model="formState.password"
              :disabled="loading || success"
              :type="passwordReveal ? 'input' : 'password'"
              autocomplete="off"
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
    <div class="form-group">
      <div class="border-input-wrap">
        <label class="label">邮箱</label>
        <div class="row">
          <input
              ref="form-email"
              class="input col"
              :disabled="success || loading"
              required
              pattern="^[a-zA-Z0-9@!._-+]{2,20}$"
              title="请输入2-20位字母开头的字母数字，或有效的邮箱地址"
              v-model="formState.email"
          />
          <button type="button" :disabled="sender !== 0 || loading || success" class="button border send-code-btn" @click="sendCode">
            {{ sender !== 0 ? `${sender}秒后重新发送` : '发送验证码' }}
          </button>
        </div>
      </div>
    </div>
    <div class="form-group">
      <div class="border-input-wrap">
        <label class="label">验证码</label>
        <input
            class="input"
            :disabled="success || loading"
            required
            pattern="[A-Z0-9]{5}"
            title="5位字母或数字"
            v-model="formState.code"
        />
      </div>
    </div>
    <button type="submit" class="submit-button button " :disabled="loading" :class="success ? 'border' : 'black'">
      {{ success ? '再次修改' : '保存' }}
    </button>
  </form>
</template>

<style scoped lang="less">
@import "@/assets/css/variable.less";
@import "./index.less";

.form-group {
  margin-bottom: 2rem;
}

.send-code-btn {
  height: 2.8125rem;
  padding: 0 1rem;
  font-size: 14px;
  margin-left: 1rem;
}
</style>
