package com.jiayou.shanghai

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DingDongService : AccessibilityService() {
    var currentClassName: String = ""
    var chooseTimeSuccess: Boolean = false
    var enableJumpCart: Boolean = true
    var checkNotificationCount: Int = 0;

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d(TAG, "event: $event")
        event?.runCatching {
            handleClassName(this)
            when (currentClassName) {
                CART_ACTIVITY -> {
                    pay(this)
                }
                HOME_ACTIVITY -> {
                    jumpToCartActivity(this)
                }
                CHOOSE_DELIVERY_TIME, CHOOSE_DELIVERY_TIME_V2 -> {
                    chooseDeliveryTime(this)
                }
                GX0 -> {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
                XN1 -> {
                    checkNotification(this)
                }
                RETURN_CART_DIALOG -> {
                    clickReturnCartBtn(this)
                }
                W90 -> {
                    setPassword(this)
                }
                else -> {
                    clickDialog(this)
                }
            }
        }?.onFailure {
            Log.e(TAG, "发生错误", it)
        }
    }

    private fun handleClassName(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        currentClassName = event.className as String
        Log.d(TAG, "currentClassName: $currentClassName")
        if (currentClassName in listOf(CART_ACTIVITY, CHOOSE_DELIVERY_TIME, CHOOSE_DELIVERY_TIME_V2)) {
            enableJumpCart = true
        }
        if (currentClassName == HOME_ACTIVITY) {
            checkNotificationCount = 0
        }
    }

    private fun clickReturnCartBtn(event: AccessibilityEvent) {
        var nodes = event.source?.findAccessibilityNodeInfosByText("返回购物车")
        nodes?.forEach { node ->
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "clickDialog confirm")
            return@forEach
        }
    }

    private fun checkNotification(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            if (checkNotificationCount ++ > 1) {
                Log.d(TAG, "checkNotificationCount: $checkNotificationCount, return")
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        } else {
            checkNotificationCount = 0
        }
    }

    private fun pay(event: AccessibilityEvent) {
        var nodes = event.source?.findAccessibilityNodeInfosByText("立即支付")
        nodes?.forEach { node ->
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "onClick pay button")
        }
    }

    private fun chooseDeliveryTime(event: AccessibilityEvent) {
        Log.d(TAG, "chooseDeliveryTime: ${event.source}")
        var nodes = event.source?.findAccessibilityNodeInfosByText("-")
        nodes?.forEach { node ->
            if (node.parent.isEnabled) {
                chooseTimeSuccess = true
                node.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "onClick chooseDeliveryTime")
                return@forEach
            }
        }
        if (!chooseTimeSuccess) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            GlobalScope.launch {
                delay(100)
                if (currentClassName in listOf(CHOOSE_DELIVERY_TIME, CHOOSE_DELIVERY_TIME_V2)) {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
            }
        }
    }

    private fun jumpToCartActivity(event: AccessibilityEvent) {
        if (!enableJumpCart) {
            Log.d(TAG, "enableJumpCart: $enableJumpCart, return")
            return
        }
        event.source?.findAccessibilityNodeInfosByText("全选")
            ?.forEach {
                if (!it.parent.getChild(0).isChecked) {
                    it.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d(TAG, "onClick select all")
                }
            }
        event.source?.findAccessibilityNodeInfosByText("去结算")
            ?.forEach { node ->
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                enableJumpCart = false
                Log.d(TAG, "onClick jump to cart")
                return@forEach
            }
    }

    private fun setPassword(event: AccessibilityEvent) {
        event.source?.findAccessibilityNodeInfosByText("余额支付")
            ?.takeIf {
                it.isNotEmpty()
            }?.let {
                it[0].parent
            }?.takeIf {
                it.childCount > 3
            }?.getChild(3)?.apply {
                val arguments = Bundle()
                arguments.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    Card.getPassword(applicationContext)
                )
                performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            }
        Log.d(TAG, "onSet password")
    }

    private fun clickDialog(event: AccessibilityEvent) {
        var nodes = event.source?.findAccessibilityNodeInfosByText("继续支付")
        if (nodes == null) {
            nodes = event.source?.findAccessibilityNodeInfosByText("修改送达时间")
        }
        nodes?.forEach { node ->
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d(TAG, "clickDialog confirm")
            return@forEach
        }
    }

    override fun onInterrupt() {
    }

    companion object {
        const val TAG = "DingDongService"
        const val HOME_ACTIVITY = "com.yaya.zone.home.HomeActivity"
        const val CART_ACTIVITY = "cn.me.android.cart.activity.WriteOrderActivity"
        const val CHOOSE_DELIVERY_TIME = "gy"
        const val GX0 = "gx0"
        const val RETURN_CART_DIALOG = "by"
        const val XN1 = "xn1"
        const val CHOOSE_DELIVERY_TIME_V2 = "iy"
        const val W90 = "w90"
    }
}