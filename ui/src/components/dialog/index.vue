<template>
  <div class="dialog-modal" v-if="props.visible">
    <div class="mask" @click="emits('close')"></div>
    <div class="content">
      <slot/>
    </div>
  </div>
</template>
<script setup>
const props = defineProps({
  visible: Boolean,
})
const emits = defineEmits(['close'])
</script>

<style scoped lang="less">

.dialog-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2400;
  padding: 1rem;

  .mask {
    pointer-events: auto;
    background: #000;
    border-radius: inherit;
    bottom: 0;
    left: 0;
    opacity: 0.32;
    position: fixed;
    right: 0;
    top: 0;
  }

  .content {
    z-index: 0;
    background-color: #fff;
    padding: 1rem;
    margin: 1rem;
    max-width: 100%;
    border-radius: 4px;
  }
}
</style>