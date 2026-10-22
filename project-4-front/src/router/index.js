import Vue from 'vue'
import VueRouter from 'vue-router'
import RecipesView from '../views/Recipes.vue'

Vue.use(VueRouter);

const routes = [
    {
        path: '/',
        name: 'Recipes',
        component: RecipesView
    }
];

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
});

export default router