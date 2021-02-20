package com.yashovardhan99.healersdiary.patients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yashovardhan99.healersdiary.R
import com.yashovardhan99.healersdiary.databinding.ActivitySeparatorBinding
import com.yashovardhan99.healersdiary.databinding.ItemCardPaymentBinding
import com.yashovardhan99.healersdiary.utils.PaymentParent


private const val VIEW_TYPE_PAYMENT = R.layout.item_card_payment
private const val VIEW_TYPE_SEPARATOR = R.layout.activity_separator

class PaymentListAdapter(private val onRequestEdit: (PaymentParent.Payment) -> Unit, private val onRequestDelete: (PaymentParent.Payment) -> Unit) : PagingDataAdapter<PaymentParent, PaymentListAdapter.PaymentParentViewHolder>(PaymentDiffUtils()) {
    sealed class PaymentParentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(payment: PaymentParent?, onRequestEdit: (PaymentParent.Payment) -> Unit, onRequestDelete: (PaymentParent.Payment) -> Unit)
        class PaymentViewHolder(val binding: ItemCardPaymentBinding) : PaymentParentViewHolder(binding.root) {
            override fun bind(payment: PaymentParent?, onRequestEdit: (PaymentParent.Payment) -> Unit, onRequestDelete: (PaymentParent.Payment) -> Unit) {
                if (payment is PaymentParent.Payment) {
                    binding.payment = payment
                    binding.root.setOnCreateContextMenuListener { menu, _, _ ->
                        menu.add(R.string.edit).setOnMenuItemClickListener { _ ->
                            onRequestEdit(payment)
                            true
                        }
                        menu.add(R.string.delete).setOnMenuItemClickListener { _ ->
                            onRequestDelete(payment)
                            true
                        }
                    }
                }
            }
        }

        class SeparatorViewHolder(val binding: ActivitySeparatorBinding) : PaymentParentViewHolder(binding.root) {
            override fun bind(payment: PaymentParent?, onRequestEdit: (PaymentParent.Payment) -> Unit, onRequestDelete: (PaymentParent.Payment) -> Unit) {
                if (payment !is PaymentParent.PaymentSeparator) throw IllegalArgumentException()
                binding.heading = payment.heading
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PaymentParent.Payment -> VIEW_TYPE_PAYMENT
            is PaymentParent.PaymentSeparator -> VIEW_TYPE_SEPARATOR
            null -> VIEW_TYPE_PAYMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentParentViewHolder {
        return when (viewType) {
            VIEW_TYPE_PAYMENT -> PaymentParentViewHolder.PaymentViewHolder(ItemCardPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_SEPARATOR -> PaymentParentViewHolder.SeparatorViewHolder(ActivitySeparatorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: PaymentParentViewHolder, position: Int) {
        holder.bind(getItem(position), onRequestEdit, onRequestDelete)
    }
}

class PaymentDiffUtils : DiffUtil.ItemCallback<PaymentParent>() {
    override fun areItemsTheSame(oldItem: PaymentParent, newItem: PaymentParent): Boolean {
        return if (oldItem is PaymentParent.Payment && newItem is PaymentParent.Payment) oldItem.id == newItem.id
        else if (oldItem is PaymentParent.PaymentSeparator && newItem is PaymentParent.PaymentSeparator) oldItem.heading == newItem.heading
        else false
    }

    override fun areContentsTheSame(oldItem: PaymentParent, newItem: PaymentParent): Boolean {
        return oldItem == newItem
    }

}