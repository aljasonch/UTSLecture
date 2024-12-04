package com.example.utslecture.auth

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R
import com.example.utslecture.data.ProfileUser
import com.example.utslecture.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Login : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Activity Result Launcher for Google Sign-In
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as HomeActivity).hideBottomNavigation()

        val registerButton = view.findViewById<TextView>(R.id.register_text)
        val emailInput =
            view.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.email_input_field)
        val passwordInput =
            view.findViewById<androidx.appcompat.widget.AppCompatEditText>(R.id.password_input_field)
        val loginButton =
            view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.submit_button)
        val googleSignInButton =
            view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.button)

        auth = FirebaseAuth.getInstance()

        registerButton.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Email and Password are required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Log.d("Login", "signInWithEmail:success")
                            findNavController().navigate(R.id.Home)
                        } else {
                            Log.w("Login", "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                requireContext(),
                                "Email or Password Wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w("Google Sign In", "signInResult:failed code=" + e.statusCode)
            Toast.makeText(requireContext(), "Google Sign In Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("Login", "signInWithCredential:success")
                    val user = auth.currentUser
                    user?.let {
                        val uid = it.uid
                        val userDocRef = firestore.collection("users").document(uid)

                        // Cek apakah dokumen sudah ada
                        userDocRef.get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // Dokumen ada, update field tertentu kecuali 'name'
                                    val profileUpdates = hashMapOf<String, Any>(
                                        "email" to acct?.email.orEmpty()
                                        // Tambahkan field lain yang ingin diupdate, kecuali 'name'
                                    )

                                    userDocRef.set(profileUpdates, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Log.d(
                                                "Firestore",
                                                "Data ProfileUser berhasil diperbarui"
                                            )
                                            findNavController().navigate(R.id.Home)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                "Firestore",
                                                "Error memperbarui data ProfileUser",
                                                e
                                            )
                                            Toast.makeText(
                                                requireContext(),
                                                "Terjadi kesalahan saat memperbarui data.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    // Dokumen tidak ada, buat dokumen baru dengan semua field
                                    val profileUser = ProfileUser(
                                        userId = uid,
                                        email = acct?.email.orEmpty(),
                                        name = acct?.displayName.orEmpty()
                                        // Tambahkan field awal lainnya di sini
                                    )

                                    userDocRef.set(profileUser)
                                        .addOnSuccessListener {
                                            Log.d("Firestore", "Data ProfileUser berhasil disimpan")
                                            findNavController().navigate(R.id.Home)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                "Firestore",
                                                "Error menyimpan data ProfileUser",
                                                e
                                            )
                                            Toast.makeText(
                                                requireContext(),
                                                "Terjadi kesalahan saat menyimpan data.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error memeriksa dokumen", e)
                                Toast.makeText(
                                    requireContext(),
                                    "Terjadi kesalahan.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Log.w("Login", "signInWithCredential:failure", task.exception)
                    Toast.makeText(requireContext(), "Autentikasi gagal.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}