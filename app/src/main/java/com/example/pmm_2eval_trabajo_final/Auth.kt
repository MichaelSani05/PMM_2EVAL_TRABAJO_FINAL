package com.example.pmm_2eval_trabajo_final

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Auth : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Inicializa Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Vincula las vistas
        val logo = findViewById<View>(R.id.logo)
        val btnChooseLogin = findViewById<View>(R.id.btnChooseLogin)
        val btnChooseRegister = findViewById<View>(R.id.btnChooseRegister)
        val loginForm = findViewById<View>(R.id.loginForm)
        val registerForm = findViewById<View>(R.id.registerForm)

        // Configura el botón de "Iniciar sesión"
        btnChooseLogin.setOnClickListener {
            showLoginForm()
        }

        // Configura el botón de "Registrarse"
        btnChooseRegister.setOnClickListener {
            showRegisterForm()
        }

        // Configura el botón de retroceso en el formulario de inicio de sesión
        loginForm.findViewById<View>(R.id.btnBackLogin).setOnClickListener {
            showInitialState()
        }

        // Configura el botón de retroceso en el formulario de registro
        registerForm.findViewById<View>(R.id.btnBackRegister).setOnClickListener {
            showInitialState()
        }

        // Configura el botón de "Proceder" en el formulario de inicio de sesión
        loginForm.findViewById<View>(R.id.btnProceedLogin).setOnClickListener {
            val email = loginForm.findViewById<TextView>(R.id.etEmailLogin).text.toString().trim()
            val password = loginForm.findViewById<TextView>(R.id.etPasswordLogin).text.toString().trim()
            loginUser(email, password)
        }

        // Configura el botón de "Proceder" en el formulario de registro
        registerForm.findViewById<View>(R.id.btnProceedRegister).setOnClickListener {
            val name = registerForm.findViewById<TextView>(R.id.etNameRegister).text.toString().trim()
            val email = registerForm.findViewById<TextView>(R.id.etEmailRegister).text.toString().trim()
            val password = registerForm.findViewById<TextView>(R.id.etPasswordRegister).text.toString().trim()
            registerUser(name, email, password)
        }
    }

    private fun showLoginForm() {
        findViewById<View>(R.id.logo).visibility = View.GONE
        findViewById<View>(R.id.btnChooseLogin).visibility = View.GONE
        findViewById<View>(R.id.btnChooseRegister).visibility = View.GONE
        findViewById<View>(R.id.loginForm).visibility = View.VISIBLE
    }

    private fun showRegisterForm() {
        findViewById<View>(R.id.logo).visibility = View.GONE
        findViewById<View>(R.id.btnChooseLogin).visibility = View.GONE
        findViewById<View>(R.id.btnChooseRegister).visibility = View.GONE
        findViewById<View>(R.id.registerForm).visibility = View.VISIBLE
    }

    private fun showInitialState() {
        findViewById<View>(R.id.logo).visibility = View.VISIBLE
        findViewById<View>(R.id.btnChooseLogin).visibility = View.VISIBLE
        findViewById<View>(R.id.btnChooseRegister).visibility = View.VISIBLE
        findViewById<View>(R.id.loginForm).visibility = View.GONE
        findViewById<View>(R.id.registerForm).visibility = View.GONE
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

    private fun registerUser(name: String, email: String, password: String) {
        if (!validateInput(email, password)) {
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserToDatabase(user.uid, name, email)
                    } else {
                        Toast.makeText(this, "Error: No se pudo autenticar al usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(uid: String, name: String, email: String) {
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(uid)
        val userData = mapOf(
            "name" to name,
            "email" to email,
            "balance" to 0.0,
            "transactions" to emptyMap<String, Any>(),
            "cards" to emptyMap<String, Any>(),
            "crypto_wallet" to emptyMap<String, Any>()
        )
        userRef.setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { exception ->
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