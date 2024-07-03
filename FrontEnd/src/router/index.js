import { createRouter, createWebHistory } from 'vue-router';
import {useUserStore} from "@/stores/user.js";

const whiteList = ['/login','/register'] // no redirect whitelist


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/user',
      name: 'User',
      redirect: '/user/report/submit',
      component: () => import('../views/MainView.vue'),
      children: [
        {
          path: 'report',
          name: 'Report',
          children: [
            {
              path: 'submit',
              name: 'ReportSubmit',
              component: () => import('../views/user/reports/Submit.vue')
            },
            {
              path: 'result',
              name: 'ReportResult',
              component: () => import('../views/user/reports/LabResult.vue')
            }
          ]
        }
      ]
    },
    {
      path: '/admin',
      name: 'Admin',
      redirect: '/admin/reports/publish',
      component: () => import('../views/MainView.vue'),
      children: [
        {
          path: 'reports',
          name: 'Reports',
          children: [
            {
              path: 'publish',
              name: 'ReportsPublish',
              component: () => import('../views/admin/reports/Publish.vue')
            },
            {
              path: 'published',
              name: 'ReportsPublished',
              component: () => import('../views/admin/reports/Published.vue')
            },
            {
              path: 'submitted',
              name: 'ReportsSubmitted',
              component: () => import('../views/admin/reports/Submitted.vue')
            }
          ]
        },

        {
          path: 'sections',
          name: 'Sections',
          children: [
            {
              path: 'add',
              name: 'SectionsAdd',
              component: () => import('../views/admin/sections/Add.vue')
            },
            {
              path: 'edit',
              name: 'SectionsEdit',
              component: () => import('../views/admin/sections/Edit.vue')
            }
          ]
        },

        {
          path: 'students',
          name: 'Students',
          children: [
            {
              path: 'add',
              name: 'StudentsAdd',
              component: () => import('@/views/admin/students/Add.vue')
            },
            {
              path: 'edit',
              name: 'StudentsEdit',
              component: () => import('@/views/admin/students/Edit.vue')
            }
          ]
        },
        {
          path: 'templates',
          name: 'Templates',
          children: [
            {
              path: 'add',
              name: 'TemplatesAdd',
              component: () => import('../views/admin/templates/Add.vue')
            },
            {
              path: 'edit',
              name: 'TemplatesEdit',
              component: () => import('../views/admin/templates/Edit.vue')
            }
          ]
        },
        {
          path: 'statistics',
          name: 'Statistics',
          component: () => import('../views/admin/statistics/Display.vue')
        },


      ]
    }

  ]
})

router.beforeEach(async(to, from, next) => {
  // set page title
  // document.title = getPageTitle(to.meta.title)

  if (useUserStore().hasFullInfo()) {
    const role = useUserStore().role
    if (to.path === '/login') {
      role === 'ROLE_USER' ? next({ path: '/user' }) : next({ path: '/admin' })
    } else {
      if (to.path.includes('/admin') && role === 'ROLE_USER') {
        next({ path: '/user' })
      }
      else if (to.path.includes('/user') && role === 'ROLE_ADMIN') {
        next({ path: '/admin' })
      }
      else{
        next()
      }
    }
  } else {
    useUserStore().logout()
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
