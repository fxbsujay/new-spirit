<template>
  <div class="responsive">
    <div class="responsive__sizer" :style="`padding-bottom: ${aspectRatio * 100}%`"></div>
    <div class="responsive__content">
      <slot />
    </div>
  </div>
</template>

<script setup>
const { aspectRatio } = defineProps(['aspectRatio'])
</script>

<style scoped lang="less">

.responsive {
  display: flex;
  max-height: 100%;
  max-width: 100%;
  overflow: hidden;
  position: relative;

  .responsive__sizer {
    flex: 1 0 0;
    pointer-events: none;
  }

  .responsive__content {
    margin-inline-start: -100%;
    flex: 1 0 0;
    max-width: 100%;
  }

}
</style>