<script setup>

import Dialog from '@/components/dialog/index.vue'
import Slider from '@/components/slider/index.vue'
import { reactive, ref, toRaw } from 'vue'
import http from '@/utils/http'
import { TypeConstant } from '@/constant'
import { throttle } from '@/utils/index.js'

const emits = defineEmits(['createSuccess'])
const visible = ref(false)
const loading = ref(false)

const ModeConstant = [
  { label: '休闲', value: 'CASUAL' },
  { label: '好友', value: 'RANK' },
]

const BoardSizeConstant = [
  { label: '9x9', value: 9 },
  { label: '13x13', value: 13 },
  { label: '19x19', value: 19 },
  { label: '21x21', value: 21 },
  { label: '25x25', value: 25 },
]

const formState = reactive({
  boardSize: 21,
  type: 'SHORT',
  mode: 'CASUAL',
  duration: 10,
  stepDuration: 0
})

const open = () => {
  visible.value = true
}

const close = () => {
  loading.value = false
  Object.assign(formState, {
    boardSize: 21,
    type: 'SHORT',
    mode: 'CASUAL',
    duration: 10,
    stepDuration: 0
  })
  visible.value = false
}

const submitHandle = throttle(() => {
  if (loading.value) {
    return
  }
  loading.value = true
  http.post('/game/create', formState).then(() => {
    close()
    emits('createSuccess')
  }).catch(() => loading.value = false)
})

defineExpose({ open })
</script>

<template>
  <Dialog :visible="visible">
    <div class="create-game-dialog" style="background-color: #fff">
      <form class="form" @submit.prevent="submitHandle">

        <div class="row input-row">
          <div class="form-group col">
            <label class="form-label" >
              棋盘尺寸
            </label>
            <select v-model="formState.boardSize" class="select">
              <option v-for="item in BoardSizeConstant" :value="item.value">{{ item.label }}</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label" >
            各方限时（分钟）：{{ formState.duration }}
          </label>
          <Slider v-model="formState.duration" :step="1" :min="1" :max="180"/>
        </div>
        <div class="form-group">
          <label class="form-label" >
            每步加时（秒）：{{ formState.stepDuration }}
          </label>
          <Slider v-model="formState.stepDuration" :step="1" :min="1" :max="180"/>
        </div>

        <div class="form-group">
          <div class="row toggles">
            <button class=" button" type="button" v-for="item in ModeConstant" :class="`${formState.mode === item.value ? 'active' : ''}`" @click="formState.mode = item.value">
              {{ item.label }}
            </button>
          </div>
        </div>

        <div class="form-footer">
          <button class="button border" @click="close"  type="button" >取消</button>
          <button class="button black submit-btn" type="submit" >创建</button>
        </div>
      </form>
    </div>
  </Dialog>
</template>

<style scoped lang="less">
@import "@/assets/css/variable.less";
.create-game-dialog {
  padding: 2rem;
  position: relative;
  width: 450px;
  max-width: 100%;

  .form {
    .border-input-wrap {
      .input {
        height: auto;
        font-size: 14px;
      }
    }
  }

  .form-label {
    display: block;
  }

  .input-row {
    gap: 1rem;
  }

  .toggles {
    margin: 2rem 0;
    justify-content: center;
    .button {
      font-weight: normal;
      border-radius: 0;
      background-color: #F5F5F5;
      width: 180px;
      max-width: 50%;
      height: 2.6rem;

      &:not(.active):hover {
        background-color: #e6e5e5;
      }
    }

    .active {
      box-shadow: 0 2px 4px  rgba(0, 0, 0, 0.25);
      color: #fff;
      background-color: #616161;
    }
  }

  .form-footer {
    margin-top: 3rem;
    border-top: 1px solid #ddd;
    padding-top: 2rem;
    display: flex;
    gap: 1rem;

    .submit-btn {
      background-color: #4A4D4F;
    }

    .button {
      min-width: 0;
      width: 100%;
      border-radius: 2px;
    }
  }

}
</style>