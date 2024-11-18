
import { createRouter, createWebHashHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import SessionsView from '../views/ProgramsView.vue';

const routes = [
  {
    path: '/',
    name: 'login',
    component: LoginView
  },
  {
    path: '/programs/:name',
    name: 'programs',
    component: SessionsView
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
});

export default router;