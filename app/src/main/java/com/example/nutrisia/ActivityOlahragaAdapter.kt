package com.example.nutrisia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActivityOlahragaAdapter(private var olahragaList: List<ViewSport>) :
    RecyclerView.Adapter<ActivityOlahragaAdapter.OlahragaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OlahragaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_olahraga, parent, false)
        return OlahragaViewHolder(view)
    }

    override fun onBindViewHolder(holder: OlahragaViewHolder, position: Int) {
        val olahraga = olahragaList[position]
        holder.bind(olahraga, position + 1) // Kirim posisi sebagai nomor urut
    }

    override fun getItemCount(): Int = olahragaList.size

    fun updateData(newData: List<ViewSport>) {
        olahragaList = newData
        notifyDataSetChanged()
    }

    fun formatJenisOlahraga(input: String): String {
        return input.split('_')
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
    }


    inner class OlahragaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJenisOlahraga: TextView = itemView.findViewById(R.id.tvItemJenisOlahraga)
        private val tvDurasi: TextView = itemView.findViewById(R.id.tvItemDurasi)
        private val tvKaloriOut: TextView = itemView.findViewById(R.id.tvItemKaloriOut)

        fun bind(olahraga: ViewSport, nomor: Int) {
            tvJenisOlahraga.text = formatJenisOlahraga(olahraga.jenis_olahraga) // Format jenis olahraga
            tvDurasi.text = "${olahraga.durasi} Menit"
            tvKaloriOut.text = "${olahraga.kalori_out} Kalori"
        }
    }

}

