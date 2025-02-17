package com.example.pmm_2eval_trabajo_final

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var rvCards: RecyclerView
    private lateinit var tvBalance: TextView
    private lateinit var tvCardNumber: TextView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        rvCards = findViewById(R.id.rvCards)
        tvBalance = findViewById(R.id.tvBalance)
        tvCardNumber = findViewById(R.id.tvCardNumber)

        // Verifica si el usuario está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Si no hay usuario autenticado, redirige a AuthActivity
            startActivity(Intent(this, Auth::class.java))
            finish()
            return
        }

        database = FirebaseDatabase.getInstance("https://pmm-investor-default-rtdb.europe-west1.firebasedatabase.app").reference
        val userCardsRef = database.child("users").child(currentUser.uid).child("cards")

        // Observa los datos de las tarjetas
        userCardsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cards = mutableListOf<Card>()
                for (cardSnapshot in snapshot.children) {
                    val card = cardSnapshot.getValue(Card::class.java)
                    if (card != null) {
                        cards.add(card)
                    }
                }

                // Configura el adaptador
                setupRecyclerView(cards)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al cargar las tarjetas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView(cards: List<Card>) {
        val adapter = CardAdapter(cards) { selectedCard ->
            tvBalance.text = selectedCard.balanceFormatted
            tvCardNumber.text = selectedCard.lastFourDigitsFormatted
        }
        rvCards.adapter = adapter
        rvCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Añade SnapHelper para centrar las tarjetas
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvCards)

        // Actualiza la información al desplazarse
        rvCards.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val centerPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (centerPosition != RecyclerView.NO_POSITION) {
                        val selectedCard = cards[centerPosition]
                        tvBalance.text = selectedCard.balanceFormatted
                        tvCardNumber.text = selectedCard.lastFourDigitsFormatted
                    }
                }
            }
        })
    }
}

data class Card(
    val cardNumber: String? = null,
    val cvv: String? = null,
    val expirationDate: String? = null,
    val limit: Double? = null,
    val currentBalance: Double? = null
) {
    // Formatea los últimos 4 dígitos de la tarjeta
    val lastFourDigitsFormatted: String
        get() = "**** **** **** ${cardNumber?.takeLast(4)}"

    // Formatea el saldo actual
    val balanceFormatted: String
        get() = "$${currentBalance ?: 0.0}"
}