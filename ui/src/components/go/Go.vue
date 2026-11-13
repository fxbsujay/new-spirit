<template>
  <svg
      ref="svgRef"
      version="1.1"
      xmlns="http://www.w3.org/2000/svg"
      @click="boardClickHandle"
      @mousemove="boardMoveHandler"
      @mouseleave="boardLeaveHandler"
  >
    <defs>
      <radialGradient id="shadow-black" r="1.0">
        <stop offset="0" stop-color="black" stop-opacity="1.0"></stop>
        <stop offset="30%" stop-color="black" stop-opacity="1.0"></stop>
        <stop offset="31%" stop-color="#333333" stop-opacity="0.6"></stop>
        <stop offset="34%" stop-color="#333333" stop-opacity="0.50"></stop>
        <stop offset="40%" stop-color="#333333" stop-opacity="0.5"></stop>
        <stop offset="50%" stop-color="#333333" stop-opacity="0.0"></stop>
      </radialGradient>
      <radialGradient id="shadow-white" r="1.0">
        <stop offset="0" stop-color="white" stop-opacity="1.0"></stop>
        <stop offset="30%" stop-color="white" stop-opacity="1.0"></stop>
        <stop offset="31%" stop-color="#333333" stop-opacity="0.6"></stop>
        <stop offset="34%" stop-color="#333333" stop-opacity="0.50"></stop>
        <stop offset="40%" stop-color="#333333" stop-opacity="0.5"></stop>
        <stop offset="50%" stop-color="#333333" stop-opacity="0.0"></stop>
      </radialGradient>
      <image id="white-shell" :width="boardSetting.squareSize" :height="boardSetting.squareSize" x="0" y="0"
             xlink:href="data:image/png;base64,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"></image>
      <image id="black-shell" :width="boardSetting.squareSize" :height="boardSetting.squareSize" x="0" y="0"
             xlink:href="data:image/png;base64,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"></image>
    </defs>

    <g class="coordinate-labels">
      <text v-for="item in lineNumbers" :x="item.x" :y="item.y" :font-size="`${item.fontSize}px`" font-weight="500"
            fill="#444444">{{ item.text }}
      </text>
    </g>
    <g class="shadow-layer">
      <circle
          v-for="item in points"
          :fill="`url(#shadow-${item.type})`"
          :cx="boardSetting.point.shadowSize"
          :cy="boardSetting.point.shadowSize"
          :r="boardSetting.point.shadowRadius *  0.95"
          :transform="`translate(${item.x * boardSetting.ss + boardSetting.ox - boardSetting.mid},${item.y * boardSetting.ss + boardSetting.oy - boardSetting.mid})`"
      />
    </g>
    <g class="grid">
      <g v-for="item in points"
         :transform="`translate(${item.x * boardSetting.ss + boardSetting.ox - boardSetting.mid},${item.y * boardSetting.ss + boardSetting.oy - boardSetting.mid})`">
        <use :href="`#${item.type}-shell`" :x="boardSetting.point.offset" :y="boardSetting.point.offset"></use>
      </g>
      <g v-if="hintPoint.show"
         opacity="0.5"
         :transform="`translate(${hintPoint.x * boardSetting.ss + boardSetting.ox - boardSetting.mid},${hintPoint.y * boardSetting.ss + boardSetting.oy - boardSetting.mid})`">
        <use :href="`#${hintPoint.type}-shell`" :x="boardSetting.point.offset" :y="boardSetting.point.offset"></use>
      </g>
    </g>
  </svg>
</template>
<script setup>
import { computeBoard } from '@/components/go/goban'
import { nextTick, reactive, ref, useTemplateRef, watch, onUnmounted } from 'vue'

const { onBoardClick, showHintPointFunc, points, size, label } = defineProps({
    onBoardClick: {
        type: Function
    },
    showHintPointFunc: {
        type: Function
    },
    points: {
        default: [],
        type: Array
    },
    size: {
        default: 0,
        type: Number
    },
    label: {
        default: true,
        type: Boolean
    }
})

const svg = useTemplateRef('svgRef')
const lineNumbers = ref([])
const hintPoint = reactive({
    show: false,
    type: '',
    x: 0,
    y: 0,
})

const boardSetting = reactive({
    ss: 0,
    ox: 0,
    oy: 0,
    mid: 0,
    squareSize: 0,
    w: 0,
    h: 0,
    showLabel: false,
    point: {
        shadowSize: 0,
        shadowRadius: 0,
        offset: 0
    }
})
let observer = null

onUnmounted(() => {
    observer?.disconnect()
})

const render = () => {
    if (!observer) {
        observer = new ResizeObserver((entries) => {
            for (const entry of entries) {
                render()
            }
        })
    }

    if (size) {
        nextTick(() => {
            const w = svg.value.parentNode.offsetWidth
            const h = svg.value.parentNode.offsetHeight
            const maxSize = Math.min(w, h)
            const squareSize = maxSize / (size + (label ? 2 : 0))
            const setting = computeBoard(svg, size, size, parseInt(squareSize), label)
            Object.assign(boardSetting, setting)
            const total = setting.squareSize * (setting.w + (setting.showLabel ? 2 : 0))
            svg.value.setAttribute('viewBox', `0 0 ${ total } ${ total }`)
            observer.observe(svg.value.parentNode)
        })
    }

}

const getPoint = event => {
    const rect = svg.value.getBoundingClientRect()
    const total = boardSetting.squareSize * (boardSetting.w + (boardSetting.showLabel ? 2 : 0))
    const scale = total / rect.width
    const x = parseInt(((event.clientX - rect.left) * scale - boardSetting.ox + boardSetting.mid) / boardSetting.ss)
    const y = parseInt(((event.clientY - rect.top) * scale - boardSetting.oy + boardSetting.mid) / boardSetting.ss)
    return { x, y }
}

const boardClickHandle = event => {
    if (onBoardClick) {
        const point = getPoint(event)
        onBoardClick(point.x, point.y)
    }
}

const boardMoveHandler = event => {
    if (showHintPointFunc) {
        const { x, y } = getPoint(event)
        const hint = showHintPointFunc(x, y)
        if (hint.show) {
            hintPoint.show = true
            hintPoint.type = hint.type
            hintPoint.x = x
            hintPoint.y = y
        } else {
            boardLeaveHandler()
        }
    }
}

const boardLeaveHandler = () => {
    hintPoint.show = false
    hintPoint.type = ''
    hintPoint.x = 0
    hintPoint.y = 0
}

watch(() => size, () => render())
</script>

<style scoped lang="scss">
svg {
  max-width: 100%;
  max-height: 100%;
  background-image: url(./bg.jpg);
  background-repeat: no-repeat;
  background-position: center center;
  background-size: 100% 100%;
}
</style>
