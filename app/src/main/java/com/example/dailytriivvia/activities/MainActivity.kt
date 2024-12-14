package com.example.dailytriivvia.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dailytriivvia.R
import com.example.dailytriivvia.fragments.SettingsFragment
import com.example.dailytriivvia.models.Picture
import com.example.dailytriivvia.utils.PictureClass
import com.example.dailytriivvia.utils.QuizClass
import com.example.dailytriivvia.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), SettingsFragment.OnSeekBarValueChangeListener {


    private val SETTING_PREFERENCES = "SettingPreferences"
    private val SEEK_BAR_VALUE_KEY = "SeekBarValue"
    private val QUESTION_DIFFICULTY_KEY = "DifficultySelection"
    private val CATEGORY_SELECTION_KEY = "CategorySelection"
    private val SWITCH_BAR_VALUE_KEY = "SwitchBarValue"

    private var currentSeekBarValue: Int = 10 // Default value
    private var multipleChoice: Boolean = true
    private var difficultySelection: Int = 0
    private var categorySelection: Int = 0
    private lateinit var auth: FirebaseAuth
    private val pictureClass = PictureClass()
    private lateinit var imageView: ImageView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userSettingsRef: DocumentReference




    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()


        val settingButton = findViewById<ImageButton>(R.id.settingButton5)
        val playButton = findViewById<Button>(R.id.play_button)
        val quickPlay = findViewById<Button>(R.id.quick_play_button)
        val signOut = findViewById<Button>(R.id.sign_out_button)
        val editProfile = findViewById<Button>(R.id.edit_profile_button)
       // imageView = findViewById<ImageView>(R.id.ivImage)





        playButton.setOnClickListener{_ -> playQuiz() }
        quickPlay.setOnClickListener{_ -> quickPlayQuiz() }
        settingButton.setOnClickListener{_ -> openSetting() }
        signOut.setOnClickListener{_ -> signOut() }
        editProfile.setOnClickListener {_ ->editProfile() }




    }




    fun playQuiz() {



        var savedNumOfQuestions = 10
        var savedMultipleChoice = true
        var savedDifficulty = 0
        var savedCategory = 0


        //loadSettingsFromFirestore()
        firestore = FirebaseFirestore.getInstance() // Initialize Firestore
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")
        userSettingsRef = firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document("preferences")
        val quizClass = QuizClass(this)

        userSettingsRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve and update the settings values from Firestore
                    savedNumOfQuestions = document.getLong(SEEK_BAR_VALUE_KEY)?.toInt() ?: 10
                    savedMultipleChoice = document.getBoolean(SWITCH_BAR_VALUE_KEY) ?: false
                    savedDifficulty = document.getLong(QUESTION_DIFFICULTY_KEY)?.toInt() ?: 0
                    savedCategory = document.getLong(CATEGORY_SELECTION_KEY)?.toInt() ?: 0

                    // Log the retrieved values for debugging
                    Log.d(
                        "MainActivity", "Settings loaded: SeekBarValue=$savedNumOfQuestions, " +
                                "MultipleChoice=$savedMultipleChoice, Difficulty=$savedDifficulty, " +
                                "Category=$savedCategory"
                    )
                } else {
                    Utils.showToast(this, "No saved settings found.")
                }






                if (!savedMultipleChoice) {
                    //fetchImage("soccer")
                    quizClass.getQuizList(
                        savedNumOfQuestions, getCategory(savedCategory),
                        getDifficulty(savedDifficulty),
                        "boolean"
                    )
                    Log.e("MainActivity", "Number of Questions: $savedNumOfQuestions")
                    Log.e("MainActivity", "Question type is: True or False")
                    Log.e(
                        "MainActivity",
                        "Difficulty selection is ${getDifficulty(savedDifficulty)}"
                    )
                    Log.e("MainActivity", "Category selection is : $savedCategory")

                } else {
                    //fetchImage("soccer")
                    quizClass.getQuizList(
                        savedNumOfQuestions, getCategory(savedCategory),
                        getDifficulty(savedDifficulty),
                        "multiple"
                    )
                    Log.e("MainActivity", "Number of Questions: $savedNumOfQuestions")
                    Log.e("MainActivity", "Question type is: Multiple Choice")
                    Log.e(
                        "MainActivity",
                        "Difficulty selection is ${getDifficulty(savedDifficulty)}"
                    )
                    Log.e(
                        "MainActivity",
                        "Category selection is : ${getCategory(savedCategory)}"
                    )
                }
            }
    }


    fun quickPlayQuiz() {
        val quizClass = QuizClass(this)
        quizClass.getQuizList(10, null, null, null)
    }

    fun getDifficulty(value: Int): String? {
        return when (value) {
            1 -> "easy"
            2 -> "medium"
            3 -> "hard"
            else -> null
        }
    }

    fun getCategory(value: Int): Int? {
        return when (value) {
            1 -> 9
            2 -> 10
            3 -> 11
            4 -> 12
            5 -> 13
            6 -> 14
            7 -> 15
            8 -> 16
            9 -> 29
            10 -> 31
            11 -> 32
            12 -> 17
            13 -> 18
            14 -> 19
            15 -> 30
            16 -> 20
            17 -> 21
            18 -> 22
            19 -> 26
            20 -> 23
            21 -> 24
            22 -> 25
            23 -> 27
            24 -> 28
            else -> null
        }
    }


    fun openSetting(){
        //refreshPreferences()
        val dialog = SettingsFragment()
        dialog.show(supportFragmentManager, "SettingsDialog")
    }

    fun refreshPreferences() {
        val sharedPreferences = getSharedPreferences("SettingPreferences", Context.MODE_PRIVATE)
        currentSeekBarValue = sharedPreferences.getInt(SEEK_BAR_VALUE_KEY, 10) // Default is 3
        multipleChoice = sharedPreferences.getBoolean("SwitchBarValue", true) // Default to true
        difficultySelection =
            sharedPreferences.getInt(QUESTION_DIFFICULTY_KEY, 0) // Default to first option
        categorySelection =
            sharedPreferences.getInt(CATEGORY_SELECTION_KEY, 0) // Default to first option
    }

    override fun onSeekBarValueChanged(value: Int) {
        // Update the current SeekBar value dynamically
        currentSeekBarValue = value
        Log.d("MainActivity", "SeekBar value updated: $currentSeekBarValue")

        // Save the value in SharedPreferences
        val sharedPreferences = getSharedPreferences(SETTING_PREFERENCES, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(SEEK_BAR_VALUE_KEY, currentSeekBarValue)
            apply()
        }
    }

    fun signOut() {
        auth.signOut()
        Toast.makeText(this, "Signed Out Successfully", Toast.LENGTH_SHORT).show()
        val newIntent = Intent(this, LoginActivity::class.java).apply {}
        startActivity(newIntent)
    }

    private fun editProfile(){
        val profile = Intent(this,ProfileActivity::class.java)
        startActivity(profile)
    }

    private fun loadSettingsFromFirestore() {
        firestore = FirebaseFirestore.getInstance() // Initialize Firestore
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        // Reference to the user's settings document
        userSettingsRef = firestore.collection("users")
            .document(userId)
            .collection("settings")
            .document("preferences")

        userSettingsRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve and update the settings values from Firestore
                    currentSeekBarValue = document.getLong(SEEK_BAR_VALUE_KEY)?.toInt() ?: 10
                    multipleChoice = document.getBoolean(SWITCH_BAR_VALUE_KEY) ?: false
                    difficultySelection = document.getLong(QUESTION_DIFFICULTY_KEY)?.toInt() ?: 0
                    categorySelection = document.getLong(CATEGORY_SELECTION_KEY)?.toInt() ?: 0

                    // Log the retrieved values for debugging
                    Log.d("MainActivity", "Settings loaded: SeekBarValue=$currentSeekBarValue, " +
                            "MultipleChoice=$multipleChoice, Difficulty=$difficultySelection, " +
                            "Category=$categorySelection")
                } else {
                    Utils.showToast(this, "No saved settings found.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to load settings from Firestore", e)
            }
    }

    private fun fetchImage(keyword: String) {
        val imageView = findViewById<ImageView>(R.id.ivImage)
        pictureClass.fetchImage(keyword, object : PictureClass.DataCallback {
            override fun onSuccess(picture: Picture) {
                runOnUiThread {
                    Picasso.get().load(picture.imageUrl).into(imageView)
                }
            }

            override fun onFailure(errorMessage: String) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }




}