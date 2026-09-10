<script setup>
import { reactive, ref } from 'vue'
import { Regex } from '@/utils/constant.js'
import http from '@/utils/http.js'
import { passwordStrength } from '@/utils/index.js'

const rules = [ value => !value || !Regex.PASSWORD.test(value) ? '请输入6-30位字母，数字或以下@!$^.*_%合法符号': true ]

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
const alert = reactive({
    show: false,
    success: false,
    message: ''
})
const submitHandle = async (event) => {
    if (loading.value) {
        return
    }
    if (alert.show) {
        Object.assign(alert, {
            show: false,
            success: false,
            message: ''
        })
        return
    }
    loading.value = true
    const { valid } = await event
    if (valid) {
        if (formState.newPassword === formState.oldPassword) {
            loading.value = false
            Object.assign(alert, {
                show: true,
                success: false,
                message: '新密码不能与旧密码相同'
            })
            return
        }

        if (formState.newPassword !== formState.confirmPassword) {
            loading.value = false
            Object.assign(alert, {
                show: true,
                success: false,
                message: '两次输入的新密码不同'
            })
            return
        }
        loading.value = true
        http.post('/account/password', formState).then(() => {
            loading.value = false
            Object.assign(alert, {
                show: true,
                success: true,
                message: '修改成功'
            })
            event.target.reset()
        }).catch(res => {
            loading.value = false
            Object.assign(alert, {
                show: true,
                success: false,
                message: res.message
            })
        })
    } else {
        loading.value = false
    }

}

const passwordInputHandler = (event) => {
    strength.value = passwordStrength(event.target.value)
}

</script>

<template>
  <v-form @submit.prevent="submitHandle">
    <v-alert
        v-model="alert.show"
        class="mb-4"
        :color="alert.success ? 'success' : 'warning'"
        variant="tonal"
        density="compact"
        :text="alert.message"
        closable
        :icon="alert.success ? 'check-bold' : 'warning'"
        close-icon="close"
    />
    <label class="text-label-large">密码</label>
    <v-text-field
        :readonly="loading || alert.show"
        :type="oldPasswordReveal ? 'text' : 'password'"
        density="compact"
        v-model="formState.oldPassword"
        :rules="rules"
        variant="outlined"
        class="mb-2 mt-2"
    >
      <template #append-inner>
        <v-icon @click="oldPasswordReveal = !oldPasswordReveal" :icon="oldPasswordReveal ? 'custom:eye' : 'custom:eye-off'"  size="small"/>
      </template>
    </v-text-field>
    <label class="text-label-large">新密码</label>
    <v-text-field
        :readonly="loading || alert.show"
        :type="newPasswordReveal ? 'text' : 'password'"
        density="compact"
        v-model="formState.newPassword"
        :rules="rules"
        variant="outlined"
        class="mb-2 mt-2"
        @input="passwordInputHandler"
    >
      <template #append-inner>
        <v-icon @click="newPasswordReveal = !newPasswordReveal" :icon="newPasswordReveal ? 'custom:eye' : 'custom:eye-off'"  size="small"/>
      </template>
    </v-text-field>
    <label class="text-label-large">密码强度</label>
    <div class="password-complexity-meter mb-8 mt-2">
      <span :class="strength > 0 ? 'action' : ''"></span>
      <span :class="strength > 1 ? 'action' : ''"></span>
      <span :class="strength > 2 ? 'action' : ''"></span>
      <span :class="strength > 3 ? 'action' : ''"></span>
    </div>
    <label class="text-label-large">再次输入新密码</label>
    <v-text-field
        :readonly="loading || alert.show"
        :type="confirmPasswordReveal ? 'text' : 'password'"
        density="compact"
        v-model="formState.confirmPassword"
        :rules="rules"
        variant="outlined"
        class="mb-2 mt-2"
    >
      <template #append-inner>
        <v-icon @click="confirmPasswordReveal = !confirmPasswordReveal" :icon="confirmPasswordReveal ? 'custom:eye' : 'custom:eye-off'"  size="small"/>
      </template>
    </v-text-field>
    <v-btn
        :loading="loading"
        color="blue-darken-2"
        type="submit"
        rounded="2"
        style="width: 150px"
        class="float-right"
    >
      {{ alert.show ? '再次修改' : '保存' }}
    </v-btn>
  </v-form>
</template>

<style scoped lang="scss">
@use "vuetify";
@use "sass:map";


.password-complexity-meter {
  display: flex;
  grid-gap: .25rem;
  height: .4rem;
  & > span {
    background-color: map.get(vuetify.$grey, 'lighten-2');
    width: 25%;
    &.action {
      background-color: map.get(vuetify.$green, 'base')
    }
  }

}



</style>
