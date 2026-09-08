<script setup>
import { reactive, ref } from 'vue'
import { Regex } from '@/utils/constant.js'
import http from '@/utils/http.js'
import snackbar from '@/components/snackbar/index.js'
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
const result = reactive({
    success: false,
    message: ''
})
const submitHandle = async (event) => {
    if (loading.value) {
        return
    }
    if (result.success) {
        Object.assign(formState, {
            oldPassword: '',
            newPassword: '',
            confirmPassword: ''
        })
        result.success = false
        return
    }
    loading.value = true
    const { valid } = await event
    if (valid) {
        if (formState.newPassword === formState.oldPassword) {
            snackbar.warning('新密码不能与旧密码相同')
            loading.value = false
            return
        }

        if (formState.newPassword !== formState.confirmPassword) {
            snackbar.warning('两次输入的新密码不同')
            loading.value = false
            return
        }
        loading.value = true
        http.post('/account/password', formState).then(() => {
            loading.value = false
            result.success = true
        }).catch(() => loading.value = false)
    } else {
        loading.value = false
    }

}

const passwordInputHandler = (event) => {
    strength.value = passwordStrength(event.target.value)
}

</script>

<template>
  <v-form validate-on="blur" @submit.prevent="submitHandle">
    <v-alert
        v-model="success"
        class="mb-4"
        color="success"
        variant="tonal"
        density="compact"
        text="操作成功"
        closable
        icon="check-bold"
        close-icon="custom:close"
    />
    <label class="text-label-large">密码</label>
    <v-text-field
        :readonly="loading"
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
        :readonly="loading"
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
        :readonly="loading"
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
      {{ success ? '再次修改' : '保存' }}
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
