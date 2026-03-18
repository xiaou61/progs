import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { pinia } from './stores/pinia'
import { useAdminSessionStore } from './stores/admin-session'

const app = createApp(App)

app.use(pinia)
useAdminSessionStore(pinia).hydrate()
app.use(router)
app.mount('#app')
