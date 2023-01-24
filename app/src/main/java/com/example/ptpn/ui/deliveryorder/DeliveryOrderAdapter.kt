package com.example.ptpn.ui.deliveryorder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ptpn.databinding.ItemDeliveryOrderBinding
import com.example.ptpn.model.DeliveryOrder

class DeliveryOrderAdapter(
    private val listener: (DeliveryOrder) -> Unit
) : RecyclerView.Adapter<DeliveryOrderAdapter.ViewHolder>() {

    private var listDeliveryOrder = emptyList<DeliveryOrder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemDeliveryOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() : Int = listDeliveryOrder.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(listDeliveryOrder[position], listener)
    }

    class ViewHolder(private val itemDeliveryOrderBinding: ItemDeliveryOrderBinding) : RecyclerView.ViewHolder(itemDeliveryOrderBinding.root) {
        fun bindItem(listDeliveryOrder: DeliveryOrder, listener: (DeliveryOrder) -> Unit) {
            itemDeliveryOrderBinding.tvSopir.text = listDeliveryOrder.sopir
            itemDeliveryOrderBinding.tvDate.text = listDeliveryOrder.tgl_tebang
            itemDeliveryOrderBinding.tvNoLori.text = listDeliveryOrder.no_lori
            itemDeliveryOrderBinding.tvNoTruk.text = listDeliveryOrder.no_truk
            itemView.setOnClickListener {
                listener(listDeliveryOrder)
            }
        }
    }

    fun setDeliveryOrderMethod(listDeliveryOrder: List<DeliveryOrder>) {
        if(listDeliveryOrder.isEmpty())
            return

        this.listDeliveryOrder = listDeliveryOrder
        notifyDataSetChanged()
    }
}