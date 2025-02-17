package com.example.pmm_2eval_trabajo_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.material3.Card
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    private val cards: List<Card>,
    private val onCardSelected: (Card) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCardBalance: TextView = itemView.findViewById(R.id.tvCardBalance)
        private val tvCardNumberPreview: TextView = itemView.findViewById(R.id.tvCardNumberPreview)

        fun bind(card: Card) {
            tvCardBalance.text = card.balanceFormatted
            tvCardNumberPreview.text = card.lastFourDigitsFormatted

            // Detecta clics en la tarjeta
            itemView.setOnClickListener {
                onCardSelected(card)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int = cards.size
}