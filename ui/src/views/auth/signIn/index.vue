<script setup>
import { useUserStore } from '@/stores/user.js'
import http from '@/utils/http.js'
import { reactive, ref } from 'vue'

const rules = {
    username: [
        value => !value || !/^[A-Za-z][A-Za-z0-9@!._+-]{1,19}$/.test(value) ? '请输入2-20位字母开头的字母数字，或有效的邮箱地址': true,
    ],
    password: [
        value => !value || !/^[a-zA-Z0-9@!$^.*_%]{6,30}$/.test(value) ? '请输入6-30位字母，数字或以下@!$^.*_%合法符号': true,
    ]
}

const formState = reactive({
    username: '',
    password: ''
})

const loading = ref(false)
const passwordReveal = ref(false)
const userStore = useUserStore()

const submitHandle = async (event) => {

    loading.value = true
    const { valid } = await event
    if (valid) {
        http.post('/auth/signin', formState).then(() => {
            userStore.login()
            loading.value = false
        }).catch(err => {
            console.log(err)
            loading.value = false
        })
    } else {
        loading.value = false
    }
}
</script>

<template>
  <div class="content-box">
    <div class="card form-wrap">
      <h2 class="title darken-4">登录</h2>
      <v-form class="form" validate-on="blur" @submit.prevent="submitHandle">
        <label class="text-label-large ">用户名 或 邮箱</label>
        <v-text-field
            :readonly="loading"
            density="comfortable"
            v-model="formState.username"
            :rules="rules.username"
            variant="outlined"
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
            variant="outlined"
            class="mb-2 mt-2"
        />
        <v-btn :loading="loading" class="mt-4 mb-2" type="submit" block color="black" size="large">
          登录
        </v-btn>
        <div class="alternative red-accent-4">
          <RouterLink to="">忘记密码？</RouterLink>
          <RouterLink to="">邮箱登录</RouterLink>
        </div>
      </v-form>
    </div>
    <div class="card signup-wrap">
      <div class="title">没有 Spirit 账户?</div>
      <RouterLink to="/sign-up">
        <v-btn
            class="w-100"
            color="grey-darken-4"
            variant="outlined"
            size="large"
        >
          创建账户
        </v-btn>
      </RouterLink>
      <ul class="links">
        <li> <RouterLink to="/">© Spirit</RouterLink></li>
        <li><RouterLink to="/">使用条款</RouterLink></li>
        <li><RouterLink to="/">隐私协议</RouterLink></li>
      </ul>
    </div>
  </div>
</template>
<style lang="sass" scoped>
@use 'index.sass'
</style>

