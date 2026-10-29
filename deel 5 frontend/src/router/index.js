
import Vue from 'vue'
import VueRouter from 'vue-router'

import HomeView from '../views/Home.vue'
import RecipesView from '../views/Recipes.vue'
import ProductionView from '../views/Production.vue'
import AboutView from '../views/About.vue'
import StockView from '../views/Stock.vue'

// Nieuwe pagina voor OAuth 2.0 + PKCE
import CallbackView from '../views/Callback.vue'

Vue.use(VueRouter)

const routes = [
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

export default router
