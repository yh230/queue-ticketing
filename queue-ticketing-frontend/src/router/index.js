import { createRouter, createWebHistory } from 'vue-router';
import { isLoggedIn } from '../utils/auth';

const routes = [
  {
    path: '/',
    redirect: '/console'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue')
  },
  {
    path: '/console',
    name: 'CallConsole',
    component: () => import('../views/console/CallConsole.vue')
  },
  {
    path: '/config',
    name: 'StoreConfig',
    component: () => import('../views/config/StoreConfig.vue')
  },
  {
    path: '/simulator',
    name: 'TakeTicketSimulator',
    component: () => import('../views/simulator/TakeTicketSimulator.vue')
  },
  {
    path: '/h5',
    name: 'ClientTake',
    component: () => import('../views/h5/ClientTake.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  if (to.path === '/h5' || to.path === '/login') {
    return true;
  }
  if (!isLoggedIn()) {
    return { path: '/login' };
  }
  return true;
});

export default router;
