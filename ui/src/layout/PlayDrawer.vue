<script setup>
import Icon from '@/components/icon/Icon.vue'

const props = defineProps({
  visible: Boolean,
})
</script>

<template>
  <transition>
    <div class="play-drawer" v-if="visible">
      <div class="drawer-content-wrapper">
        <div class="mode-selected">
          <div class="item">
            <Icon name="game-mode-1" size="3rem" color="#1f1f1f"/>
            <div class="mode-name">休闲赛</div>
            <div class="mode-doc">轻松对局，不计积分，享受围棋乐趣</div>
          </div>
          <div class="item">
            <Icon name="game-mode-2" size="3rem" color="#1f1f1f"/>
            <div class="mode-name">积分赛</div>
            <div class="mode-doc">正式对局，累计积分，提升段位等级</div>
          </div>
          <div class="item">
            <Icon name="game-mode-3" size="3rem" color="#1f1f1f"/>
            <div class="mode-name">人机对战</div>
            <div class="mode-doc">与AI对弈，多个难度等级可选</div>
          </div>
          <div class="item">
            <Icon name="game-mode-4" size="3rem" color="#1f1f1f"/>
            <div class="mode-name">同屏对战</div>
            <div class="mode-doc">同一设备，双人对弈，无需联网</div>
          </div>
        </div>
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
  height: 30vh;
  overflow: hidden;
  width: 100%;
  box-shadow: 0 30px 28px 8px rgba(0, 0, 0, 0.05);

  .drawer-content-wrapper {
    max-width: @contentWidth;
    margin: 0 auto;
    padding: 1rem;
  }

  .mode-selected {
    display: flex;
    align-items: center;
    justify-content: center;

    .item {
      padding: 1rem;
      text-align: center;

      .mode-name {
        font-weight: bolder;
      }

      .mode-doc {
        font-size: calc(1rem - 2px);
        color: #1e293b;
      }
    }


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
