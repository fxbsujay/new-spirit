<script setup lang="ts">
import { reactive, ref } from 'vue'
import http from '@/utils/http.js'
import snackbar from '@/components/snackbar/index.js'
import { debounce, passwordStrength } from '@/utils/index.js'

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
const submitHandle = () => {
  snackbar.warning("A")
}

const passwordInputHandler = (event) => {
  strength.value = passwordStrength(event.target.value)
}

</script>

<template>
  <form class="form" @submit.prevent="submitHandle">
    <div class="form-group">
      <div class="border-input-wrap">
        <label class="label" >密码</label>
        <div class="password-reveal">
          <input
              class="input"
              required
              pattern="[a-zA-Z0-9@!$^.*_%]{6,30}"
              title="6-30位字母，数字或以下@!$^.*_%合法符号"
              v-model="formState.oldPassword"
              :disabled="loading"
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
        <label class="label" >新密码</label>
        <div class="password-reveal">
          <input
              class="input"
              required
              pattern="[a-zA-Z0-9@!$^.*_%]{6,30}"
              title="6-30位字母，数字或以下@!$^.*_%合法符号"
              v-model="formState.newPassword"
              :disabled="loading"
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
        <label class="label" >新密码（再次输入）</label>
        <div class="password-reveal">
          <input
              class="input"
              required
              pattern="[a-zA-Z0-9@!$^.*_%]{6,30}"
              title="6-30位字母，数字或以下@!$^.*_%合法符号"
              v-model="formState.confirmPassword"
              :disabled="loading"
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
    <button type="submit" class="submit-button button black">确认</button>
  </form>
</template>

<style scoped lang="less">
@import "@/assets/css/variable.less";

.form-group {
  margin-bottom: 2rem;

  .label {
    font-weight: bolder;
  }
}

.password-complexity {
  margin-top: -1rem;
  margin-bottom: 2rem ;
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

.submit-button {

  margin-top: 2rem;
  height: 36px;
  font-size: 14px;
  max-width: 100%;
  width: 150px;
  float: right;

  &.black {
    background-color: #312d2a;
  }

}

</style>
