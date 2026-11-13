<script setup>
import { reactive, ref, useTemplateRef } from 'vue'
import http from '@/utils/http.js'
import { Regex } from '@/utils/constant.js'
import { useUserStore } from '@/stores/user'
import snackbar from '@/components/snackbar/index.js'

const store = useUserStore()
const formEmailRef = useTemplateRef('formEmailRef')
const formState = reactive({
    password: '',
    email: '',
    code: ''
})

const rules = {
    password: [value => !value || !Regex.PASSWORD.test(value) ? '请输入6-30位字母，数字或以下@!$^.*_%合法符号' : true],
    email: [value => !value || !Regex.EMAIL.test(value) ? '请输入正确的电子邮箱' : true],
    code: [value => !value || !Regex.EMAIL_CODE.test(value) ? '请输入5位大写英文字母或数字' : true],
}

const passwordReveal = ref(false)
const loading = ref(false)
const sender = ref(0)
const sending = ref(false)
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
        http.post('/account/email', formState).then(() => {
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

const sendCode = async () => {
    if (loading.value || sending.value) {
        return
    }

    sending.value = true
    const valid = await formEmailRef.value.validate()
    if (valid.length === 0) {
        if (formState.email === store.user.email) {
            Object.assign(alert, {
                show: true,
                success: false,
                message: '邮箱地址不能与原邮箱地址相同'
            })
            return
        }
        http.post('/account/send/code', { email: formState.email }).then(() => {
            snackbar.success('发送成功')
            sending.value = false
            sender.value = 60
            const timer = setInterval(() => {
                sender.value--
                if (sender.value <= 0) {
                    clearInterval(timer)
                }
            }, 1000)
        }).catch(error => {
            snackbar.warning(error.message)
            sending.value = false
        })

    } else {
        sending.value = false
    }
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
        :type="passwordReveal ? 'text' : 'password'"
        density="compact"
        v-model="formState.password"
        :rules="rules.password"
        variant="outlined"
        class="mb-2 mt-2"
    >
      <template #append-inner>
        <v-icon @click="passwordReveal = !passwordReveal" :icon="passwordReveal ? 'eye' : 'eye-off'" size="small"/>
      </template>
    </v-text-field>

    <label class="text-label-large">邮箱</label>
    <v-text-field
        :readonly="loading || alert.show"
        ref="formEmailRef"
        density="compact"
        v-model="formState.email"
        :rules="rules.email"
        variant="outlined"
        type="email"
        class="mb-2 mt-2"
    >
      <template #append>
        <v-btn :variant="sender !== 0 ? 'outlined' : 'tonal'" :loading="sending" :disabled="sender !== 0"
               @click="sendCode"> {{ sender !== 0 ? `${ sender }秒后重新发送` : '发送验证码' }}
        </v-btn>
      </template>
    </v-text-field>

    <label class="text-label-large">验证码</label>
    <v-text-field
        :readonly="loading || alert.show"
        type="text"
        density="compact"
        v-model="formState.code"
        :rules="rules.code"
        variant="outlined"
        class="mb-2 mt-2"
    >
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
@use "@/assets/css/variable" as *;
@use "../index" as *;

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
