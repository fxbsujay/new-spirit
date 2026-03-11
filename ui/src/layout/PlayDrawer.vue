<script setup>
const props = defineProps({
  visible: Boolean,
})
</script>

<template>
  <transition>
    <div class="play-drawer" v-if="visible">
      <div class="drawer-content-wrapper">
        <div class="mode-selected">
          <div class="item">休闲赛</div>
          <div class="item">积分赛</div>
          <div class="item">人机对战</div>
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
