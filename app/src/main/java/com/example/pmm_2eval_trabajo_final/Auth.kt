package com.example.pmm_2eval_trabajo_final

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pmm_2eval_trabajo_final.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger

class Auth : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Habilita el registro detallado de Firebase Database para depuración
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG)

        // Configura el botón de inicio de sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            loginUser(email, password)
        }

        // Configura el botón de registro
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            registerUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        if (!validateInput(email, password)) {
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerUser(email: String, password: String) {
        if (!validateInput(email, password)) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d("Auth", "Usuario autenticado con UID: ${user.uid}")
                        saveUserToDatabase(user.uid, email)
                    } else {
                        Log.e("Auth", "El usuario no está autenticado después del registro")
                        Toast.makeText(this, "Error: No se pudo autenticar al usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(uid: String, email: String) {
        Log.d("Auth", "Guardando usuario con UID: $uid")

        // Usa explícitamente la URL correcta de Firebase Realtime Database
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(uid)

        val userData = mapOf(
            "email" to email,
            "name" to "",
            "balance" to 0.0,
            "transactions" to emptyMap<String, Any>(),
            "cards" to emptyMap<String, Any>(),
            "crypto_wallet" to emptyMap<String, Any>()
        )

        userRef.setValue(userData)
            .addOnSuccessListener {
                Log.d("Auth", "Usuario guardado correctamente en la base de datos")
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
                Log.e("Auth", "Error al guardar el usuario: ${exception.message}")
                Toast.makeText(this, "Error al guardar el usuario: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}