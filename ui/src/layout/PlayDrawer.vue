<script setup>
const props = defineProps({
  visible: Boolean,
})
</script>

<template>
  <div class="play-drawer" :class="props.visible ? 'open' : ''">
    <div class="drawer-content-wrapper">
      <div class="mode-selected">
        <div class="item">休闲赛</div>
        <div class="item">积分赛</div>
        <div class="item">人机对战</div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import "@/assets/css/variable.less";

.play-drawer {
  background-color: #fff;
  position: absolute;
  top: 100%;
  z-index: @headerZIndex - 1;
  height: 0;
  overflow: hidden;
  transition: height .3s ease-in-out;
  width: 100%;
  box-shadow: 0 30px 28px 8px rgba(0, 0, 0, 0.05);

  &.open {
    height: 30vh;
  }

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
</style>