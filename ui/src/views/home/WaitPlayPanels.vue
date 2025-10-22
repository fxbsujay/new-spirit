<script setup lang="ts">
import Loading from '@/components/loading/index.vue'
</script>

<template>
  <div class="play-panels">
    <div class="panels">
      <Loading color="#fff" size="24px"/>
      <div class="info">
        <div>
          <span class="type">积分赛</span>
          <span class="text">21x21•10h+6s </span>
        </div>
        <span class="code">#A2CE4</span>
      </div>
      <div class="time">
        00:42
      </div>
    </div>
    <button class="button border">取消</button>
  </div>
</template>

<style scoped lang="less">
.play-panels {
  position: fixed;
  top: 0;
  left: 50%;
  z-index: 100;
  transform: translateX(-50%);
  cursor: pointer;

  &:hover {
    .panels {
      height: 90px;
      .text, .code {
        opacity: 1;
        height: auto;
      }
      .time {
        font-size: 14px;
      }
    }
    .button {
      height: 30px;
      opacity: 1;
      transition: opacity 0.5s 0.3s ease;
    }
  }
}

.panels {
  background-color: #3E7DD6;
  padding: 8px 10px;
  width: 250px;
  color: #fff;
  border-radius:  0 0 4px 4px;
  display: flex;
  height: 40px;
  transition: height 0.5s ease;

  .info {
    margin-left: 8px;
    margin-right: auto;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .type {
    font-size: 18px;
    line-height: 0;
  }

  .text, .code {
    display: block;
    font-size: 14px;
    opacity: 0;
    height: 0;
    transition: opacity 0.5s ease;
  }

  .code {
    margin-top: auto;
    transition-delay: 0.2s;
  }

  .time {
    margin-top: auto;
    font-size: 16px;
    transition: font-size 0.5s ease;
  }
}


.button {
  font-size: 14px;
  height: 0;
  margin-top: 6px;
  border-radius: 2px;
  border-color: #351FF2;
  opacity: 0;
}
</style>