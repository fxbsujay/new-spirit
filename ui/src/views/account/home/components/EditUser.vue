<script setup>
import { reactive, toRaw, useTemplateRef } from 'vue'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const formState = reactive({
  avatar: '',
  nickname: '',
  username: ''
})
Object.assign(formState, toRaw(store.user))

const uploadRef = useTemplateRef('upload-input')

const submitHandle = () => {
  console.log(store.user)
}

const uploadChangeHandle = e => {
  console.log(e.target.files[0])
}

</script>

<template>
  <form class="form" @submit.prevent="submitHandle">
    <div class="row" style="align-items: end">
      <div class="avatar-editor" @click="() => uploadRef.click()">
        <div class="avatar-img">
          <img width="100%" height="100%" :src="formState.avatar" alt="头像上传">
        </div>
        <div class="upload-wrapper">
          <input @change="uploadChangeHandle" type="file" accept="image/png,image/jpeg" class="upload-input" ref="upload-input" />
          <button class="upload-btn">上传头像</button>
        </div>
      </div>
      <div class="form-group col">
        <div class="border-input-wrap">
          <label class="label">
            昵称
          </label>
          <input
              class="input"
              required
              pattern="^[a-zA-Z0-9@!._-+]{2,20}$"
              title="请输入2-20位字母开头的字母数字，或有效的邮箱地址"
              v-model="formState.nickname"
          />
        </div>
      </div>
    </div>
    <button type="submit" class="button black">保存</button>
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

  .button {
    background-color: #312d2a;
    margin-top: 2rem;
    height: 36px;
    font-size: 14px;
    max-width: 100%;
    width: 150px;
    float: right;
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
</style>
