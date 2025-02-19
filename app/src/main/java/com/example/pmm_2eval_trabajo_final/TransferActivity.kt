package com.example.pmm_2eval_trabajo_final

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.*

class TransferActivity : AppCompatActivity() {
    private lateinit var spnRecipient: Spinner
    private lateinit var etAmount: EditText
    private lateinit var btnSendTransfer: Button
    private lateinit var database: DatabaseReference
    private lateinit var currentUserUid: String
    private val usersList = mutableListOf<String>()
    private val userIds = mutableListOf<String>()

    private lateinit var selectedCardId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        // Recibe el ID de la tarjeta seleccionada
        selectedCardId = intent.getStringExtra("selectedCardId") ?: ""
        if (selectedCardId.isEmpty()) {
            Toast.makeText(this, "No se ha seleccionado ninguna tarjeta", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val goBack = findViewById<ImageView>(R.id.goBack)
        goBack.setOnClickListener{
            finish()
        }

        spnRecipient = findViewById(R.id.spnRecipient)
        etAmount = findViewById(R.id.etAmount)
        btnSendTransfer = findViewById(R.id.btnSendTransfer)

        // Inicializa Firebase
        database = FirebaseDatabase.getInstance("https://pmm-investor-default-rtdb.europe-west1.firebasedatabase.app").reference
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Carga la lista de usuarios disponibles como destinatarios
        loadUsers()

        // Configura el botón para enviar la transferencia
        btnSendTransfer.setOnClickListener {
            val recipientPosition = spnRecipient.selectedItemPosition
            val amountText = etAmount.text.toString().trim()
            if (recipientPosition == -1 || amountText.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val recipientId = userIds[recipientPosition]
            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Por favor, ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendTransfer(recipientId, amount)
        }
    }

    private fun loadUsers() {
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key
                    val userName = userSnapshot.child("name").getValue(String::class.java)
                    if (userId != null && userName != null && userId != currentUserUid) {
                        usersList.add(userName)
                        userIds.add(userId)
                    }
                }

                if (usersList.isNotEmpty()) {
                    val adapter = ArrayAdapter(this@TransferActivity, android.R.layout.simple_spinner_item, usersList)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spnRecipient.adapter = adapter
                } else {
                    Toast.makeText(this@TransferActivity, "No hay otros usuarios disponibles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransferActivity, "Error al cargar usuarios: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendTransfer(recipientId: String, amount: Double) {
        val transactionId = database.child("transactions").push().key
        val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())

        if (transactionId == null) {
            Toast.makeText(this, "Error al generar la transacción", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtiene el saldo de la tarjeta seleccionada
        database.child("users").child(currentUserUid).child("cards").child(selectedCardId).child("currentBalance")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(cardSnapshot: DataSnapshot) {
                    val cardBalance = cardSnapshot.getValue(Double::class.java) ?: 0.0
                    if (cardBalance < amount) {
                        Toast.makeText(this@TransferActivity, "Saldo insuficiente en la tarjeta seleccionada", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Deduce el monto del saldo de la tarjeta seleccionada
                    database.child("users").child(currentUserUid).child("cards").child(selectedCardId).child("currentBalance")
                        .setValue(cardBalance - amount)

                    // Carga el nombre del destinatario
                    database.child("users").child(recipientId).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(nameSnapshot: DataSnapshot) {
                            val recipientName = nameSnapshot.getValue(String::class.java)
                            val formattedRecipientName = recipientName?.split(" ")?.firstOrNull() ?: "Usuario"

                            // Descripción para el remitente
                            val senderDescription = "Transferencia a $formattedRecipientName"

                            // Añade la transacción al remitente
                            val senderTransaction = mapOf(
                                "amount" to -amount,
                                "date" to currentDate,
                                "status" to "completed",
                                "to" to recipientId,
                                "type" to "transfer",
                                "description" to senderDescription // Mantenemos esta descripción igual
                            )
                            database.child("users").child(currentUserUid).child("transactions").child(transactionId).setValue(senderTransaction)

                            // Carga el nombre del remitente para usarlo en la descripción del destinatario
                            database.child("users").child(currentUserUid).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(senderNameSnapshot: DataSnapshot) {
                                    val senderName = senderNameSnapshot.getValue(String::class.java)
                                    val formattedSenderName = senderName?.split(" ")?.firstOrNull() ?: "Usuario"

                                    // Actualiza el saldo del destinatario
                                    database.child("users").child(recipientId).child("balance")
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(recipientSnapshot: DataSnapshot) {
                                                val recipientBalance = recipientSnapshot.getValue(Double::class.java) ?: 0.0
                                                database.child("users").child(recipientId).child("balance").setValue(recipientBalance + amount)

                                                // Añade la transacción al destinatario
                                                val recipientTransaction = mapOf(
                                                    "amount" to amount,
                                                    "date" to currentDate,
                                                    "status" to "completed",
                                                    "from" to currentUserUid,
                                                    "type" to "transfer",
                                                    "description" to formattedSenderName // Solo el nombre del remitente
                                                )
                                                database.child("users").child(recipientId).child("transactions").child(transactionId).setValue(recipientTransaction)

                                                Toast.makeText(this@TransferActivity, "Transferencia realizada con éxito", Toast.LENGTH_SHORT).show()
                                                finish()
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Toast.makeText(this@TransferActivity, "Error al actualizar el saldo del destinatario", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@TransferActivity, "Error al obtener el nombre del remitente", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@TransferActivity, "Error al obtener el nombre del destinatario", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TransferActivity, "Error al obtener el saldo de la tarjeta seleccionada", Toast.LENGTH_SHORT).show()
                }
            })
    }
}