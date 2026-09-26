package com.example.messagetesttool

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var panelView: View? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val bubbleParams = WindowManager.LayoutParams(
            56,
            56,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        bubbleParams.gravity = Gravity.TOP or Gravity.START
        bubbleParams.x = 30
        bubbleParams.y = 250

        bubbleView = LayoutInflater.from(this)
            .inflate(R.layout.floating_bubble, null)

        bubbleView?.setOnClickListener {
            showPanel()
        }

        windowManager.addView(bubbleView, bubbleParams)
    }

    private fun showPanel() {
        if (panelView != null) return

        val params = WindowManager.LayoutParams(
            300,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER

        panelView = LayoutInflater.from(this)
            .inflate(R.layout.floating_panel, null)

        panelView?.findViewById<Button>(R.id.btnClose)?.setOnClickListener {
            windowManager.removeView(panelView)
            panelView = null
        }

        windowManager.addView(panelView, params)
    }

    override fun onDestroy() {
        panelView?.let {
            windowManager.removeView(it)
        }

        bubbleView?.let {
            windowManager.removeView(it)
        }

        panelView = null
        bubbleView = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
