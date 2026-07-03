import './assets/css/index.less'
import { createPinia } from 'pinia'

import { createApp } from 'vue'

import App from './App.vue'
import Icon from './components/icon/Icon.vue'
import router from './router'

import 'virtual:svg-icons-register'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.component('Icon', Icon)

app.mount('#app')


