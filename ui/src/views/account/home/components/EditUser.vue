<script setup>
import { useUserStore } from '@/stores/user'
import http, { Method } from '@/utils/http.js'
import { reactive, ref, useTemplateRef } from 'vue'

const store = useUserStore()
const formState = reactive({
    avatar: '',
    nickname: '',
    username: ''
})
const loading = ref(false)
const file = ref(null)
const uploadRef = useTemplateRef('upload-input')
const alert = reactive({
    show: false,
    success: false,
    message: ''
})

const getUserInfo = () => {
    loading.value = true
    http.post('/user/info').then(res => {
        Object.assign(formState, res)
        loading.value = false
    }).catch(() => {
        Object.assign(formState, {
            avatar: '',
            nickname: '',
            username: ''
        })
        loading.value = false
    })
}
getUserInfo()

const submitHandle = async (event) => {
    if (loading.value) {
        return
    }
    if (alert.show) {
        file.value = null
        Object.assign(alert, {
            show: false,
            success: false,
            message: ''
        })
        getUserInfo()
        return
    }
    loading.value = true

    const { valid } = await event
    if (valid) {
        const formData = new FormData()
        if (file.value) {
            formData.append('file', file.value)
        }

        formData.append('nickname', formState.nickname)
        http.api('/account/edit', {
            method: Method.POST,
            body: formData
        }).then(() => {
            loading.value = false
            Object.assign(alert, {
                show: true,
                success: true,
                message: '操作成功'
            })
            store.refreshInfo()
        }).catch(err => {
            loading.value = false
            Object.assign(alert, {
                show: true,
                success: false,
                message: err.message,
            })
        })
    } else {
        loading.value = false
    }
}

const uploadChangeHandle = e => {
    formState.avatar = URL.createObjectURL(e.target.files[0])
    file.value = e.target.files[0]
    return false
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
    <div class="d-flex ga-4 align-end">
      <div class="avatar-editor" @click="() => uploadRef.click()">
        <div class="avatar-img">
          <img width="100%" height="100%"
               :src="formState.avatar ? formState.avatar.startsWith('blob') ? formState.avatar : '/api/static/avatar/' + formState.avatar : '/avatar-error.jpg'"
               alt="头像上传">
        </div>
        <div class="upload-wrapper">
          <input :disabled="alert.show || loading" @change="uploadChangeHandle" type="file"
                 accept="image/png,image/jpeg"
                 class="upload-input" ref="upload-input"/>
          <div class="upload-btn">上传头像</div>
        </div>
      </div>
      <div class="flex-1-1-100 mb-7">
        <label class="text-label-large">
          昵称
        </label>
        <v-text-field
            density="compact"
            v-model="formState.nickname"
            variant="outlined"
            :readonly="alert.show || loading"
            hide-details="auto"
            class="mt-1"
        />
      </div>
    </div>
    <v-btn
        :loading="loading"
        color="blue-darken-2"
        :disabled="!formState.nickname"
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
.avatar-editor {
  position: relative;
  vertical-align: top;
  cursor: pointer;

  .avatar-img {
    width: 100px;
    height: 100px;

    img {
      border-radius: 3px;
    }
  }

  .upload-wrapper {
    .upload-input {
      color: inherit;
      font: inherit;
      display: none;
    }

    .upload-btn {
      cursor: pointer;
      padding: 4px 6px;
      margin-top: 4px;
      font-size: 12px;
      border-radius: 3px;
      background-color: #f1f1f1;
      width: 100%;
      text-align: center;
      border: none;

      &:hover {
        background-color: #d3d3d3;
      }
    }
  }
}

</style>
