package cl.alkewallet.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cl.alkewallet.R
import cl.alkewallet.data.local.TransactionEntity
import cl.alkewallet.databinding.ItemTransactionBinding
import cl.alkewallet.util.Formatters

class TransactionAdapter :
    ListAdapter<TransactionEntity, TransactionAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        val expense = item.isExpense()

        holder.binding.tvDescription.text =
            item.description.ifBlank { context.getString(R.string.no_description) }
        holder.binding.tvDate.text = Formatters.date(item.date)
        holder.binding.tvAmount.text = Formatters.signedMoney(item.absoluteAmount(), expense)
        holder.binding.tvAmount.setTextColor(
            ContextCompat.getColor(context, if (expense) R.color.expense else R.color.income)
        )
    }

    private object Diff : DiffUtil.ItemCallback<TransactionEntity>() {
        override fun areItemsTheSame(old: TransactionEntity, new: TransactionEntity) = old.id == new.id
        override fun areContentsTheSame(old: TransactionEntity, new: TransactionEntity) = old == new
    }
}
