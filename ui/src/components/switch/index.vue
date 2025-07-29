<script setup>
const value = defineModel()

</script>

<template>
  <div class="switch-wrap" :class="value ? 'active' : ''" >
    <div class="switch-track" @click="value = !value"></div>
    <div class="switch-input">
      <input type="checkbox" v-model="value" />
      <div class="switch-thumb"></div>
    </div>
  </div>
</template>

<style scoped lang="less">

.switch-wrap {
  --height: 22px;
  display: inline-flex;
  align-items: center;
  position: relative;
  justify-content: center;
  flex: none;
  width: auto;
  height: var(--height);
  opacity: 1;

  &.active {
    .switch-track {
      background-color: #21C210;
    }
    .switch-input {
      transform: translateX(calc(var(--height) / 2)) scale(.9);
    }
  }

  .switch-track {
    border-radius: 999px;
    height: 100%;
    width: calc(var(--height) * 2);
    display: inline-flex;
    align-items: center;
    padding: 0 5px;
    background-color: #e6e5e5;
    cursor: pointer;
    transition: .15s .1s background-color cubic-bezier(0,0,.2,1);
  }

  .switch-input  {
    border-radius: 50%;
    transition: .2s transform cubic-bezier(.4,0,.2,1);
    position: absolute;
    width: var(--height);
    height: var(--height);
    align-items: center;
    display: flex;
    flex: none;
    justify-content: center;
    transform: translateX(calc(var(--height) / -2)) scale(.7);

    input {
      cursor: pointer;
      position: absolute;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      opacity: 0;
    }

    .switch-thumb {
      transform: none;
      transition: .15s .05s transform cubic-bezier(0,0,.2,1);
      align-items: center;
      background-color: #fff;
      color: #000;
      border-radius: 50%;
      display: flex;
      font-size: .75rem;
      height: var(--height);
      justify-content: center;
      width: var(--height);
      pointer-events: none;
      position: relative;
      overflow: hidden;
    }
  }
}
</style>