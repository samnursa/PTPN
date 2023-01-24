package com.example.ptpn.ui.spta

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ptpn.databinding.ItemSptaBinding
import com.example.ptpn.model.SPTA

class SPTAAdapter(
    private val listener: (SPTA) -> Unit
) : RecyclerView.Adapter<SPTAAdapter.ViewHolder>() {

    private var listSpta = emptyList<SPTA>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSptaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() : Int = listSpta.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(listSpta[position], listener)
    }

    class ViewHolder(private val itemSptaBinding: ItemSptaBinding) : RecyclerView.ViewHolder(itemSptaBinding.root) {
        fun bindItem(listSpta: SPTA, listener: (SPTA) -> Unit) {
            itemSptaBinding.tvNoPetak.text = listSpta.no_petak
            itemSptaBinding.tvDate.text = listSpta.tanggal
            itemSptaBinding.tvExpired.text = listSpta.expired
            itemSptaBinding.tvAfdeling.text = listSpta.afdeling
            itemSptaBinding.tvKategori.text = listSpta.kategori
            itemSptaBinding.tvPta.text = listSpta.pta
            itemSptaBinding.tvDeskripsi.text = listSpta.deskripsi
            itemView.setOnClickListener {
                listener(listSpta)
            }
        }
    }

    fun setSptaMethod(listSpta: List<SPTA>) {
        if(listSpta.isEmpty())
            return

        this.listSpta = listSpta
        notifyDataSetChanged()
    }
}