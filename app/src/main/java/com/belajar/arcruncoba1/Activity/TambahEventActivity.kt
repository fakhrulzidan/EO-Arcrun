package com.belajar.arcruncoba1.Activity

import android.R
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.belajar.arcruncoba1.databinding.ActivityTambahEventBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class TambahEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTambahEventBinding
    private var selectedImageUri: Uri? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val kategoriList = listOf("Lari 5K", "Lari 10K", "Maraton")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.etKategori.adapter = adapter

        // Handler untuk tanggal
        binding.etDlEvent.setOnClickListener {
            showDatePicker { date -> binding.etDlEvent.setText(date) }
        }
        binding.etMulaiEvent.setOnClickListener {
            showDatePicker { date -> binding.etMulaiEvent.setText(date) }
        }

        // Handler untuk memilih gambar
        binding.btnPilihGambar.setOnClickListener {
            pickImageFromGallery()
        }

        binding.saveBtn.setOnClickListener {
            saveEventToFirebase()
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadUserName(currentUser.uid)
        } else {
            Toast.makeText(this, "User belum login.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadUserName(userId: String) {
        val userRef = database.getReference("users/$userId")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userName = snapshot.child("name").getValue(String::class.java)
                    if (!userName.isNullOrEmpty()) {
                        binding.namaUser.text = userName
                    } else {
                        binding.namaUser.text = "User" // Default jika nama kosong
                    }
                } else {
                    Toast.makeText(this@TambahEventActivity, "Data user tidak ditemukan.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TambahEventActivity, "Gagal memuat nama user: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d-%02d-%d", dayOfMonth, month + 1, year)
            onDateSelected(formattedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissions = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            requestPermissions(permissions.toTypedArray(), REQUEST_PERMISSION)
            return false
        }

        return true
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
        if (checkAndRequestPermissions()) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            if (selectedImageUri != null) {

                Glide.with(this)
                    .load(selectedImageUri)
                    .apply(RequestOptions().transform(RoundedCorners(30)))
                    .into(binding.ivPreviewGambar)

                binding.ivPreviewGambar.visibility = android.view.View.VISIBLE
            } else {
                Toast.makeText(this, "Gagal mendapatkan gambar dari galeri.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToLocalDirectory(uri: Uri): String? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val file = File(filesDir, "event_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace() // Log error ke logcat
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun saveEventToFirebase() {
        val namaEvent = binding.etTittle.text.toString()
        val tanggalAkhirDaftar = binding.etDlEvent.text.toString()
        val tanggalMulai = binding.etMulaiEvent.text.toString()
        val deskripsi = binding.etDeskripsi.text.toString()
        val benefit = binding.etBenefit.text.toString()
        val kategori = binding.etKategori.selectedItem.toString()
        val harga = binding.etHarga.text.toString().toDoubleOrNull()
        val lokasi = binding.etLokasi.text.toString()
        val kuota = binding.etKuota.text.toString().toIntOrNull()

        if (namaEvent.isEmpty() || tanggalAkhirDaftar.isEmpty() || tanggalMulai.isEmpty() || deskripsi.isEmpty() || benefit.isEmpty()
            || kategori.isEmpty() || harga == null || lokasi.isEmpty() || kuota == null || selectedImageUri == null
        ) {
            Toast.makeText(this, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        val localImagePath = saveImageToLocalDirectory(selectedImageUri!!)

        if (localImagePath == null) {
            Toast.makeText(this, "Gagal menyimpan gambar, coba lagi.", Toast.LENGTH_SHORT).show()
            return
        }

        val tiketId = FirebaseDatabase.getInstance().getReference("TiketEvents").push().key ?: return
        val event = mapOf(
            "nama_event" to namaEvent,
            "batas_akhir" to tanggalAkhirDaftar,
            "waktu_mulai" to tanggalMulai,
            "deskripsi" to deskripsi,
            "benefit" to benefit,
            "kategori" to kategori,
            "harga" to harga,
            "lokasi" to lokasi,
            "kuota" to kuota,
            "gambar" to localImagePath,
            "status_event" to "active"
        )

        // Menyimpan event ke Firebase
        FirebaseDatabase.getInstance().getReference("TiketEvents")
            .child(tiketId)
            .setValue(event)
            .addOnSuccessListener {
                // Menyimpan relasi event dengan user
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    FirebaseDatabase.getInstance()
                        .getReference("users/$userId/event")
                        .child(tiketId)
                        .setValue(true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Event berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menyimpan relasi user-event.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan event, coba lagi.", Toast.LENGTH_SHORT).show()
            }
    }
    companion object {
        const val REQUEST_IMAGE_PICK = 1001
        const val REQUEST_PERMISSION = 1002
    }
}

//class TambahEventActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityTambahEventBinding
//    private var selectedImageUri: Uri? = null
//    private lateinit var database: FirebaseDatabase
//    private lateinit var auth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityTambahEventBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val kategoriList = listOf("Lari 5K", "Lari 10K", "Maraton")
//        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, kategoriList)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.etKategori.adapter = adapter
//
//        // Handler untuk tanggal
//        binding.etDlEvent.setOnClickListener {
//            showDatePicker {
//                date -> binding.etDlEvent.setText(date)
//            }
//        }
//        binding.etMulaiEvent.setOnClickListener {
//            showDatePicker {
//                date -> binding.etMulaiEvent.setText(date)
//            }
//        }
//
//        // Handler untuk memilih gambar
//        binding.btnPilihGambar.setOnClickListener {
//            pickImageFromGallery()
//        }
//
//        binding.saveBtn.setOnClickListener {
//            saveEventToFirebase()
//        }
//
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            loadUserName(currentUser.uid)
//        } else {
//            Toast.makeText(this, "User belum login.", Toast.LENGTH_SHORT).show()
//            finish()
//        }
//    }
//
//    private fun loadUserName(userId: String) {
//        val userRef = database.getReference("users/$userId")
//
//        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    val userName = snapshot.child("name").getValue(String::class.java)
//                    if (!userName.isNullOrEmpty()) {
//                        binding.namaUser.text = userName
//                    } else {
//                        binding.namaUser.text = "User" // Default jika nama kosong
//                    }
//                } else {
//                    Toast.makeText(this@TambahEventActivity, "Data user tidak ditemukan.", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@TambahEventActivity, "Gagal memuat nama user: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }


    //    private fun saveEventToFirebase() {
//        val namaEvent = binding.etTittle.text.toString()
//        val tanggalAkhirDaftar = binding.etDlEvent.text.toString()
//        val tanggalMulai = binding.etMulaiEvent.text.toString()
//        val deskripsi = binding.etDeskripsi.text.toString()
//        val benefit = binding.etBenefit.text.toString()
//        val kategori = binding.etKategori.selectedItem.toString()
//        val harga = binding.etHarga.text.toString().toDoubleOrNull()
//        val lokasi = binding.etLokasi.text.toString()
//        val kuota = binding.etKuota.text.toString().toIntOrNull()
//
//        if (namaEvent.isEmpty() || tanggalAkhirDaftar.isEmpty() || tanggalMulai.isEmpty() || deskripsi.isEmpty() || benefit.isEmpty()
//            || kategori.isEmpty() || harga == null || lokasi.isEmpty() || kuota == null || selectedImageUri == null
//        ) {
//            Toast.makeText(this, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val localImagePath = saveImageToLocalDirectory(selectedImageUri!!)
//
//        if (localImagePath == null) {
//            Toast.makeText(this, "Gagal menyimpan gambar, coba lagi.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val tiketId = FirebaseDatabase.getInstance().getReference("TiketEvents").push().key ?: return
//        val event = mapOf(
//            "nama_event" to namaEvent,
//            "batas_akhir" to tanggalAkhirDaftar,
//            "waktu_mulai" to tanggalMulai,
//            "deskripsi" to deskripsi,
//            "benefit" to benefit,
//            "kategori" to kategori,
//            "harga" to harga,
//            "lokasi" to lokasi,
//            "kuota" to kuota,
//            "gambar" to localImagePath,
//            "status_event" to "active"
//        )
//
//        FirebaseDatabase.getInstance().getReference("TiketEvents")
//            .child(tiketId)
//            .setValue(event)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Event berhasil disimpan!", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this, MainActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(intent)
//                finish()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Gagal menyimpan event, coba lagi.", Toast.LENGTH_SHORT).show()
//            }
//    }
