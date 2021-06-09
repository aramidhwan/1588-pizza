
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import OrderManager from "./components/OrderManager"

import StoreManager from "./components/StoreManager"
import StoreOrderManager from "./components/StoreOrderManager"

import DeliveryManager from "./components/DeliveryManager"


import MyPage from "./components/myPage"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/Order',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/Store',
                name: 'StoreManager',
                component: StoreManager
            },
            {
                path: '/StoreOrder',
                name: 'StoreOrderManager',
                component: StoreOrderManager
            },

            {
                path: '/Delivery',
                name: 'DeliveryManager',
                component: DeliveryManager
            },


            {
                path: '/myPage',
                name: 'myPage',
                component: myPage
            },


    ]
})
