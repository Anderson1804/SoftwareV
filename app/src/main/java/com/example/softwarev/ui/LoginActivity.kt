package com.example.softwarev.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.softwarev.R
import com.example.softwarev.data.UserPreferences
import com.example.softwarev.network.LoginRequest
import com.example.softwarev.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.softwarev.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.inputEmail)
        passwordInput = findViewById(R.id.inputPassword)
        loginButton = findViewById(R.id.btnLogin)
        userPrefs = UserPreferences(this)

        // 🔹 Verificar si ya existe token guardado
        CoroutineScope(Dispatchers.IO).launch {
            val savedToken = userPrefs.getToken()
            if (!savedToken.isNullOrEmpty()) {
                // Usuario ya autenticado, pasar directamente al MainActivity
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun login(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitClient.instance.login(LoginRequest(email, password))
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    CoroutineScope(Dispatchers.IO).launch { userPrefs.saveToken(token) }

                    Toast.makeText(this@LoginActivity, "Bienvenido!", Toast.LENGTH_SHORT).show()

                    // ✅ Ahora va directo al MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
