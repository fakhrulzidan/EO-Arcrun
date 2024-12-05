package com.belajar.arcruncoba1.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.belajar.arcruncoba1.Adapter.EventAdapter
import com.belajar.arcruncoba1.Models.EventModels
import com.belajar.arcruncoba1.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.addBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, TambahEventActivity::class.java)
            startActivity(intent)
        }

        // Memastikan user login
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadUserName(currentUser.uid)
            loadUserEvents(currentUser.uid)
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
                        binding.namaUser.text = userName // Set nama user
                    } else {
                        binding.namaUser.text = "User" // Default jika nama kosong
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Data user tidak ditemukan.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal memuat nama user: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserEvents(userId: String) {
        val userRef = database.getReference("users/$userId/event")
        val eventRef = database.getReference("TiketEvents")
        val items = ArrayList<EventModels>()
        val eventIds = ArrayList<String>()

        // Ambil daftar event ID yang dimiliki user
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userEventIds = snapshot.children.mapNotNull { it.key }
                    eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(eventSnapshot: DataSnapshot) {
                            if (eventSnapshot.exists()) {
                                for (eventId in userEventIds) {
                                    val event = eventSnapshot.child(eventId).getValue(EventModels::class.java)
                                    if (event != null) {
                                        items.add(event)
                                        eventIds.add(eventId)
                                    }
                                }
                                setupRecyclerView(items, eventIds)
                            } else {
                                Toast.makeText(this@MainActivity, "Tidak ada event ditemukan.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@MainActivity, "Gagal memuat event: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this@MainActivity, "User tidak memiliki event.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal memuat data user: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView(items: ArrayList<EventModels>, eventIds: ArrayList<String>) {
        binding.viewEvent.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.viewEvent.adapter = EventAdapter(items, eventIds, this)
    }
}

//class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var database: FirebaseDatabase
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        database = FirebaseDatabase.getInstance()
//
//        binding.addBtn.setOnClickListener {
//            val intent = Intent(this@MainActivity, TambahEventActivity::class.java)
//            startActivity(intent)
//        }
//
//        initViewEvent()
//    }
//    private fun initViewEvent() {
//        val myRef: DatabaseReference = database.getReference("TiketEvents")
//        val items = ArrayList<EventModels>()
//        val eventIds = ArrayList<String>()
//
//        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    for (issue in snapshot.children) {
//                        val event = issue.getValue(EventModels::class.java)
//                        val eventId = issue.key
//                        if (event != null && eventId != null) {
//                            items.add(event)
//                            eventIds.add(eventId)
//                            Log.d("FirebaseData", "Loaded event: ${event.nama_event} with ID: $eventId")
//                        }
//                    }
//                    if (items.isNotEmpty()) {
//                        binding.viewEvent.layoutManager = LinearLayoutManager(
//                            this@MainActivity, LinearLayoutManager.VERTICAL, false
//                        )
//
//                        binding.viewEvent.adapter = EventAdapter(items, eventIds, this@MainActivity)
//                    } else {
//                        Toast.makeText(this@MainActivity, "No events found.", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(this@MainActivity, "No data in Firebase.", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@MainActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }
//
//}