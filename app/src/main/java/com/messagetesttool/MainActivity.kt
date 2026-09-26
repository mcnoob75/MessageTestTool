package com.messagetesttool

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.messagetesttool.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRequestOverlay.setOnClickListener {
            requestOverlayPermission()
        }

        binding.btnOpenAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }

        binding.btnStartFloating.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Ative a permissão de janela flutuante primeiro", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (!isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "Ative o serviço de Acessibilidade primeiro", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            startFloatingService()
        }

        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val overlayOk = Settings.canDrawOverlays(this)
        binding.tvOverlayStatus.text = if (overlayOk) {
            "Overlay: ✅ Liberado"
        } else {
            "Overlay: ❌ Precisa liberar"
        }
        binding.tvOverlayStatus.setTextColor(
            if (overlayOk) 0xFFA6E3A1.toInt() else 0xFFF38BA8.toInt()
        )

        val accOk = isAccessibilityServiceEnabled()
        binding.tvAccessibilityStatus.text = if (accOk) {
            "Acessibilidade: ✅ Ativada"
        } else {
            "Acessibilidade: ❌ Precisa ativar"
        }
        binding.tvAccessibilityStatus.setTextColor(
            if (accOk) 0xFFA6E3A1.toInt() else 0xFFF38BA8.toInt()
        )
    }

    private fun requestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Já está liberado", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        Toast.makeText(
            this,
            "Procure por \"Message Test Tool\" e ative",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = "\( packageName/ \){TypingAccessibilityService::class.java.canonicalName}"
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            if (splitter.next().equals(expected, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun startFloatingService() {
        val intent = Intent(this, FloatingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Bolinha flutuante iniciada", Toast.LENGTH_SHORT).show()
        moveTaskToBack(true)
    }
}
