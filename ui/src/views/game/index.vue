<script setup>
import Go from '@/components/go/Go.vue'
import { PRETTY_COORDINATE_SEQUENCE } from '@/components/go/goban.js'
import Icon from '@/components/icon/Icon.vue'
import Switch from '@/components/switch/index.vue'
import { useUserStore } from '@/stores/user.js'
import { formatTimeAgo, formatTimeDiff } from '@/utils/time.js'
import { computed, ref, useTemplateRef, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { GameSocket } from './index'
import { getGameEnding } from '@/utils/constant.js'


const router = useRoute()
const userStore = useUserStore()
const peaceSender = ref('')
const chartOpen = ref(false)
const chartMessageRef = useTemplateRef('chartMessage')
const chartMessage = ref('')
const chartMessageLoading = ref(false)

const socket = new GameSocket(router.params.code, pck => {
    switch (pck.type) {
        case 'GAME_PEACE':
            peaceSender.value = pck.data
            break
        case 'GAME_REPLY_PEACE':
            peaceSender.value = ''
            break
        case 'ROOM_CHAT':
            if (socket.chatMessages.value.find(
                item => item.content === chartMessage.value && item.sender === userStore.user.username)
            ) {
                chartMessageLoading.value = false
                chartMessage.value = ''
            }
            nextTick(() => {
                chartMessageRef.value.scrollTop = chartMessageRef.value.scrollHeight;
            })
            break
        case 'ROOM_CHAT_MESSAGE':
            nextTick(() => {
                chartMessageRef.value.scrollTop = chartMessageRef.value.scrollHeight;
            })
            break
    }
})

const { game, board, loading, success } = socket
const isWhite = computed(() => userStore.user.username === game.white.username)

/**
 * 落子
 */
const onBoardClick = (x, y) => {
    const type = userStore.user.username === game.white.username ? 'white' : 'black'
    socket.sendAddStep(type, x, y)
}

/**
 * 显示预落子
 */
const showHintPointFunc = (x, y) => {
    if (socket.isEnd() || socket.stoneAt(socket.board.value, x, y) || !socket.inBoard(x, y)) {
        return {
            show: false
        }
    }
    const type = userStore.user.username === game.white.username ? 'white' : 'black'
    if (socket.currentColor() === type) {
        return {
            show: true,
            type
        }
    } else {
        return {
            show: false,
            type
        }
    }
}

const controlsClickHandler = type => {
    if (type === 'CANCEL') {
        socket.sendCancel()
    } else if (type === 'PEACE') {
        socket.sendPeace()
    } else if (type === 'SURRENDER') {
        socket.sendSurrender()
    }
}

const peaceClickHandler = isAgree => {
    socket.sendReplyPeace(isAgree)
}

const sendChatClickHandler = () => {
    if (!chartMessageLoading.value) {
        socket.sendChat(chartMessage.value)
        chartMessageLoading.value = true
    }
}

</script>

<template>
  <div class="main-container">
    <div class="side side-info">
      <div class="card">
        <div class="game-info">
          <img class="mode-icon" src="@/assets/img/game-rank.png" alt="排位">
          <div class="info">
            <div class="text-grey-darken-4">
              {{ game.info.mode === 'CASUAL' ? '休闲赛' :  game.info.mode === 'RANK' ? '积分赛' : '人机对战' }} •
              {{ game.info.type === 'SHORT' ? `实时棋局 • ${ game.info.duration / 1000 / 60 }+${ game.info.stepDuration / 1000 }` : game.info.type === 'LONG' ? '通讯棋 • 1天' : '无限制' }}
             </div>
            <div class="text-grey-darken-1 text-label-medium">
              {{ socket.isEnd() ? '对局已结束' : game.steps.length > 0 ? formatTimeAgo(game.steps[game.steps.length - 1].timestamp) + (socket.currentColor() === 'black' ? '黑棋落子' : '白棋落子') : '正在进行中' }}
            </div>
          </div>
          <Icon name="star" size="1.5rem" color="#F0B01A"/>
        </div>
        <div class="player">
          <img class="player-icon" alt="白棋选手"
               :src="game.white.avatar ? '/api/static/avatar/' + game.white.avatar : '/avatar-error.jpg'"/>
          <span class="player-name">{{ game.white.nickname }}</span>
          <span class="player-score">{{ game.white.rating }}</span>
        </div>
        <div class="player">
          <img class="player-icon" alt="黑棋选手"
               :src="game.black.avatar ? '/api/static/avatar/' + game.black.avatar : '/avatar-error.jpg'"/>
          <span class="player-name">{{ game.black.nickname }}</span>
          <span class="player-score">{{ game.black.rating }}</span>
        </div>
      </div>
      <div class="chat-wrap" v-if="!socket.isEnd()">
        <div class="header card">
          <span>聊天室</span>
          <Switch v-model="chartOpen"/>
        </div>
        <div class="message-box card overflow-y-auto scrollbar" ref="chartMessage">
          <div v-for="item in socket.chatMessages.value" class="message-item" :class="item.sender === userStore.user.username ? 'receive' : ''">
            <img :src="'/api/static/avatar/' + (item.sender === game.black.username ? game.black.avatar : game.white.avatar)" class="avatar" alt="头像"/>
            <div class="info">
              <div class="username">{{ item.sender === game.black.username ? game.black.nickname : game.white.nickname }}</div>
              <div class="content">
                <span class="tag"/>
                <span class="text">{{ item.content }}</span>
              </div>
            </div>
          </div>
        </div>
        <div>
          <v-text-field
              class="mt-2 border-0"
              style="background: white"
              v-model="chartMessage"
              :loading="chartMessageLoading"
              append-inner-icon="send"
              density="compact"
              label="恶语伤人六月寒..."
              variant="outlined"
              hide-details
              single-line
              @click:append-inner="sendChatClickHandler"
          />
        </div>
      </div>
    </div>
    <div class="board">
      <Go :size="game.info.boardSize" :points="board" :onBoardClick="onBoardClick" :showHintPointFunc="showHintPointFunc"/>
    </div>
    <div class="side controller-side">
      <div class="game-time">
        <div class="time">
          <time v-if="!socket.isEnd()">
            {{ formatTimeDiff(isWhite ? game.black.remainder : game.white.remainder) }}
          </time>
        </div>
        <Icon name="signal" size="20px" color="#F0B01A"/>
        <div class="icon-box">
          <Icon name="plus" size="1rem" color="#fff"/>
        </div>
      </div>
      <div class="time-progress"></div>
      <div class="user-info">
        <img v-if="isWhite" src="@/assets/img/b.png" class="avatar" alt="黑棋玩家"/>
        <img v-else src="@/assets/img/w.png" class="avatar" alt="白棋玩家"/>
        <span class="username">{{ isWhite ? game.black.nickname : game.white.nickname }}</span>
        <span class="source-label">提子 -</span>
        <span class="source-value">{{ isWhite ? game.black.captured : game.white.captured }}</span>
      </div>
      <div class="controller">
        <div class="buttons">
          <div class="btn">
            <Icon name="video"/>
          </div>
          <div class="btn">
            <Icon name="skip-back"/>
          </div>
          <div class="btn">
            <Icon name="skip-previous"/>
          </div>
          <div class="btn">
            <Icon name="skip-next"/>
          </div>
          <div class="btn">
            <Icon name="skip-for"/>
          </div>
          <div class="btn">
            <v-icon icon="flag" size="1.5rem" color="green"></v-icon>
          </div>
        </div>
        <div class="step-wrap">
          <div v-if="socket.stepLength() < 1" class="opening-tips d-flex align-center h-100 text-body-medium text-grey-darken-3">
            <v-icon class="mx-5" icon="bell-alert" size="2.5rem" color="grey-darken-2"></v-icon>
            <div class="message">
              {{ isWhite ? '您执白棋' : '您执黑棋' }}<br/>
              <strong v-if="!isWhite">该您走棋了！</strong>
            </div>
          </div>
          <div class="step" v-for="index in Math.ceil(socket.stepLength() / 2)">
            <span class="number">{{ index }}</span>
            <span class="pos">
              {{ PRETTY_COORDINATE_SEQUENCE[game.steps[(index - 1) * 2].x] }}{{ game.steps[(index - 1) * 2].y + 1 }}
            </span>
            <span class="pos" v-if="game.steps[index * 2 - 1]">
              {{ PRETTY_COORDINATE_SEQUENCE[game.steps[index * 2 - 1].x] }}{{ game.steps[index * 2 - 1].y + 1 }}
            </span>
          </div>
        </div>
        <template v-if="socket.isEnd()">
          <div class="text-center mb-4 text-grey-darken-3">
            {{ getGameEnding(game.info.winner, game.info.reason) }}
          </div>
        </template>
        <template v-else>
          <div v-if="!peaceSender" class="controls d-flex justify-center align-center ga-2 py-1">
            <div :class="socket.stepLength() < 2 ? 'controls-icon' : 'controls-disabled'" class="flag-icon py-1 px-2 rounded-md" @click="controlsClickHandler('CANCEL')">
              <v-icon icon="close" size="2rem" color="grey-darken-3"></v-icon>
              <v-tooltip
                  activator="parent"
                  location="top"
                  text="取消对局"
              />
            </div>
            <div :class="socket.stepLength() > 1 ? 'controls-icon' : 'controls-disabled'" class="fraction-icon py-1 px-2 rounded-md" @click="controlsClickHandler('PEACE')">
              <v-icon icon="fraction-one-half" size="1.7rem" color="grey-darken-3"></v-icon>
              <v-tooltip
                  activator="parent"
                  location="top"
                  text="和棋"
              />
            </div>
            <div :class="socket.stepLength() > 1 ? 'controls-icon' : 'controls-disabled'" class="flag-icon py-1 px-2 rounded-md" @click="controlsClickHandler('SURRENDER')">
              <v-icon icon="flag" size="2rem" color="grey-darken-3"></v-icon>
              <v-tooltip
                  activator="parent"
                  location="top"
                  text="认输"
              />
            </div>
          </div>
          <div v-else class="peace d-flex align-center text-center bg-grey-lighten-3">
            <div v-if="peaceSender !== userStore.user.username" class="peace-no bg-white py-4 px-5" @click="peaceClickHandler(false)">
              <v-icon icon="close" size="1.7rem" color="red-darken-4"></v-icon>
            </div>
            <div class="peace-text py-2 flex-1-1-100 text-body-medium text-grey-darken-2">
              {{ peaceSender !== userStore.user.username ? '对方请求和棋' : '和棋请求已发送' }}
            </div>
            <div v-if="peaceSender !== userStore.user.username" class="peace-yes bg-white py-4 px-5" @click="peaceClickHandler(true)">
              <v-icon icon="fraction-one-half" size="1.7rem" color="green-accent-4"></v-icon>
            </div>
          </div>
        </template>
      </div>
      <div class="user-info">
        <img v-if="isWhite" src="@/assets/img/w.png" class="avatar" alt="白棋玩家"/>
        <img v-else src="@/assets/img/b.png" class="avatar" alt="黑棋玩家"/>
        <span class="username">{{ isWhite ? game.white.nickname : game.black.nickname }}</span>
        <span class="source-label">提子 -</span>
        <span class="source-value">{{ isWhite ? game.white.captured : game.black.captured }}</span>
      </div>
      <div class="time-progress"></div>
      <div class="game-time">
        <div class="time">
          <time v-if="!socket.isEnd()">
            {{ formatTimeDiff(isWhite ? game.white.remainder : game.black.remainder) }}
          </time>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "./index" as *;
</style>
