package com.example.pmm_2eval_trabajo_final

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import android.provider.CalendarContract.Calendars

class ScheduledPaymentsActivity : AppCompatActivity() {

    private lateinit var spRecipient: Spinner
    private lateinit var spCard: Spinner
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var btnSchedulePayment: Button

    private val users = mutableListOf<Map<String, String>>()
    private val cards = mutableListOf<Card>()

    private lateinit var rvScheduledPayments: RecyclerView
    private val scheduledPayments = mutableListOf<ScheduledPayment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheduled_payments)

        spRecipient = findViewById(R.id.spRecipient)
        spCard = findViewById(R.id.spCard)
        etAmount = findViewById(R.id.etAmount)
        etDate = findViewById(R.id.etDate)
        etTime = findViewById(R.id.etTime)
        btnSchedulePayment = findViewById(R.id.btnSchedulePayment)
        rvScheduledPayments = findViewById(R.id.rvScheduledPayments)

        loadUsers()
        loadCardsForSpinner()
        loadScheduledPayments()

        btnSchedulePayment.setOnClickListener {
            schedulePayment()
        }

        etDate.setOnClickListener { showDatePickerDialog() }
        etTime.setOnClickListener { showTimePickerDialog() }

        val adapter = ScheduledPaymentAdapter(scheduledPayments)
        rvScheduledPayments.adapter = adapter
        rvScheduledPayments.layoutManager = LinearLayoutManager(this)

        val btnHome = findViewById<Button>(R.id.btnHome)
        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val btnStatistics = findViewById<Button>(R.id.btnStatistics)
        btnStatistics.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        val btnPagos = findViewById<Button>(R.id.btnPagos)
        btnPagos.setOnClickListener {
            Toast.makeText(this, "Ya estás en la página de Pagos", Toast.LENGTH_SHORT).show()
        }

        setupSwipeToDelete(adapter)

        val btnSyncCalendar: Button = findViewById(R.id.btnSyncCalendar)
        btnSyncCalendar.setOnClickListener {
            Toast.makeText(this, "Pagos sincronizados con el calendario", Toast.LENGTH_SHORT).show()
        }
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
                cards.clear()
                val cardNumbers = mutableListOf<String>()

                for (cardSnapshot in snapshot.children) {
                    val cardNumber = cardSnapshot.key
                    val card = cardSnapshot.getValue(Card::class.java)
                    if (card != null && cardNumber != null) {
                        card.cardNumber = cardNumber
                        cards.add(card)
                        val formattedCardNumber = "**** **** **** ${cardNumber.takeLast(4)}"
                        cardNumbers.add(formattedCardNumber)
                    }
                }

                setupCardSpinner(cardNumbers)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ScheduledPayments", "Error al cargar las tarjetas: ${error.message}")
                Toast.makeText(this@ScheduledPaymentsActivity, "Error al cargar las tarjetas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupCardSpinner(cardNumbers: List<String>) {
        spCard = findViewById(R.id.spCard)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            cardNumbers
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

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

        if (selectedCardPosition >= cards.size) {
            Toast.makeText(this, "Error al seleccionar la tarjeta", Toast.LENGTH_SHORT).show()
            return
        }

        val recipient = users[selectedRecipientPosition]
        val card = cards[selectedCardPosition]
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
        val paymentId = paymentsRef.push().key

        if (paymentId != null) {
            payment.paymentId = paymentId
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

                setupScheduledPaymentsRecyclerView(scheduledPayments)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ScheduledPaymentsActivity, "Error al cargar los pagos programados", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupScheduledPaymentsRecyclerView(payments: MutableList<ScheduledPayment>) {
        if (payments.isEmpty()) {
            rvScheduledPayments.visibility = View.GONE
            findViewById<View>(R.id.tvNoScheduledPaymentsMessage).visibility = View.VISIBLE
        } else {
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
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (position < 0 || position >= scheduledPayments.size) {
                    Toast.makeText(this@ScheduledPaymentsActivity, "Error al eliminar el pago", Toast.LENGTH_SHORT).show()
                    adapter.notifyItemChanged(position)
                    return
                }

                val payment = scheduledPayments[position]

                deletePaymentFromFirebase(payment)

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

        val paymentId = payment.paymentId
        if (paymentId.isNullOrEmpty()) {
            Log.e("ScheduledPayments", "El pago no tiene un ID único")
            return
        }

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