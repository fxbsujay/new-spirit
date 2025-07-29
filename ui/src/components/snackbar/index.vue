<template>
  <transition name="message-fade">
    <div class="message-box" v-if="visible" :class="[typeClass]">
      <span class="message-content">{{ message }}</span>
    </div>
  </transition>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  message: String,
  type: {
    type: String,
    default: 'info',
    validator: v => ['info', 'success', 'warning', 'error'].includes(v)
  }
})


const visible = ref(false)
const typeClass = computed(() => `message-${props.type}`)

const close = () => {
  visible.value = false
}
const open = () => {
  visible.value = true
}

defineExpose({ open, close })
</script>

<style scoped>
.message-box {
  position: fixed;
  top: 8%;
  left: 50%;
  min-width: 300px;

  transform: translateX(-50%);
  padding: 12px 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  z-index: 9999;
  font-size: 14px;
  color: rgb(238, 238, 238);
  background-color: #424242;
}

.message-info {
  background-color: #424242;
}

.message-success {
  background-color: #67c23a;
}
.message-warning {
  background-color: #F57C00;
}
.message-error {
  background-color: #D1392E;
}

.message-fade-enter-active,
.message-fade-leave-active {
  transition: all 0.3s ease;
}

.message-fade-enter-from,
.message-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}
</style>