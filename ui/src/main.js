import './assets/css/index.less'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
  
import App from './App.vue'
import router from './router'
import Icon from './components/icon/Icon.vue'

import 'virtual:svg-icons-register'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.component('Icon', Icon)

app.mount('#app')


