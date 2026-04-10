<script setup>
import Icon from '@/components/icon/Icon.vue'
import { ref, reactive, watch } from 'vue'
import { TypeConstant } from '@/constant'

const props = defineProps({
  visible: Boolean
})

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
const formShow = ref(false)
const formState = reactive({
  type: 'SHORT',
  rule: 1,
  boardSize: 21,
  duration: 10,
  stepDuration: 0
})

const modeChangeHandle = (mode) => {
  if (mode === 'CASUAL') {
    formShow.value = true
  }
}

const typeChangeHandle = (type) => {
  formState.type = type
}

watch(() => props.visible, () => {
  formShow.value = false
})
</script>

<template>
  <transition>
    <div class="play-drawer" v-if="visible">
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
        <form v-else class="form">
          <div class="flex" style="gap: 2rem">
            <div class="tabs-horiz">
              <button type="button" v-for="item in TypeConstant" :class="formState.type === item.value ? 'active' : ''"
                      @click="typeChangeHandle(item.value)">{{ item.label }}
              </button>
            </div>
            <div class="flex flex-column gap-8 flex-1">
              <div class="row gap-8">
                <div class="form-group col">
                <span class="form-label">
                  游戏规则
                </span>
                  <select class="select" v-model="formState.rule">
                    <option v-for="item in RuleConstant" :value="item.value">{{ item.label }}</option>
                  </select>
                </div>
                <div class="form-group col">
                <span class="form-label">
                  棋盘尺寸
                </span>
                  <select class="select" v-model="formState.boardSize">
                    <option v-for="item in BoardSizeConstant" :value="item.value">{{ item.label }}</option>
                  </select>
                </div>
              </div>
              <div class="row gap-8" v-if="formState.type !== 'NONE'">
                <div class="form-group col">
                  <span class="form-label">
                    {{ formState.type === 'SHORT' ? '各方限时（分钟）' : '每步允许天数' }}
                  </span>
                  <input class="range" type="range" v-model="formState.duration" min="0" max="38"/>
                </div>
                <div class="form-group col" v-if="formState.type === 'SHORT'">
                  <span class="form-label" style="text-align: right; width: 100%">
                    每步加时（秒）
                  </span>
                  <input class="range" type="range" v-model="formState.stepDuration" min="0" max="30"/>
                </div>
              </div>
              <span class="form-label" v-else>请随意安排时间</span>
            </div>
          </div>
          <button class="button">A</button>
        </form>
      </div>
    </div>
  </transition>
</template>

<style scoped lang="less">
@import "@/assets/css/variable.less";

.play-drawer {
  background-color: #fff;
  position: absolute;
  top: 100%;
  z-index: @headerZIndex - 1;
  height: fit-content;
  overflow: hidden;
  width: 100%;
  box-shadow: 0 30px 28px 8px rgba(0, 0, 0, 0.05);

  .drawer-content-wrapper {
    max-width: 1000px;
    margin: 0 auto;
    padding: 1rem;
    height: 100%;
  }

  .mode-box {
    display: flex;
    height: 100%;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
  }

  .mode-selected {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    width: 100%;
    text-align: center;

    .item {
      flex: 1;
      padding: 1rem;
      border: 2px solid transparent;
      cursor: pointer;
      border-radius: @borderRadius;
      white-space: nowrap;
      overflow: hidden;

      .mode-name {
        font-weight: bolder;
      }

      .mode-doc {
        font-size: calc(1rem - 2px);
        color: #1e293b;
        margin-top: 8px;
      }

      &:hover {
        background-color: rgba(245, 63, 63, 0.05);
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
        border: 2px solid @c-orange;
      }
    }
  }

  .tip {
    font-size: 14px;
    color: #717171;
    margin-top: 1rem;
  }

  .form {
    width: 100%;
    margin: 1rem auto;

    .row {
      width: 100%;
    }

    .form-label {
      display: inline-block;
      margin-bottom: .5rem;
    }

    .form-left {
      min-width: 250px;
    }
  }

  .tabs-horiz {
    width: 250px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    button {
      width: 100%;
      user-select: none;
      background-color: white;
      flex: 1;
      font-size: 14px;
      text-align: center;
      padding: .8em .2em;
      cursor: pointer;
      border: none;
      white-space: nowrap;
      overflow: hidden;
      font-weight: bolder;
      margin: .2rem 0;
      border-left: 3px solid transparent;
      color: @c-dark-light;

      &.active {
        color: @c-dark;
        background-color: rgba(0, 0, 0, 0.05);
        border-color: @c-primary;
      }

      &:hover {
        color: @c-dark;
        background-color: rgba(0, 0, 0, 0.05);
      }
    }
  }

  .range {
    cursor: pointer;
    width: 100%;
    -webkit-appearance: none;
    appearance: none;
    background-color: rgba(0, 0, 0, 0.05);
    border: 0;
    color: #000;
    border-radius: 7px;
  }

  input[type="range"]::-webkit-slider-thumb {
    -webkit-appearance: none;
    appearance: none;
    width: 20px;
    height: 14px;
    border-radius: 6px;
    background: #fff;
    cursor: pointer;
    border: 1px solid rgba(0, 0, 0, 0.2);
  }
}

.v-enter-active,
.v-leave-active {
  transition: height 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  height: 0;
}
</style>
