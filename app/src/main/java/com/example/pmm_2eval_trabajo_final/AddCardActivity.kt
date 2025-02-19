package com.example.pmm_2eval_trabajo_final

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddCardActivity : AppCompatActivity() {
    private lateinit var etCardNumber: EditText
    private lateinit var etCVV: EditText
    private lateinit var etExpirationDate: EditText
    private lateinit var btnSaveCard: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        val goBack = findViewById<ImageView>(R.id.goBack)
        goBack.setOnClickListener{
            finish()
        }

        // Inicializa las vistas
        etCardNumber = findViewById(R.id.etCardNumber)
        etCVV = findViewById(R.id.etCVV)
        etExpirationDate = findViewById(R.id.etExpirationDate)
        btnSaveCard = findViewById(R.id.btnSaveCard)

        // Configura el botón para guardar la tarjeta
        btnSaveCard.setOnClickListener {
            val cardNumber = etCardNumber.text.toString().trim()
            val cvv = etCVV.text.toString().trim()
            val expirationDate = etExpirationDate.text.toString().trim()

            // Validación de campos
            if (cardNumber.isEmpty() || cvv.isEmpty() || expirationDate.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardNumber.length != 16 || cvv.length != 3 || expirationDate.length != 5) {
                Toast.makeText(this, "Por favor, ingresa datos válidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guarda la tarjeta en Firebase Realtime Database
            saveCardToDatabase(cardNumber, cvv, expirationDate)
        }
    }

    private fun saveCardToDatabase(cardNumber: String, cvv: String, expirationDate: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance("https://pmm-investor-default-rtdb.europe-west1.firebasedatabase.app").reference

        // Usa el cardNumber como clave (key) en lugar de push()
        val cardRef = database.child("users").child(currentUser.uid).child("cards").child(cardNumber)
        val cardData = mapOf(
            "cvv" to cvv,
            "expirationDate" to expirationDate,
            "limit" to 1000.0, // Límite predeterminado
            "currentBalance" to 0.0 // Saldo inicial
        )

        // Guarda la tarjeta en Firebase
        cardRef.setValue(cardData)
            .addOnSuccessListener {
                Toast.makeText(this, "Tarjeta añadida correctamente", Toast.LENGTH_SHORT).show()
                finish() // Regresa a MainActivity
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al añadir la tarjeta: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}