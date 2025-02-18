package com.example.pmm_2eval_trabajo_final

import android.view.LayoutInflater
import android.view.SurfaceControl
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDescription: TextView = itemView.findViewById(R.id.tvTransactionDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvTransactionDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvTransactionAmount)

        fun bind(transaction: Transaction) {
            tvDescription.text = transaction.description
            tvDate.text = transaction.date ?: "Fecha desconocida"
            tvAmount.text = "$${transaction.amount ?: 0.0}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size
}

data class Transaction(
    val amount: Double? = null,
    val date: String? = null,
    val status: String? = null,
    val to: String? = null,
    val type: String? = null,
    val description : String? = null
) {

}