package com.example.pmm_2eval_trabajo_final

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class ScheduledPaymentsActivity : AppCompatActivity() {

    private lateinit var spRecipient: Spinner
    private lateinit var spCard: Spinner
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var btnSchedulePayment: Button

    private val users = mutableListOf<Map<String, String>>() // Lista de usuarios (nombre y ID)
    private val cards = mutableListOf<Card>() // Lista de tarjetas del usuario actual

    private lateinit var rvScheduledPayments: RecyclerView
    private val scheduledPayments = mutableListOf<ScheduledPayment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduled_payments)

        // Inicializa las vistas
        spRecipient = findViewById(R.id.spRecipient)
        spCard = findViewById(R.id.spCard)
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        btnSchedulePayment = findViewById(R.id.btnSchedulePayment)
        rvScheduledPayments = findViewById(R.id.rvScheduledPayments)

        // Carga los usuarios, tarjetas y pagos programados
        loadUsers()
        loadCardsForSpinner()
        loadScheduledPayments()

        // Configura el botón para programar el pago
        btnSchedulePayment.setOnClickListener {
            schedulePayment()
        }

        // Configura el clic en los campos de fecha y hora
        etDate.setOnClickListener { showDatePickerDialog() }
        etTime.setOnClickListener { showTimePickerDialog() }

        // Configura el RecyclerView con el adaptador
        val adapter = ScheduledPaymentAdapter(scheduledPayments)
        rvScheduledPayments.adapter = adapter
        rvScheduledPayments.layoutManager = LinearLayoutManager(this)

        // Habilita el swipe para eliminar
        setupSwipeToDelete(adapter)
    }

    private fun loadUsers() {
        val database = FirebaseDatabase.getInstance().reference
        val usersRef = database.child("users")
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key
                    val userName = userSnapshot.child("name").getValue(String::class.java)
                    if (userId != null && userName != null && userId != currentUserUid) {
                        users.add(mapOf("id" to userId, "name" to userName))
                    }
                }

                // Configura el Spinner de usuarios
                val adapter = ArrayAdapter(this@ScheduledPaymentsActivity, android.R.layout.simple_spinner_item, users.map { it["name"] })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spRecipient.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ScheduledPaymentsActivity, "Error al cargar los usuarios", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadCardsForSpinner() {
        val database = FirebaseDatabase.getInstance().reference
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userCardsRef = database.child("users").child(currentUser.uid).child("cards")
        userCardsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cards.clear() // Limpia la lista de tarjetas
                val cardNumbers = mutableListOf<String>() // Lista para los números formateados

                for (cardSnapshot in snapshot.children) {
                    val cardNumber = cardSnapshot.key // Obtiene la clave como cardNumber
                    val card = cardSnapshot.getValue(Card::class.java)
                    if (card != null && cardNumber != null) {
                        card.cardNumber = cardNumber // Asigna el número de tarjeta al modelo
                        cards.add(card) // Agrega la tarjeta a la lista
                        // Formatea el número de tarjeta
                        val formattedCardNumber = "**** **** **** ${cardNumber.takeLast(4)}"
                        cardNumbers.add(formattedCardNumber)
                    }
                }

                // Configura el Spinner con los números de tarjeta formateados
                setupCardSpinner(cardNumbers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ScheduledPayments", "Error al cargar las tarjetas: ${error.message}")
                Toast.makeText(this@ScheduledPaymentsActivity, "Error al cargar las tarjetas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCardSpinner(cardNumbers: List<String>) {
        // Inicializa el Spinner
        spCard = findViewById(R.id.spCard)

        // Crea un adaptador para el Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            cardNumbers
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Asigna el adaptador al Spinner
        spCard.adapter = adapter
    }

    private fun schedulePayment() {
        val selectedRecipientPosition = spRecipient.selectedItemPosition
        val selectedCardPosition = spCard.selectedItemPosition
        val amount = etAmount.text.toString().toDoubleOrNull()
        val date = etDate.text.toString()
        val time = etTime.text.toString()

        if (selectedRecipientPosition == -1 || selectedCardPosition == -1 || amount == null || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica que la lista de tarjetas no esté vacía
        if (selectedCardPosition >= cards.size) {
            Toast.makeText(this, "Error al seleccionar la tarjeta", Toast.LENGTH_SHORT).show()
            return
        }

        val recipient = users[selectedRecipientPosition]
        val card = cards[selectedCardPosition] // Accede a la tarjeta desde la lista sincronizada
        val scheduledDateTime = "$date $time"

        val payment = ScheduledPayment(
            recipientId = recipient["id"]!!,
            recipientName = recipient["name"]!!,
            amount = amount,
            cardNumber = card.cardNumber!!,
            scheduledDateTime = scheduledDateTime
        )

        saveScheduledPayment(payment)
    }

    private fun saveScheduledPayment(payment: ScheduledPayment) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        val paymentsRef = database.child("users").child(currentUser.uid).child("scheduledPayments")
        val paymentId = paymentsRef.push().key // Genera una clave única para el pago

        if (paymentId != null) {
            payment.paymentId = paymentId // Asigna el ID único al pago
            paymentsRef.child(paymentId).setValue(payment)
                .addOnSuccessListener {
                    Toast.makeText(this, "Pago programado guardado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar el pago", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadScheduledPayments() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        val paymentsRef = database.child("users").child(currentUser.uid).child("scheduledPayments")

        paymentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scheduledPayments.clear()
                for (paymentSnapshot in snapshot.children) {
                    val payment = paymentSnapshot.getValue(ScheduledPayment::class.java)
                    if (payment != null) {
                        scheduledPayments.add(payment)
                    }
                }

                // Configura el RecyclerView con los pagos programados
                setupScheduledPaymentsRecyclerView(scheduledPayments)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ScheduledPaymentsActivity, "Error al cargar los pagos programados", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupScheduledPaymentsRecyclerView(payments: MutableList<ScheduledPayment>) {
        if (payments.isEmpty()) {
            // Muestra un mensaje o vista indicando que no hay pagos programados
            rvScheduledPayments.visibility = View.GONE
            findViewById<View>(R.id.tvNoScheduledPaymentsMessage).visibility = View.VISIBLE
        } else {
            // Configura el RecyclerView con los pagos programados
            rvScheduledPayments.visibility = View.VISIBLE
            findViewById<View>(R.id.tvNoScheduledPaymentsMessage).visibility = View.GONE

            val adapter = ScheduledPaymentAdapter(payments)
            rvScheduledPayments.adapter = adapter
            rvScheduledPayments.layoutManager = LinearLayoutManager(this)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                etDate.setText("$selectedYear-${String.format("%02d", selectedMonth + 1)}-${String.format("%02d", selectedDay)}")
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                etTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun setupSwipeToDelete(adapter: ScheduledPaymentAdapter) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // No necesitamos mover elementos
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // Verifica si la lista tiene elementos
                if (position < 0 || position >= scheduledPayments.size) {
                    Toast.makeText(this@ScheduledPaymentsActivity, "Error al eliminar el pago", Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(position) // Restaura el elemento en la interfaz
                    return
                }

                val payment = scheduledPayments[position]

                // Elimina el pago de Firebase
                deletePaymentFromFirebase(payment)

                // Elimina el pago de la lista local
                adapter.removePayment(position)

                Toast.makeText(this@ScheduledPaymentsActivity, "Pago eliminado", Toast.LENGTH_SHORT).show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvScheduledPayments)
    }

    private fun deletePaymentFromFirebase(payment: ScheduledPayment) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        val paymentsRef = database.child("users").child(currentUser.uid).child("scheduledPayments")

        // Verifica que el pago tenga un ID único
        val paymentId = payment.paymentId
        if (paymentId.isNullOrEmpty()) {
            Log.e("ScheduledPayments", "El pago no tiene un ID único")
            return
        }

        // Elimina el pago usando su ID único
        paymentsRef.child(paymentId).removeValue()
            .addOnSuccessListener {
                Log.d("ScheduledPayments", "Pago eliminado de Firebase")
            }
            .addOnFailureListener {
                Log.e("ScheduledPayments", "Error al eliminar el pago de Firebase", it)
            }
    }
}

data class ScheduledPayment(
    var paymentId: String? = null,
    var recipientId: String? = null,
    var recipientName: String? = null,
    var amount: Double? = null,
    var cardNumber: String? = null,
    var scheduledDateTime: String? = null
)