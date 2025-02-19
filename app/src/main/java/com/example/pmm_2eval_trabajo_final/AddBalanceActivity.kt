package com.example.pmm_2eval_trabajo_final

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddBalanceActivity : AppCompatActivity() {
    private lateinit var etAmountToAdd: EditText
    private lateinit var btnAddBalance: Button
    private lateinit var database: DatabaseReference
    private lateinit var currentUserUid: String
    private var cardId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_balance)

        val goBack = findViewById<ImageView>(R.id.goBack)
        goBack.setOnClickListener{
            finish()
        }

        // Inicializa las vistas
        etAmountToAdd = findViewById(R.id.etAmountToAdd)
        btnAddBalance = findViewById(R.id.btnAddBalance)

        // Obtiene el ID de la tarjeta seleccionada desde el Intent
        cardId = intent.getStringExtra("cardId")

        // Inicializa Firebase
        database = FirebaseDatabase.getInstance("https://pmm-investor-default-rtdb.europe-west1.firebasedatabase.app").reference
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Configura el botón para añadir saldo
        btnAddBalance.setOnClickListener {
            val amountText = etAmountToAdd.text.toString().trim()
            if (amountText.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Por favor, ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verifica que el ID de la tarjeta no sea nulo
            if (cardId.isNullOrEmpty()) {
                Toast.makeText(this, "No se pudo identificar la tarjeta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Añade el saldo a la tarjeta
            addBalanceToCard(cardId, amount)

        }
    }

    private fun addBalanceToCard(cardId: String?, amount: Double) {
        if (cardId.isNullOrEmpty()) {
            Log.e("AddBalanceActivity", "ID de tarjeta nulo o vacío")
            Toast.makeText(this, "No se pudo identificar la tarjeta", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("AddBalanceActivity", "Añadiendo saldo a la tarjeta con ID: $cardId")

        val cardRef = database.child("users").child(currentUserUid).child("cards").child(cardId)
        cardRef.child("currentBalance").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentBalance = snapshot.getValue(Double::class.java) ?: 0.0
                Log.d("AddBalanceActivity", "Saldo actual: $currentBalance, Saldo a añadir: $amount")

                val newBalance = currentBalance + amount

                cardRef.child("currentBalance").setValue(newBalance).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("AddBalanceActivity", "Saldo añadido con éxito")
                        Toast.makeText(this@AddBalanceActivity, "Saldo añadido con éxito", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Log.e("AddBalanceActivity", "Error al añadir saldo: ${task.exception?.message}")
                        Toast.makeText(this@AddBalanceActivity, "Error al añadir saldo", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddBalanceActivity", "Error al cargar el saldo: ${error.message}")
                Toast.makeText(this@AddBalanceActivity, "Error al cargar el saldo: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}