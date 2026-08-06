<script setup>
import Icon from '@/components/icon/Icon.vue'
import snackbar from '@/components/snackbar/index.js'
import { TypeConstant } from '@/constant'
import { useUserStore } from '@/stores/user'
import http from '@/utils/http'
import { debounce } from '@/utils/index'
import { reactive, ref } from 'vue'

const BoardSizeConstant = [
    { label: '9x9', value: 9 },
    { label: '13x13', value: 13 },
    { label: '19x19', value: 19 },
    { label: '21x21', value: 21 },
    { label: '25x25', value: 25 }
]

const RuleConstant = [
    { label: '中国规则', value: 1 },
    { label: '韩国', value: 2 }
]

const shortDurations = []
const shortStepDuration = []
for (let i = 1; i < 35; i++) {
    if (i <= 20) {
        shortDurations.push(i)
        shortStepDuration.push(i)
    } else if (i <= 25) {
        shortDurations.push(shortDurations[i - 2] + 5)
        shortStepDuration.push(shortStepDuration[i - 2] + 5)
    } else {
        shortDurations.push(shortDurations[i - 2] + 15)
        if (i <= 26) {
            shortStepDuration.push(shortStepDuration[i - 2] + 15)
        } else if (i <= 30) {
            shortStepDuration.push(shortStepDuration[i - 2] + 30)
        }
    }
}

const { startWaitGame } = useUserStore()
const drawerVisible = ref(false)
const loading = ref(false)
const formShow = ref(false)
const formState = reactive({
    type: 'SHORT',
    rule: 1,
    boardSize: 21,
    duration: 10,
    stepDuration: 10
})

const modeChangeHandle = (mode) => {
    if (mode === 'CASUAL') {
        formShow.value = true
    } else if (mode === 'RANK') {
        http.post('/game/ranking').then(res => {
            if (res) {
                startWaitGame({
                    boardSize: 19,
                    type: 'SHORT',
                    mode: 'RANK',
                    duration: 60,
                    stepDuration: 60,
                    timestamp: 0
                })
                closeDrawer()
            } else {
                snackbar.warning('您已在匹配队列中')
            }
        })
    }
}

const typeChangeHandle = (type) => {
    formState.type = type
    if (type === 'SHORT') {
        formState.duration = 10
        formState.stepDuration = 10
    } else if (type === 'LONG') {
        formState.duration = 1
        formState.stepDuration = 0
    } else {
        formState.duration = 0
        formState.stepDuration = 0
    }
}

const createHandle = debounce(() => {
    if (loading.value) {
        return
    }
    loading.value = true

    const info = formState.type === 'SHORT' ? { ...formState, duration: shortDurations[formState.duration], stepDuration: shortStepDuration[formState.stepDuration] } : formState

    http.post('/game/create', info).then(code => {
        loading.value = false
        startWaitGame({
            ...info,
            code,
            mode: 'CASUAL'
        })
        closeDrawer()
    }).catch(() => {
        loading.value = false
    })
})

const closeDrawer = () => {
    drawerVisible.value = false
    cancelCreateHandle()
}

const cancelCreateHandle = () => {
    if (loading.value) {
        return
    }
    Object.assign(formState, {
        type: 'SHORT',
        rule: 1,
        boardSize: 21,
        duration: 10,
        stepDuration: 0
    })
    formShow.value = false
}

</script>

<template>
  <v-btn
      color="blue-darken-2"
      class="font-weight-medium text-white py-5"
      @click="drawerVisible = !drawerVisible"
      rounded="2"
      text="开始游戏"
  />
  <v-overlay v-model="drawerVisible" transition="dialog-top-transition">
    <div class="drawer-content">
      <v-btn
          color="red-darken-2"
          class="font-weight-medium text-white py-5 my-2"
          @click="closeDrawer"
          rounded="2"
          text="关闭"
      />
      <div class="drawer-content-wrapper">
        <div class="mode-box" v-if="!formShow">
          <div class="mode-selected">
            <div class="item" @click="modeChangeHandle('CASUAL')">
              <Icon name="game-mode-1" size="3rem" color="#1f1f1f"/>
              <div class="mode-name">休闲赛</div>
              <div class="mode-doc">自定义棋盘尺寸以及比赛时长</div>
            </div>
            <div class="item" @click="modeChangeHandle('RANK')">
              <Icon name="game-mode-2" size="3rem" color="#1f1f1f"/>
              <div class="mode-name">积分赛</div>
              <div class="mode-doc">为你寻找旗鼓相当的对手</div>
            </div>
            <div class="item" @click="modeChangeHandle('ROBOT')">
              <Icon name="game-mode-3" size="3rem" color="#1f1f1f"/>
              <div class="mode-name">人机对战</div>
              <div class="mode-doc">与AI对弈，多个难度等级可选</div>
            </div>
            <div class="item" @click="modeChangeHandle('LOCAL')">
              <Icon name="game-mode-4" size="3rem" color="#1f1f1f"/>
              <div class="mode-name">同屏对战</div>
              <div class="mode-doc">与您的好友面对面下棋</div>
            </div>
          </div>
          <span class="tip">! 鼠标点击上分选择模式开始游戏</span>
        </div>
        <div v-else class="form">
          <div class="flex" style="gap: 2rem">
            <div class="tabs-horiz">
              <button type="button" v-for="item in TypeConstant" :class="formState.type === item.value ? 'active' : ''"
                      @click="typeChangeHandle(item.value)">{{ item.label }}
              </button>
            </div>
            <div class="flex flex-column flex-1">
              <div class="row gap-1r">
                <div class="col">
                  <span class="form-label">游戏规则</span>
                  <v-select
                      density="compact"
                      item-title="label"
                      :items="RuleConstant"
                      variant="outlined"
                      menu-icon="custom:menu"
                      v-model="formState.rule"
                  >
                  </v-select>
                </div>
                <div class="col">
                  <span class="form-label">棋盘尺寸</span>
                  <v-select
                      density="compact"
                      item-title="label"
                      :items="BoardSizeConstant"
                      variant="outlined"
                      menu-icon="custom:menu"
                      v-model="formState.boardSize"
                  >
                  </v-select>
                </div>
              </div>
              <div class="row gap-1r" v-if="formState.type !== 'NONE'">
                <div class="form-group col">
                  <label class="form-label" style="width: 100%;">
                    <span>{{ formState.type === 'SHORT' ? '各方限时（分钟）' : '每步允许天数' }}</span>
                    <span style="float: right; font-weight: bolder">{{
                        formState.type === 'SHORT' ? shortDurations[formState.duration] : formState.duration
                      }}</span>
                  </label>
                  <input class="range" type="range" v-model="formState.duration" min="0" :max="formState.type === 'SHORT' ? 33 : 14"/>
                </div>
                <div class="form-group col" v-if="formState.type === 'SHORT'">
                  <label class="form-label" style="width: 100%">
                    <span>每步加时（秒）</span>
                    <span style="float: right; font-weight: bolder">{{
                        shortStepDuration[formState.stepDuration]
                      }}</span>
                  </label>
                  <input class="range" type="range" v-model="formState.stepDuration" min="0" max="29"/>
                </div>
              </div>
              <span class="mb-3" v-else>请随意安排时间</span>
            </div>
          </div>
          <div class="flex justify-center btn-row">
            <button class="button submit-btn" @click="createHandle">立即创建</button>
            <button class="button cancel-btn" @click="cancelCreateHandle">取消</button>
          </div>
        </div>
      </div>
    </div>
  </v-overlay>

</template>

<style scoped lang="less">
@import "palyDrawer.less";
</style>
