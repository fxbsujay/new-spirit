<script setup>
import { reactive, ref, toRaw, useTemplateRef } from 'vue'
import { useUserStore } from '@/stores/user'
import http, { Method } from '@/utils/http.js'

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
  http.post("/user/info").then(res => {
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
    formData.append('file', file.value);
  }

  formData.append('nickname', formState.nickname);
  http.api('/account/edit', {
    method: Method.POST,
    body: formData
  }).then(() => {
    loading.value = false
    success.value = true
    store.refreshInfo()
  }).catch(() => loading.value = false)
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
    <div class="success-tip" v-if="success">
      <Icon name="check-bold" color="#fff" size="2rem" />
      <span>操作成功</span>
    </div>
    <div class="row" style="align-items: end">
      <div class="avatar-editor" @click="() => uploadRef.click()">
        <div class="avatar-img">
          <img width="100%" height="100%" :src="formState.avatar ? formState.avatar.startsWith('blob') ? formState.avatar : '/api/static/avatar/' + formState.avatar : '/avatar-error.jpg'" alt="头像上传">
        </div>
        <div class="upload-wrapper">
          <input :disabled="success || loading" @change="uploadChangeHandle" type="file" accept="image/png,image/jpeg" class="upload-input" ref="upload-input" />
          <div class="upload-btn" >上传头像</div>
        </div>
      </div>
      <div class="form-group col">
        <div class="border-input-wrap">
          <label class="label">
            昵称
          </label>
          <input
              class="input"
              :disabled="success || loading"
              required
              pattern="^[a-zA-Z0-9@!._-+]{2,20}$"
              title="请输入2-20位字母开头的字母数字，或有效的邮箱地址"
              v-model="formState.nickname"
          />
        </div>
      </div>
    </div>
    <button type="submit" class="submit-button button " :disabled="loading" :class="success ? 'border' : 'black'">{{ success ? '再次修改' : '保存' }}</button>
  </form>
</template>

<style scoped lang="less">
@import "@/assets/css/variable.less";
@import "./index.less";

.row {
  gap: 1rem;
}

.avatar-editor {
  position: relative;
  vertical-align: top;
  cursor: pointer;
  .avatar-img {
    width: 100px;
    height: 100px;
    img {
      border-radius: @borderRadius;
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
      border-radius: @borderRadius;
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
