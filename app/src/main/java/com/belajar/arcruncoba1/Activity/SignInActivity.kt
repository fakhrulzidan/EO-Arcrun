package com.belajar.arcruncoba1.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.belajar.arcruncoba1.R
import com.belajar.arcruncoba1.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signInBtn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null) {
                            val userRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(currentUser.uid)

                            // Cek apakah data pengguna sudah ada
                            userRef.get().addOnSuccessListener { snapshot ->
                                if (!snapshot.exists()) {
                                    // Inisialisasi data pengguna baru
                                    val userData = mapOf(
                                        "email" to currentUser.email,
                                        "name" to (currentUser.displayName ?: "User"),
                                        "event" to mapOf<String, Boolean>() // Event kosong
                                    )
                                    userRef.updateChildren(userData)
                                        .addOnSuccessListener {
                                            Log.d("SignInActivity", "User data initialized successfully.")
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("SignInActivity", "Error initializing user data: ${exception.message}")
                                        }
                                } else {
                                    Log.d("SignInActivity", "User data already exists.")
                                }
                            }.addOnFailureListener { exception ->
                                Log.e("SignInActivity", "Error fetching user data: ${exception.message}")
                            }
                        }

                        // Berpindah ke MainActivity setelah login berhasil
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Login gagal: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "Masukkan Email dan Password Anda", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }
}//        binding.signInBtn.setOnClickListener {
//            val email = binding.etEmail.text.toString().trim()
//            val pass = binding.etPassword.text.toString().trim()
//
//            // Lakukan proses login
//            firebaseAuth.signInWithEmailAndPassword(email, pass)
//                .addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        val intent = Intent(this, MainActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    } else {
//                        Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }



//            if (email.isEmpty() || pass.isEmpty()) {
//                Toast.makeText(this, "Masukkan Email dan Password Anda.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//                Toast.makeText(this, "Format email tidak valid.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (pass.length < 6) {
//                Toast.makeText(this, "Password harus memiliki minimal 6 karakter.", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
