<script setup lang="ts">
import Loading from '@/components/loading/index.vue'
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'

const ModeConstant = [
  { label: '休闲赛', value: 'CASUAL' },
  { label: '好友赛', value: 'RANK' },
  { label: '积分赛', value: 'RANK' },
]

const waitGame = reactive({
  code: '',
  boardSize: 0,
  type: 'SHORT',
  mode: 'CASUAL',
  duration: 0,
  stepDuration: 0,
  username: '',
  nickname: '',
  score: 0,
  timestamp: 0,
})
const timeText = ref('')
let timerInterval = null
const isStart = ref(false)

const startWait = game => {
  Object.assign(waitGame, game)
  isStart.value = true

  timerInterval = setInterval(() => {
    if (!isStart) {
      clearInterval(timerInterval)
      timerInterval = null
      timeText.value = ''
      return
    }
    const unix = dayjs().unix()



  }, 1000)
}

const endWait = () => {
  isStart.value = false
}

const detailedText = () => {
  if (!isStart.value) {
    return ''
  }
  let text = ''
  if (waitGame.type === 'SHORT') {
    text += waitGame.duration + 'm + ' + waitGame.stepDuration + 's'
  } else if (waitGame.type === 'LONG') {
    text += waitGame.duration + 'd'
  } else {
    text += '∞'
  }

  text +=  '•' + waitGame.boardSize + 'x' + waitGame.boardSize
  return text
}

defineExpose({ startWait })

</script>

<template>
  <div class="play-panels" :class="isStart ? 'panels-open' : ''">
    <div class="panels">
      <Loading color="#fff" size="24px"/>
      <div class="info">
        <div>
          <span class="type">{{ isStart ? ModeConstant.find(item => item.value === waitGame.mode).label : ''}}</span>
          <span class="text">{{ detailedText() }}</span>
        </div>
        <span class="code">#{{ waitGame.code }}</span>
      </div>
      <div class="time">
        {{ timeText }}
      </div>
    </div>
    <button class="button border" @click="endWait">取消</button>
  </div>
</template>

<style scoped lang="less">
@import "@/assets/css/variable.less";
.play-panels {
  position: fixed;
  top: 0;
  left: 50%;
  visibility: hidden;
  z-index: @headerZIndex + 1;
  transform: translateX(-50%);
  cursor: pointer;

  &.panels-open {
    visibility: visible;
  }

  &:hover {
    .panels {
      height: 90px;
      .text, .code {
        opacity: 1;
        height: auto;
      }
      .time {
        font-size: 14px;
      }
    }
    .button {
      height: 30px;
      opacity: 1;
      transition: opacity 0.3s 0.1s ease;
    }
  }
}

.panels {
  background-color: #3E7DD6;
  padding: 8px 10px;
  width: 250px;
  color: #fff;
  border-radius:  0 0 4px 4px;
  display: flex;
  height: 40px;
  transition: height 0.3s ease;

  .info {
    margin-left: 8px;
    margin-right: auto;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .type {
    font-size: 18px;
    line-height: 0;
  }

  .text, .code {
    display: block;
    font-size: 14px;
    opacity: 0;
    height: 0;
    transition: opacity 0.3s ease;
  }

  .code {
    margin-top: auto;
    transition-delay: 0.2s;
  }

  .time {
    margin-top: auto;
    font-size: 16px;
    transition: font-size 0.3s ease;
  }
}


.button {
  font-size: 14px;
  height: 0;
  margin-top: 6px;
  border-radius: 2px;
  border-color: #351FF2;
  opacity: 0;
}
</style>