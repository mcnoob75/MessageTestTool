package com.example.messagetesttool

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnRequestOverlay
        ).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permissão já ativada!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnOpenAccessibility
        ).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnStartFloating
        ).setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, FloatingService::class.java))
            } else {
                Toast.makeText(
                    this,
                    "Ative a permissão de janela flutuante primeiro.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
