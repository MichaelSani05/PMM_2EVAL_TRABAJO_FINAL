package com.example.pmm_2eval_trabajo_final

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import android.os.Parcel
import android.os.Parcelable

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDescription: TextView = itemView.findViewById(R.id.tvTransactionDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvTransactionDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvTransactionAmount)

        fun bind(transaction: Transaction) {
            tvDescription.text = transaction.description

            // Formatea la fecha si está disponible
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
            val formattedDate = transaction.date?.let { dateString ->
                try {
                    val date = inputFormat.parse(dateString)
                    outputFormat.format(date)
                } catch (e: Exception) {
                    e.printStackTrace()
                    "Fecha desconocida"
                }
            } ?: "Fecha desconocida"

            tvDate.text = formattedDate
            tvAmount.text = "$${transaction.amount}"

            // Configura el listener para el clic en el elemento
            itemView.setOnClickListener {
                onItemClick(transaction)
            }
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
    var type: String = "",
    var amount: Double = 0.0,
    var date: String = "",
    var to: String = "", // Este es el ID del destinatario en Firebase
    var status: String = "",
    var description: String = ""
) : Parcelable {

    // Constructor sin argumentos para Firebase
    constructor() : this("", 0.0, "", "", "", "")

    // Constructor para Parcelable
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "", // Lee el campo "to"
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeDouble(amount)
        parcel.writeString(date)
        parcel.writeString(to) // Escribe el campo "to"
        parcel.writeString(status)
        parcel.writeString(description)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction = Transaction(parcel)
        override fun newArray(size: Int): Array<Transaction?> = arrayOfNulls(size)
    }
}