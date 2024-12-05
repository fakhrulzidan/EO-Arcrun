package com.belajar.arcruncoba1.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belajar.arcruncoba1.Activity.UpdateEventActivity
import com.belajar.arcruncoba1.Models.EventModels
import com.belajar.arcruncoba1.R
import com.belajar.arcruncoba1.databinding.ViewholderEventBinding
import com.bumptech.glide.Glide

class EventAdapter(
    private val items: ArrayList<EventModels>,
    private val eventIds: List<String>,
    private val context: Context
) : RecyclerView.Adapter<EventAdapter.Viewholder>() {

    inner class Viewholder(private val binding: ViewholderEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EventModels, eventId: String) {
            binding.namaEventTxt.text = item.nama_event
            binding.statusEventTxt.text = item.status_event
            binding.hargaTxt.text = "Rp.${item.harga}"
            binding.batasAkhirTxt.text = item.batas_akhir

            Glide.with(context)
                .load(item.gambar)
                .error(R.drawable.imagenotfound)
                .into(binding.gambarBg)

            binding.root.setOnClickListener {
                val intent = Intent(context, UpdateEventActivity::class.java)
                intent.putExtra("object", item)
                intent.putExtra("eventId", eventId)
                Log.d("EventAdapter", "eventId dikirim: $eventId")
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        holder.bind(items[position], eventIds[position])
    }

    override fun getItemCount(): Int = items.size
}
