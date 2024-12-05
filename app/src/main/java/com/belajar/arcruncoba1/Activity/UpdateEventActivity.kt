package com.belajar.arcruncoba1.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.belajar.arcruncoba1.Models.EventModels
import com.belajar.arcruncoba1.R
import com.belajar.arcruncoba1.databinding.ActivityUpdateEventBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class UpdateEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateEventBinding
    private var eventId: String? = null
    private var statusEvent: String = "active"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Terima objek EventModels dari Intent
        val event: EventModels? = intent.getParcelableExtra("object")
        eventId = intent.getStringExtra("eventId")
        if (eventId != null) {
            Log.d("DetailEventActivity", "eventId diterima: $eventId")
        } else {
            Log.e("DetailEventActivity", "eventId tidak ditemukan dalam Intent")
        }

        event?.let {
            populateData(it)
        }

        // Tombol Save
        binding.saveBtn.setOnClickListener {
            updateEventToFirebase()
        }

        // Status Event
        binding.statusEventTxt.setOnClickListener {
            toggleEventStatus()
        }
    }


    private fun populateData(event: EventModels) {
        binding.etTittle.setText(event.nama_event)
        binding.etDlEvent.setText(event.batas_akhir)
        binding.etMulaiEvent.setText(event.waktu_mulai)
        binding.etDeskripsi.setText(event.deskripsi)
        binding.etBenefit.setText(event.benefit)
        binding.etHarga.setText(event.harga.toString())
        binding.etLokasi.setText(event.lokasi)
        binding.etKuota.setText(event.kuota.toString())

        // Set status event
        statusEvent = event.status_event ?: "active"
        binding.statusEventTxt.text = statusEvent.capitalize()

        // Load gambarnu
        Glide.with(this)
            .load(event.gambar)
            .error(R.drawable.imagenotfound)
            .into(binding.ivPreviewGambar)
    }

    private fun updateEventToFirebase() {
        // Ambil data dari EditText
        val namaEvent = binding.etTittle.text.toString()
        val batasAkhir = binding.etDlEvent.text.toString()
        val waktuMulai = binding.etMulaiEvent.text.toString()
        val deskripsi = binding.etDeskripsi.text.toString()
        val benefit = binding.etBenefit.text.toString()
        val harga = binding.etHarga.text.toString().toIntOrNull() ?: 0
        val lokasi = binding.etLokasi.text.toString()
        val kuota = binding.etKuota.text.toString().toIntOrNull() ?: 0

        if (eventId != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("TiketEvents").child(eventId!!)
            databaseRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Log.d("DetailEventActivity", "Key $eventId ditemukan di Firebase")
                    // Lanjutkan update
                    val updatedData = mapOf(
                        "nama_event" to namaEvent,
                        "batas_akhir" to batasAkhir,
                        "waktu_mulai" to waktuMulai,
                        "deskripsi" to deskripsi,
                        "benefit" to benefit,
                        "harga" to harga,
                        "lokasi" to lokasi,
                        "kuota" to kuota,
                        "status_event" to statusEvent
                    )
                    databaseRef.updateChildren(updatedData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal memperbarui data: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e("DetailEventActivity", "Key $eventId tidak ditemukan di Firebase")
                    Toast.makeText(this, "ID Event tidak ditemukan di Firebase", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun toggleEventStatus() {
        statusEvent = if (statusEvent == "active") "finish" else "active"
        binding.statusEventTxt.text = statusEvent.capitalize()
    }
}
