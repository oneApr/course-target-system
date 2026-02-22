import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/ForgotPassword.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        component: () => import('@/layouts/MainLayout.vue'),
        meta: { requiresAuth: true },
        children: [
            { path: '', redirect: '/dashboard' },
            { path: '/dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '首页' } },
            { path: '/course-objectives', name: 'CourseObjectives', component: () => import('@/views/CourseObjectives.vue'), meta: { title: '课程目标管理', role: 'director' } },
            { path: '/objective-maintenance', name: 'ObjectiveMaintenance', component: () => import('@/views/ObjectiveMaintenance.vue'), meta: { title: '课程目标维护', role: 'director' } },
            { path: '/course-task-management', name: 'CourseTaskManagement', component: () => import('@/views/CourseTaskManagement.vue'), meta: { title: '课程任务管理', role: 'director' } },
            { path: '/course-tasks', name: 'CourseTaskView', component: () => import('@/views/CourseTaskView.vue'), meta: { title: '课程任务查看' } },
            { path: '/teacher-management', name: 'TeacherManagement', component: () => import('@/views/TeacherManagement.vue'), meta: { title: '教师信息管理', role: 'director' } },
            { path: '/upload', name: 'DataUpload', component: () => import('@/views/DataUpload.vue'), meta: { title: '达成数据上传' } },
            { path: '/data-audit', name: 'DataAudit', component: () => import('@/views/DataAudit.vue'), meta: { title: '达成数据审核', role: 'director' } },
            { path: '/query', name: 'AchievementQuery', component: () => import('@/views/AchievementQuery.vue'), meta: { title: '达成情况查询' } },
            { path: '/visualization', name: 'Visualization', component: () => import('@/views/Visualization.vue'), meta: { title: '数据可视化', role: 'director' } },
        ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const isAuthenticated = userStore.isLoggedIn

    if (to.meta.requiresAuth === false) {
        if (isAuthenticated && to.path === '/login') {
            next('/dashboard')
        } else {
            next()
        }
        return
    }

    if (!isAuthenticated) {
        next('/login')
        return
    }

    if (to.meta.role && userStore.role !== to.meta.role) {
        next('/dashboard')
        return
    }

    next()
})

export default router
