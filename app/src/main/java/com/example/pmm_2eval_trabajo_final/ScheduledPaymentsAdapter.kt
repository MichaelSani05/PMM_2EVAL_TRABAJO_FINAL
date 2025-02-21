package com.example.pmm_2eval_trabajo_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView

class ScheduledPaymentAdapter(
    private var payments: MutableList<ScheduledPayment>
) : RecyclerView.Adapter<ScheduledPaymentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lottieIcon: LottieAnimationView = itemView.findViewById(R.id.ivRecipientIcon)
        private val tvRecipient: TextView = itemView.findViewById(R.id.tvRecipient)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(payment: ScheduledPayment) {
            tvRecipient.text = "Destinatario: ${payment.recipientName}"
            tvAmount.text = "Cantidad: $${payment.amount}"
            tvDate.text = "Fecha: ${payment.scheduledDateTime}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scheduled_payment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(payments[position])

        holder.itemView.setOnClickListener {
            holder.lottieIcon.playAnimation()
        }
    }

    override fun getItemCount(): Int = payments.size

    fun removePayment(position: Int) {
        if (position in 0 until payments.size) {
            payments.removeAt(position)
            notifyItemRemoved(position)
        } else {
            throw IndexOutOfBoundsException("Índice inválido: $position")
        }
    }

}