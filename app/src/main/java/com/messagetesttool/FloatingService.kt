package com.messagetesttool

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicBoolean

class FloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var panelView: View? = null

    private val handler = Handler(Looper.getMainLooper())
    private val isRunning = AtomicBoolean(false)
    private val stopRequested = AtomicBoolean(false)

    private var processed = 0
    private var total = 0

    private var etMessage: EditText? = null
    private var etQuantity: EditText? = null
    private var etInterval: EditText? = null
    private var tvCounter: TextView? = null
    private var tvStatus: TextView? = null
    private var btnStart: Button? = null
    private var btnStop: Button? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForegroundNotification()
        showBubble()
    }

    private fun startForegroundNotification() {
        val channelId = "floating_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Message Test Tool",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Message Test Tool")
            .setContentText("Bolinha flutuante ativa")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun showBubble() {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        bubbleView = inflater.inflate(R.layout.floating_bubble, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        bubbleView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var moved = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        moved = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved = true
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(bubbleView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!moved) {
                            togglePanel(params)
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(bubbleView, params)
    }

    private fun togglePanel(bubbleParams: WindowManager.LayoutParams) {
        if (panelView != null) {
            hidePanel()
            return
        }
        showPanel(bubbleParams)
    }

    private fun showPanel(bubbleParams: WindowManager.LayoutParams) {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        panelView = inflater.inflate(R.layout.floating_panel, null)

        etMessage = panelView?.findViewById(R.id.etMessage)
        etQuantity = panelView?.findViewById(R.id.etQuantity)
        etInterval = panelView?.findViewById(R.id.etInterval)
        tvCounter = panelView?.findViewById(R.id.tvCounter)
        tvStatus = panelView?.findViewById(R.id.tvStatus)
        btnStart = panelView?.findViewById(R.id.btnStart)
        btnStop = panelView?.findViewById(R.id.btnStop)
        val btnClose = panelView?.findViewById<Button>(R.id.btnClose)

        etMessage?.setText("Olá! Mensagem de teste.")

        btnStart?.setOnClickListener { startTyping() }
        btnStop?.setOnClickListener { stopTyping() }
        btnClose?.setOnClickListener { hidePanel() }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bubbleParams.x
            y = bubbleParams.y + 70
        }

        windowManager.addView(panelView, params)
    }

    private fun hidePanel() {
        panelView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        panelView = null
    }

    private fun startTyping() {
        if (isRunning.get()) return

        val message = etMessage?.text?.toString()?.trim().orEmpty()
        if (message.isEmpty()) {
            Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show()
            return
        }

        val qty = etQuantity?.text?.toString()?.toIntOrNull() ?: 0
        val intervalMs = etInterval?.text?.toString()?.toIntOrNull() ?: 500

        if (qty < 1) {
            Toast.makeText(this, "Quantidade inválida", Toast.LENGTH_SHORT).show()
            return
        }

        if (TypingAccessibilityService.instance == null) {
            Toast.makeText(
                this,
                "Serviço de Acessibilidade não está ativo",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        total = qty
        processed = 0
        stopRequested.set(false)
        isRunning.set(true)

        btnStart?.isEnabled = false
        btnStop?.isEnabled = true
        updateUiStatus("Executando")
        updateCounter()

        hidePanel()

        Thread {
            for (i in 1..qty) {
                if (stopRequested.get()) break

                val ok = TypingAccessibilityService.typeText(message, pressEnter = true)
                processed = i

                handler.post {
                    updateCounter()
                    if (!ok) {
                        updateUiStatus("Campo não encontrado")
                    }
                }

                if (intervalMs > 0) {
                    var remaining = intervalMs.toLong()
                    while (remaining > 0 && !stopRequested.get()) {
                        val chunk = minOf(50L, remaining)
                        Thread.sleep(chunk)
                        remaining -= chunk
                    }
                }
            }

            handler.post {
                isRunning.set(false)
                btnStart?.isEnabled = true
                btnStop?.isEnabled = false
                val finalStatus = if (stopRequested.get()) "Parado" else "Concluído"
                updateUiStatus(finalStatus)
                Toast.makeText(
                    this@FloatingService,
                    "$finalStatus — $processed/$total mensagens",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.start()
    }

    private fun stopTyping() {
        stopRequested.set(true)
        updateUiStatus("Parando...")
    }

    private fun updateCounter() {
        tvCounter?.text = "$processed / $total"
    }

    private fun updateUiStatus(text: String) {
        tvStatus?.text = text
    }

    override fun onDestroy() {
        stopRequested.set(true)
        hidePanel()
        bubbleView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        super.onDestroy()
    }
}
