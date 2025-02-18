package com.example.pmm_2eval_trabajo_final

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    cards: List<Card>,
    val onCardSelected: (Card) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private var cards: MutableList<Card> = cards.toMutableList()

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCardNumberPreview: TextView = itemView.findViewById(R.id.tvCardNumberPreview)
        private val tvCardExpiration: TextView = itemView.findViewById(R.id.tvCardExpirationDate)
        private val tvCardSaldoPreview : TextView = itemView.findViewById(R.id.tvCardSaldoPreview)

        fun bind(card: Card) {
            tvCardNumberPreview.text = card.lastFourDigitsFormatted
            tvCardSaldoPreview.text = card.balanceFormatted
            tvCardExpiration.text = card.expirationDate ?: "Fecha no disponible"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.bind(card)
    }

    override fun getItemCount(): Int = cards.size

    fun updateCards(newCards: List<Card>) {
        Log.d("CardAdapter", "Actualizando tarjetas: ${newCards.size} tarjetas")
        this.cards.clear()
        this.cards.addAll(newCards)
        notifyDataSetChanged()
    }
}