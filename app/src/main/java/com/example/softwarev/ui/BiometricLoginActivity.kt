package com.example.softwarev.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.example.softwarev.MainActivity
import com.example.softwarev.R
import com.example.softwarev.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BiometricLoginActivity : AppCompatActivity() {

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric_login)

        userPrefs = UserPreferences(this)

        val btnTryAgain = findViewById<Button>(R.id.btnTryAgain)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        setupBiometricPrompt()

        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> showBiometricPrompt()
            else -> {
                Toast.makeText(this, "Biometría no disponible en este dispositivo", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        btnLogout.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                userPrefs.clearToken()
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@BiometricLoginActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        btnTryAgain.setOnClickListener { showBiometricPrompt() }
    }

    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "Autenticación exitosa ✅", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@BiometricLoginActivity, MainActivity::class.java))
                finish()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Huella no reconocida ❌", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Inicio de sesión biométrico")
            .setSubtitle("Usa tu huella para acceder")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    private fun showBiometricPrompt() {
        biometricPrompt.authenticate(promptInfo)
    }
}
