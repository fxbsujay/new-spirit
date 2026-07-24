import './assets/css/index.less'
import { icons} from '@/assets/icons/icon.js'
import { createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import { createApp } from 'vue'

import App from './App.vue'
import Icon from './components/icon/Icon.vue'
import router from './router'

import 'vuetify/styles'
import 'virtual:svg-icons-register'

const app = createApp(App)

app.use(createVuetify({ icons }))

app.use(createPinia())
app.use(router)

app.component('Icon', Icon)

app.mount('#app')


