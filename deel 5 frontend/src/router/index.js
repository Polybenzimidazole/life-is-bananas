
import Vue from 'vue'
import VueRouter from 'vue-router'
import { getAccessToken, login } from '../services/auth'

import HomeView from '../views/Home.vue'
import RecipesView from '../views/Recipes.vue'
import ProductionView from '../views/Production.vue'
import AboutView from '../views/About.vue'
import StockView from '../views/Stock.vue'
import AssistantView from '../views/Assistant.vue'

// Nieuwe pagina voor OAuth 2.0 + PKCE
import CallbackView from '../views/Callback.vue'

Vue.use(VueRouter)

const routes = [
    {
        path: '/assistant',
        name: 'Assistant',
        component: AssistantView
    },
    {
        path: '/',
        name: 'Home',
        component: HomeView
    },
    {
        path: '/recipes',
        name: 'Recipes',
        component: RecipesView
    },
    {
        path: '/production',
        name: 'Production',
        component: ProductionView
    },
    {
        path: '/about',
        name: 'About',
        component: AboutView
    },
    {
        path: '/stock',
        name: 'Stock',
        component: StockView
    },
    {
        path: '/callback',
        name: 'Callback',
        component: CallbackView
    }
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

router.beforeEach(async (to, from, next) => {
    if (['/recipes', '/production', '/stock', '/assistant'].includes(to.path) && !getAccessToken()) {
        sessionStorage.setItem('auth_return_path', to.fullPath)
        try {
            await login()
            next(false)
        } catch (error) {
            console.error('Unable to start sign-in:', error)
            next('/')
        }
        return
    }
    next()
})

export default router
