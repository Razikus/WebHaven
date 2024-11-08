
import { createRouter, createWebHashHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import SessionsView from '../views/SessionsView.vue';

const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginView
  },
  {
    path: '/sessions',
    name: 'sessions',
    component: SessionsView
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export default router;