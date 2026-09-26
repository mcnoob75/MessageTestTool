package com.messagetesttool

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class TypingAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: TypingAccessibilityService? = null

        fun typeText(text: String, pressEnter: Boolean = true): Boolean {
            val service = instance ?: return false
            return service.performType(text, pressEnter)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Não precisamos processar eventos continuamente
    }

    override fun onInterrupt() {}

    private fun performType(text: String, pressEnter: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false

        var target = findFocusedEditable(root)

        if (target == null) {
            target = findFirstEditable(root)
        }

        if (target == null) {
            root.recycle()
            return false
        }

        target.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        target.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        val args = Bundle()
        args.putCharSequence(
            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
            text
        )
        val success = target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

        if (success && pressEnter) {
            clickSendButton(root)
        }

        target.recycle()
        root.recycle()
        return success
    }

    private fun findFocusedEditable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused && node.isEditable) {
            return AccessibilityNodeInfo.obtain(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findFocusedEditable(child)
            child.recycle()
            if (result != null) return result
        }
        return null
    }

    private fun findFirstEditable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable || node.className?.contains("EditText") == true) {
            return AccessibilityNodeInfo.obtain(node)
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findFirstEditable(child)
            child.recycle()
            if (result != null) return result
        }
        return null
    }

    private fun clickSendButton(root: AccessibilityNodeInfo) {
        val nodes = root.findAccessibilityNodeInfosByText("Enviar")
            ?: root.findAccessibilityNodeInfosByText("Send")
            ?: emptyList()

        for (node in nodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                node.recycle()
                return
            }
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    parent.recycle()
                    node.recycle()
                    return
                }
                val next = parent.parent
                parent.recycle()
                parent = next
            }
            node.recycle()
        }
    }
}
