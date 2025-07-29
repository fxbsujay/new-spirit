import Snackbar from './index.vue'
import { createVNode, render, nextTick } from 'vue'

const wrapper = document.createElement('div')
wrapper.className = 'message-wrapper'
document.body.appendChild(wrapper)

let timer = null
export const snackbar = (message, type, timeout = 3000) => {

    const vm = createVNode(Snackbar, {
        message,
        type,
        onVnodeMounted: () => {
            vm.component.exposed.open()
        },
        onVnodeUnmounted: () => {
            clearTimeout(timer)
            timer = null
        }
    })

    render(vm, wrapper)
    timer = setTimeout(() => {
        vm.component.exposed.close()
        nextTick(() => render(null, wrapper))
    }, timeout)
}

export default {
    info: text => snackbar(text, 'info'),
    success: text => snackbar(text, 'success'),
    warning: text => snackbar(text, 'warning'),
    error: text => snackbar(text, 'error'),
}