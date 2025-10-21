<script setup lang="ts">

const { color, size } = defineProps({
  color: {
    type: String,
    default: '#000'
  },
  size: {
    type: String,
    default: '32px'
  }
})

</script>

<template>
  <div class="loading">
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 45.714285714285715 45.714285714285715"
         :style="{transform: 'rotate(calc(-90deg))', color, width: size, height: size}">
      <circle class="underlay" fill="transparent" cx="50%" cy="50%" r="20"
              stroke-width="5.714285714285714" stroke-dasharray="125.66370614359172" stroke-dashoffset="0"></circle>
      <circle class="overlay" fill="transparent" cx="50%" cy="50%" r="20"
              stroke-width="5.714285714285714" stroke-dasharray="125.66370614359172"
              stroke-dashoffset="125.66370614359172px"></circle>
    </svg>
  </div>
</template>

<style scoped lang="less">

svg {
  animation: progress-circular-rotate 1.4s linear infinite;
  transform-origin: center center;
  transition: all .2s ease-in-out;
}

.underlay {
  stroke: rgba(0, 0, 0, 0.12);
  z-index: 1;
}

.overlay {
  stroke: currentColor;;
  transition: all .2s ease-in-out, stroke-width 0s;
  z-index: 2;
  animation: progress-circular-dash 1.4s ease-in-out infinite, progress-circular-rotate 1.4s linear infinite;
  stroke-dasharray: 25, 200;
  stroke-dashoffset: 0;
  stroke-linecap: round;
  transform-origin: center center;
  transform: rotate(-90deg);
}

@keyframes progress-circular-rotate {
  100% {
    transform: rotate(270deg);
  }
}

@keyframes progress-circular-dash {
  0% {
    stroke-dasharray: 1, 200;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 100, 200;
    stroke-dashoffset: -15px;
  }
  100% {
    stroke-dasharray: 100, 200;
    stroke-dashoffset: -124px;
  }
}
</style>