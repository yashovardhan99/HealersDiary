package com.yashovardhan99.healersdiary.patients

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.database.Payment
import com.yashovardhan99.healersdiary.databinding.ItemCardPaymentBinding


class PaymentListAdapter(private val onRequestDelete: (Payment) -> Unit) : PagingDataAdapter<Payment, PaymentListAdapter.PaymentViewHolder>(PaymentDiffUtils()) {
    class PaymentViewHolder(val binding: ItemCardPaymentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(payment: Payment?, onRequestDelete: (Payment) -> Unit) {
            binding.payment = payment
            binding.root.setOnCreateContextMenuListener { menu, _, _ ->
                menu.add(0, 0, 0, R.string.delete).setOnMenuItemClickListener { _ ->
                    payment?.let { onRequestDelete(it); true } ?: false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        return PaymentViewHolder(ItemCardPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position), onRequestDelete)
    }
}

class PaymentDiffUtils : DiffUtil.ItemCallback<Payment>() {
    override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
        return oldItem == newItem
    }

}