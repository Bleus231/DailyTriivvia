package com.example.dailytriivvia.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dailytriivvia.R
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.sign


class LoginActivity : AppCompatActivity() {

    private lateinit var splashImage:ImageView
    private lateinit var login: Button
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        //Firebase authentication
        auth = FirebaseAuth.getInstance()


        splashImage = findViewById(R.id.splash_imageView)
        login = findViewById(R.id.login_button)
        animateZoomIn()

        //Initializing variables for username and password
        val emailEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signUpButton = findViewById<Button>(R.id.sign_up_button)
        val guestPlayButton = findViewById<Button>(R.id.guest_button)


        // Login Button Logic
        login.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            //signIn("test@example.com", "Password123") // Use hardcoded values for now
            signIn(email,password)
        }

        // Sign up Button Logic
        signUpButton.setOnClickListener {
            signUp("test@example.com", "Password123") // Use hardcoded values for now
        }


        // Guest sign-in logic
        guestPlayButton.setOnClickListener {
            signInAnonymously()
        }



    }

    private fun animateZoomIn(){
        splashImage.animate()
            .scaleX(17f)
            .scaleY(17f)
            .setDuration(2000)
            .withEndAction{
                //Start main activity
               // startNewActivity()

            }
            .start()
    }

    private fun startNewActivity(){
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-up success
                    val user = auth.currentUser
                    Toast.makeText(this, "Sign-up Successful: ${user?.email}", Toast.LENGTH_SHORT).show()
                    startNewActivity()
                } else {
                    // Sign-up failed
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome: ${user?.email}", Toast.LENGTH_SHORT).show()
                    startNewActivity()
                } else {
                    // Sign-in failed
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signInAnonymously() {
        val auth = FirebaseAuth.getInstance() // Ensure `auth` is properly initialized

        val currentUser = auth.currentUser

        if (currentUser != null && currentUser.isAnonymous) {


            // Sign out the current anonymous user
            auth.signOut()

            // Sign in as a new anonymous guest
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign-in successful
                        val newUser = auth.currentUser
                        Toast.makeText(this, "Signed in as new Guest: ${newUser?.uid}",
                            Toast.LENGTH_SHORT).show()
                        startNewActivity()
                    } else {
                        // Sign-in failed
                        Toast.makeText(this, "Guest Sign-in Failed: " +
                                "${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // User is not logged in, proceed to sign in anonymously
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign-in successful
                        val newUser = auth.currentUser
                        Toast.makeText(this, "Signed in as Guest: ${newUser?.uid}"
                            , Toast.LENGTH_SHORT).show()
                        startNewActivity()
                    } else {
                        // Sign-in failed
                        Toast.makeText(this, "Guest Sign-in Failed: ${task.exception?.message}"
                            , Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

}