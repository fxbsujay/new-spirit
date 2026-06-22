<script setup lang="ts">
import { reactive, ref } from 'vue'
import http from '@/utils/http.js'
import snackbar from '@/components/snackbar/index.js'
import { passwordStrength } from '@/utils/index.js'

const formState = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const strength = ref(0)
const oldPasswordReveal = ref(false)
const newPasswordReveal = ref(false)
const confirmPasswordReveal = ref(false)
const loading = ref(false)
const success = ref(false)
const submitHandle = () => {
  if (loading.value) {
    return
  }
  if (success.value) {
    Object.assign(formState, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    success.value = false
    return
  }
  if (formState.newPassword === formState.oldPassword) {
    snackbar.warning('新密码不能与旧密码相同')
    return
  }

  if (formState.newPassword !== formState.confirmPassword) {
    snackbar.warning('两次输入的新密码不同')
    return
  }
  loading.value = true
  http.post('/account/password', formState).then(() => {
    loading.value = false
    success.value = true
  }).catch(() => loading.value = false)
}

const passwordInputHandler = (event) => {
  strength.value = passwordStrength(event.target.value)
}

</script>

<template>
  <form class="form" @submit.prevent="submitHandle">
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
              v-model="formState.oldPassword"
              :disabled="loading || success"
              :type="oldPasswordReveal ? 'input' : 'password'"
          />
          <Icon
              class="reveal-icon"
              size="1rem"
              :name="oldPasswordReveal ? 'eye-outline' : 'eye-off-outline'"
              @click="oldPasswordReveal = !oldPasswordReveal"
          />
        </div>
      </div>
    </div>
    <div class="form-group">
      <div class="border-input-wrap">
        <label class="label">新密码</label>
        <div class="password-reveal">
          <input
              class="input"
              required
              pattern="[a-zA-Z0-9@!$^.*_%]{6,30}"
              title="6-30位字母，数字或以下@!$^.*_%合法符号"
              v-model="formState.newPassword"
              :disabled="loading || success"
              :type="newPasswordReveal ? 'input' : 'password'"
              @input="passwordInputHandler"
          />
          <Icon
              class="reveal-icon"
              size="1rem"
              :name="newPasswordReveal ? 'eye-outline' : 'eye-off-outline'"
              @click="newPasswordReveal = !newPasswordReveal"
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
        <label class="label">新密码（再次输入）</label>
        <div class="password-reveal">
          <input
              class="input"
              required
              pattern="[a-zA-Z0-9@!$^.*_%]{6,30}"
              title="6-30位字母，数字或以下@!$^.*_%合法符号"
              v-model="formState.confirmPassword"
              :disabled="loading || success"
              :type="confirmPasswordReveal ? 'input' : 'password'"
          />
          <Icon
              class="reveal-icon"
              size="1rem"
              :name="confirmPasswordReveal ? 'eye-outline' : 'eye-off-outline'"
              @click="confirmPasswordReveal = !confirmPasswordReveal"
          />
        </div>
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

.password-complexity {
  margin-top: -1rem;
  margin-bottom: 2rem;

  .form-help {
    font-size: 12px;
    color: @c-grey;
  }
}

.password-complexity-meter {
  display: flex;
  grid-gap: .25rem;
  height: .4rem;
  margin-top: 1rem;

  & > span {
    background-color: #a4a4a4;
    width: 25%;

    &.action {
      background-color: @c-positive;
    }
  }
}

</style>
