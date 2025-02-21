package com.example.pmm_2eval_trabajo_final

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var rvCards: RecyclerView
    private lateinit var tvCardNumber: TextView
    private lateinit var tvNoCardsMessage: TextView
    private lateinit var database: DatabaseReference
    private lateinit var rvTransactions: RecyclerView
    private lateinit var tvNoTransactionsMessage: TextView
    private lateinit var tvTransactionsTitle: TextView
    private lateinit var btnAddBalance: ImageView
    private lateinit var btnTransfer: ImageView
    private lateinit var txtTransfer : TextView
    private lateinit var transferLayout : LinearLayout
    private lateinit var añadirLayout: LinearLayout
    private lateinit var txtAddBalance : TextView
    private var selectedCardId: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvCards = findViewById(R.id.rvCards)
        tvCardNumber = findViewById(R.id.tvCardNumber)
        tvNoCardsMessage = findViewById(R.id.tvNoCardsMessage)
        rvTransactions = findViewById(R.id.rvTransactions)
        tvNoTransactionsMessage = findViewById(R.id.tvNoTransactionsMessage)
        tvTransactionsTitle = findViewById(R.id.tvTransactionsTitle)
        btnAddBalance = findViewById(R.id.btnAddBalance)
        txtTransfer = findViewById(R.id.txtTransfer)
        txtAddBalance = findViewById(R.id.txtAddBalance)
        transferLayout = findViewById(R.id.transferLayout)
        añadirLayout = findViewById(R.id.añadirLayout)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, Auth::class.java))
            finish()
            return
        }

        auth = FirebaseAuth.getInstance()

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()

            startActivity(Intent(this, Auth::class.java))
            finish()
        }

        // Tarjeta
        val btnAddCard = findViewById<ImageView>(R.id.btnAddCard)
        btnAddCard.setOnClickListener {
            startActivity(Intent(this, AddCardActivity::class.java))
        }

        // Transferencia
        btnTransfer = findViewById<ImageView>(R.id.btnTransfer)
        btnTransfer.setOnClickListener {
            if (selectedCardId.isNullOrEmpty()) {
                Toast.makeText(this, "No hay tarjeta seleccionada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, TransferActivity::class.java)
            intent.putExtra("selectedCardId", selectedCardId)
            startActivity(intent)
        }

        // Saldo
        btnAddBalance.setOnClickListener {
            if (selectedCardId.isNullOrEmpty()) {
                Toast.makeText(this, "No hay tarjeta seleccionada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, AddBalanceActivity::class.java)
            intent.putExtra("cardId", selectedCardId)
            startActivity(intent)
        }

        // Menú
        val btnHome = findViewById<Button>(R.id.btnHome)
        btnHome.setOnClickListener {
            Toast.makeText(this, "Ya estás en la página de inicio", Toast.LENGTH_SHORT).show()
        }

        val btnStatistics = findViewById<Button>(R.id.btnStatistics)
        btnStatistics.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        val btnPagos = findViewById<Button>(R.id.btnPagos)
        btnPagos.setOnClickListener {
            startActivity(Intent(this, ScheduledPaymentsActivity::class.java))
        }

        database = FirebaseDatabase.getInstance("https://pmm-investor-default-rtdb.europe-west1.firebasedatabase.app").reference
        val userCardsRef = database.child("users").child(currentUser.uid).child("cards")

        // Controlar lista/carrousel de tarjetas
        userCardsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cards = mutableListOf<Card>()
                for (cardSnapshot in snapshot.children) {
                    val cardNumber = cardSnapshot.key
                    val card = cardSnapshot.getValue(Card::class.java)
                    if (card != null && cardNumber != null) {
                        card.cardNumber = cardNumber
                        cards.add(card)
                    }
                }

                if (cards.isEmpty()) {
                    tvNoCardsMessage.visibility = View.VISIBLE
                    tvCardNumber.visibility = View.GONE
                    rvCards.visibility = View.GONE
                    transferLayout.visibility = View.GONE
                    añadirLayout.visibility = View.GONE
                } else {
                    tvNoCardsMessage.visibility = View.GONE
                    tvCardNumber.visibility = View.VISIBLE
                    rvCards.visibility = View.VISIBLE
                    transferLayout.visibility = View.VISIBLE
                    añadirLayout.visibility = View.VISIBLE

                    if (rvCards.adapter == null) {
                        setupRecyclerView(cards)
                    } else {
                        (rvCards.adapter as? CardAdapter)?.updateCards(cards)
                    }

                    rvCards.post {
                        val layoutManager = rvCards.layoutManager as LinearLayoutManager
                        val closestPosition = findClosestPosition(layoutManager, cards)
                        if (closestPosition != RecyclerView.NO_POSITION) {
                            val initialCard = cards[closestPosition]
                            updateTextViews(initialCard)
                            selectedCardId = initialCard.cardNumber
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al cargar las tarjetas", Toast.LENGTH_SHORT).show()
            }
        })

        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume llamado")
        loadCardsFromFirebase()
    }

    private fun loadCardsFromFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userCardsRef = database.child("users").child(currentUser.uid).child("cards")

        userCardsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MainActivity", "Datos cargados desde Firebase: ${snapshot.value}")
                val cards = mutableListOf<Card>()
                for (cardSnapshot in snapshot.children) {
                    val cardNumber = cardSnapshot.key
                    val card = cardSnapshot.getValue(Card::class.java)
                    if (card != null && cardNumber != null) {
                        card.cardNumber = cardNumber
                        cards.add(card)
                    }
                }

                if (cards.isEmpty()) {
                    tvNoCardsMessage.visibility = View.VISIBLE
                    tvCardNumber.visibility = View.GONE
                    rvCards.visibility = View.GONE
                    btnAddBalance.visibility = View.GONE
                } else {
                    tvNoCardsMessage.visibility = View.GONE
                    tvCardNumber.visibility = View.VISIBLE
                    rvCards.visibility = View.VISIBLE
                    btnAddBalance.visibility = View.VISIBLE

                    (rvCards.adapter as? CardAdapter)?.updateCards(cards)

                    rvCards.post {
                        val layoutManager = rvCards.layoutManager as LinearLayoutManager
                        val closestPosition = findClosestPosition(layoutManager, cards)
                        if (closestPosition != RecyclerView.NO_POSITION) {
                            val initialCard = cards[closestPosition]
                            updateTextViews(initialCard)
                            selectedCardId = initialCard.cardNumber
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "Error al cargar las tarjetas: ${error.message}")
                Toast.makeText(this@MainActivity, "Error al cargar las tarjetas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView(cards: List<Card>) {
        val adapter = CardAdapter(cards) { selectedCard ->
            updateTextViews(selectedCard)
            selectedCardId = selectedCard.cardNumber
        }
        rvCards.adapter = adapter
        rvCards.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val snapHelper = PagerSnapHelper()
        if (rvCards.onFlingListener == null) {
            snapHelper.attachToRecyclerView(rvCards)
        }

        val spacing = resources.getDimensionPixelSize(R.dimen.card_spacing)
        rvCards.addItemDecoration(CenterItemDecoration(spacing))

        rvCards.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val closestPosition = findClosestPosition(layoutManager, cards)
                    if (closestPosition != RecyclerView.NO_POSITION) {
                        val selectedCard = cards[closestPosition]
                        updateTextViews(selectedCard)
                        selectedCardId = selectedCard.cardNumber

                        (rvCards.adapter as? CardAdapter)?.onCardSelected?.invoke(selectedCard)
                    }
                }
            }
        })
    }

    private fun findClosestPosition(layoutManager: LinearLayoutManager, cards: List<Card>): Int {
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        var closestPosition = RecyclerView.NO_POSITION
        var minDistance = Int.MAX_VALUE

        for (position in firstVisiblePosition..lastVisiblePosition) {
            if (position < 0 || position >= cards.size) continue

            val view = layoutManager.findViewByPosition(position) ?: continue
            val centerX = (view.left + view.right) / 2
            val parentCenterX = rvCards.width / 2
            val distance = kotlin.math.abs(centerX - parentCenterX)

            if (distance < minDistance) {
                minDistance = distance
                closestPosition = position
            }
        }

        return closestPosition
    }

    private fun loadTransactions() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance("https://pmm-investor-default-rtdb.europe-west1.firebasedatabase.app").reference
        val userTransactionsRef = database.child("users").child(currentUser.uid).child("transactions")

        userTransactionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactions = mutableListOf<Transaction>()
                for (transactionSnapshot in snapshot.children) {
                    val transaction = transactionSnapshot.getValue(Transaction::class.java)
                    if (transaction != null) {
                        transactions.add(transaction)
                    }
                }

                if (transactions.isEmpty()) {
                    tvTransactionsTitle.visibility = View.GONE
                    rvTransactions.visibility = View.GONE
                    tvNoTransactionsMessage.visibility = View.VISIBLE
                } else {
                    tvTransactionsTitle.visibility = View.VISIBLE
                    rvTransactions.visibility = View.VISIBLE
                    tvNoTransactionsMessage.visibility = View.GONE

                    setupTransactionsRecyclerView(transactions)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error al cargar las transacciones", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupTransactionsRecyclerView(transactions: List<Transaction>) {
        val adapter = TransactionAdapter(transactions) { selectedTransaction ->
            val intent = Intent(this, TransactionDetailsActivity::class.java)
            intent.putExtra("transaction", selectedTransaction)
            startActivity(intent)
        }
        rvTransactions.adapter = adapter
        rvTransactions.layoutManager = LinearLayoutManager(this)
    }

    private fun updateTextViews(card: Card) {
        tvCardNumber.text = card.lastFourDigitsFormatted
    }
}

data class Card(
    val cvv: String? = null,
    val expirationDate: String? = null,
    val limit: Double? = null,
    val currentBalance: Double? = null,
    var cardNumber: String? = null
) {
    val lastFourDigitsFormatted: String
        get() = "**** **** **** ${cardNumber?.takeLast(4)}"

    val balanceFormatted: String
        get() = "$${currentBalance ?: 0.0}"
}