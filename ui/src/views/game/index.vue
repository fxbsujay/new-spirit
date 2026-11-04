<script setup>
import Go from '@/components/go/Go.vue'
import { PRETTY_COORDINATE_SEQUENCE } from '@/components/go/goban.js'
import Icon from '@/components/icon/Icon.vue'
import Switch from '@/components/switch/index.vue'
import { useUserStore } from '@/stores/user.js'
import { formatTimeDiff } from '@/utils/time.js'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { GameSocket } from './index'

const value = ref(false)
const router = useRoute()
const userStore = useUserStore()
const peaceSender = ref('')

const socket = new GameSocket(router.params.code, pck => {
    switch (pck.type) {
        case 'GAME_PEACE':
            peaceSender.value = pck.data
            break
        case 'GAME_REPLY_PEACE':
            peaceSender.value = ''
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
    socket.addStep(type, x, y)
}

/**
 * 显示预落子
 */
const showHintPointFunc = (x, y) => {
    if (socket.stoneAt(socket.board.value, x, y)) {
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
        socket.cancel()
    } else if (type === 'PEACE') {
        socket.peace()
    } else if (type === 'SURRENDER') {
        socket.surrender()
    }
}

const peaceClickHandler = isAgree => {
    socket.replyPeace(isAgree)
}

</script>

<template>
  <div class="main-container">
    <div class="side side-info">
      <div class="card">
        <div class="game-info">
          <img class="mode-icon" src="@/assets/img/game-rank.png" alt="排位">
          <div class="info">
            <div>10h+6s • 休闲 • 通讯棋</div>
            <div>9分钟前</div>
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
      <div class="chat-wrap">
        <div class="header card">
          <span>聊天室</span>
          <Switch v-model="value"/>
        </div>
        <div class="message-box card">
          <div class="message-item">
            <img src="@/assets/img/w.png" class="avatar" alt="头像"/>
            <div class="info">
              <div class="username">Evan Guzman</div>
              <div class="content">
                <span class="tag"/>
                <span class="text">send message send12222222222222222222222133432123123124 </span>
              </div>
            </div>
          </div>

          <div class="message-item receive">
            <img src="@/assets/img/b.png" class="avatar" alt="头像"/>
            <div class="info">
              <div class="username">Gou Dan</div>
              <div class="content">
                <span class="tag"/>
                <span class="text">Send message send12222222222222222222222133432123123124</span>
              </div>
            </div>
          </div>
        </div>
        <div class="send-input">
          <input class="input" placeholder="恶语伤人六月寒..."/>
          <div class="icon-box">
            <Icon name="send" size="1rem" color="#fff"/>
          </div>
        </div>
      </div>
    </div>
    <div class="board">
      <Go :points="board" :onBoardClick="onBoardClick" :showHintPointFunc="showHintPointFunc"/>
    </div>
    <div class="side controller-side">
      <div class="game-time">
        <div class="time">
          <time>{{ formatTimeDiff(isWhite ? game.black.remainder : game.white.remainder) }}
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
        <div v-if="!peaceSender" class="controls d-flex justify-center align-center ga-2 py-1">
          <div :class="socket.stepLength() < 2 ? 'controls-icon' : 'controls-disabled'" class=" flag-icon py-1 px-2 rounded-md" @click="controlsClickHandler('CANCEL')">
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
        <div class="peace d-flex align-center text-center bg-grey-lighten-3" v-else>
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
          <time>{{ formatTimeDiff(isWhite ? game.white.remainder : game.black.remainder) }}
          </time>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "./index" as *;
</style>
