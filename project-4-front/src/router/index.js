
import Vue from 'vue'
import VueRouter from 'vue-router'
import axios from 'axios'

import HomeView from '../views/Home.vue'
import RecipesView from '../views/Recipes.vue'
import ProductionView from '../views/Production.vue'
import AboutView from '../views/About.vue'
import LoginView from '../views/LoginView.vue'
import StockView from '../views/Stock.vue'

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
        component: RecipesView,
        meta: { requiresAuth: true }
    },
    {
        path: '/production',
        name: 'Production',
        component: ProductionView,
        meta: { requiresAuth: true }
    },
    {
        path: '/about',
        name: 'About',
        component: AboutView
    },
    {
        path: '/login',
        name: 'Login',
        component: LoginView
    },
    {
        path: '/stock',
        name: 'Stock',
        component: StockView,
        meta: { requiresAuth: true }
    },
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

// Controleer of de gebruiker ingelogd is
router.beforeEach(async (to, from, next) => {
    if (!to.matched.some(route => route.meta.requiresAuth)) {
        return next()
    }

    try {
        const response = await axios.get(
            'http://localhost:8080/recipes/whoami',
            {
                withCredentials: true,
                headers: {
                    Accept: 'application/json'
                }
            }
        )

        console.log('Logincontrole:', response.data)

        // Controleer of we echte gebruikersgegevens ontvangen
        const user = response.data

        if (
            !user ||
            typeof user !== 'object' ||
            !user.username
        ) {
            return next({
                path: '/login',
                query: { redirect: to.fullPath }
            })
        }

        next()
    } catch (error) {
        console.error('Logincontrole mislukt:', error)

        next({
            path: '/login',
            query: { redirect: to.fullPath }
        })
    }
})

export default router
