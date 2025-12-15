<script setup >
import Loading from '@/components/loading/index.vue'
import { ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'
import http from '@/utils/http'

const ModeConstant = [
  { label: '休闲赛', value: 'CASUAL' },
  { label: '好友赛', value: 'RANK' },
  { label: '积分赛', value: 'RANK' },
]

const { waitGame, closeWaitGame } = useUserStore()

watch(waitGame, value => {
  if (value.code) {
    startWait(value)
  } else {
    isStart.value = false
    clearTimer()
  }
})

const timeText = ref('')
let timerInterval = null
const isStart = ref(false)
const loading = ref(false)

const startWait = game => {
  isStart.value = true
  timerInterval = setInterval(() => {
    if (!isStart) {
      clearTimer()
      return
    }
    const diff = dayjs().unix() - game.timestamp
    timeText.value = `${(diff / 60).toFixed(0).padStart(2, '0')}:${(diff % 60).toFixed(0).padStart(2, '0')}`
  }, 1000)
}

const clearTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
  timeText.value = ''
}

const endWait = () => {
  isStart.value = false
  loading.value = true

  http.post('/game/cancel').then(() => {
    clearTimer()
    loading.value = false
    closeWaitGame()
  }).catch(() => loading.value = false)
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

</script>

<template>
  <div class="play-panels" v-if="isStart">
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
    <button :disabled="loading" class="button border" @click="endWait">取消</button>
  </div>
</template>

<style scoped lang="less">
@import "@/assets/css/variable.less";
.play-panels {
  position: fixed;
  top: 0;
  left: 50%;
  z-index: @headerZIndex + 1;
  transform: translateX(-50%);
  cursor: pointer;

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