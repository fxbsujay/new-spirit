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
const success = ref(false)
const loading = ref(false)
const file = ref(null)
const uploadRef = useTemplateRef('upload-input')

const getUserInfo = () => {
    http.post('/user/info').then(res => {
        Object.assign(formState, res)
    }).catch(() => {
        Object.assign(formState, {
            avatar: '',
            nickname: '',
            username: ''
        })
    })
}
getUserInfo()

const submitHandle = () => {
    if (loading.value) {
        return
    }
    if (success.value) {
        file.value = null
        success.value = false
        getUserInfo()
        return
    }
    const formData = new FormData()
    loading.value = true
    if (file.value) {
        formData.append('file', file.value)
    }

    formData.append('nickname', formState.nickname)
    http.api('/account/edit', {
        method: Method.POST,
        body: formData
    }).then(() => {
        loading.value = false
        success.value = true
        store.refreshInfo()
    }).catch(() => {
        loading.value = false
    })
}

const uploadChangeHandle = e => {
    formState.avatar = URL.createObjectURL(e.target.files[0])
    file.value = e.target.files[0]
    console.log(file.value)
    return false
}
</script>

<template>
  <form class="form" @submit.prevent="submitHandle">
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
    <div class="d-flex ga-4 align-end">
      <div class="avatar-editor" @click="() => uploadRef.click()">
        <div class="avatar-img">
          <img width="100%" height="100%"
               :src="formState.avatar ? formState.avatar.startsWith('blob') ? formState.avatar : '/api/static/avatar/' + formState.avatar : '/avatar-error.jpg'"
               alt="头像上传">
        </div>
        <div class="upload-wrapper">
          <input :disabled="success || loading" @change="uploadChangeHandle" type="file"
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
            density="comfortable"
            v-model="formState.nickname"
            variant="outlined"
            :readonly="success"
            hide-details="auto"
            class="mt-1"
        />
      </div>
    </div>
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
  </form>
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
