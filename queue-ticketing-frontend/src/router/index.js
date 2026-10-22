import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    redirect: '/console'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/console',
    name: 'CallConsole',
    component: () => import('../views/CallConsole.vue')
  },
  {
    path: '/config',
    name: 'StoreConfig',
    component: () => import('../views/StoreConfig.vue')
  },
  {
    path: '/simulator',
    name: 'TakeTicketSimulator',
    component: () => import('../views/TakeTicketSimulator.vue')
  },
  {
    path: '/h5',
    name: 'ClientTake',
    component: () => import('../views/ClientTake.vue')
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
  if (!localStorage.getItem('token')) {
    return { path: '/login' };
  }
  return true;
});

export default router;
