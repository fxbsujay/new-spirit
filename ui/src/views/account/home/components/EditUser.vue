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
Object.assign(formState, toRaw(store.user))
const success = ref(false)
const uploadRef = useTemplateRef('upload-input')

const submitHandle = () => {
  if (success.value) {
    success.value = false
    return
  }
  const formData = new FormData()
  success.value = false
  formData.append('file', uploadRef.value.files[0]);
  formData.append('nickname', formState.nickname);
  http.api('/account/edit', {
    method: Method.POST,
    body: formData
  }).then(() => {
    success.value = true
  })
}

const uploadChangeHandle = e => {
  formState.avatar = URL.createObjectURL(e.target.files[0])
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
          <img width="100%" height="100%" :src="formState.avatar ? '/api/static/avatar/' + formState.avatar : '/avatar-error.jpg'" alt="头像上传">
        </div>
        <div class="upload-wrapper">
          <input :disabled="success" @change="uploadChangeHandle" type="file" accept="image/png,image/jpeg" class="upload-input" ref="upload-input" />
          <button class="upload-btn" :disabled="success">上传头像</button>
        </div>
      </div>
      <div class="form-group col">
        <div class="border-input-wrap">
          <label class="label">
            昵称
          </label>
          <input
              class="input"
              :disabled="success"
              required
              pattern="^[a-zA-Z0-9@!._-+]{2,20}$"
              title="请输入2-20位字母开头的字母数字，或有效的邮箱地址"
              v-model="formState.nickname"
          />
        </div>
      </div>
    </div>


    <button type="submit" class="submit-button button " :class="success ? 'border' : 'black'">{{ success ? '再次编辑' : '保存' }}</button>
  </form>
</template>

<style scoped lang="less">
@import "@/assets/css/variable.less";

.row {
  gap: 1rem;
}

.form {
  .label {
    font-weight: bolder;
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

.success-tip {
  display: flex;
  align-items: center;
  justify-content: left;
  color: white;
  background-color: @c-positive;
  border-radius: 3px;
  padding: 8px 2rem;
  font-size: 14px;
  margin-bottom: 1rem;

  .icon {
    margin-right: 1rem;
  }
}
</style>
