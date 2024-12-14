package com.example.dailytriivvia.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytriivvia.R
import com.example.dailytriivvia.models.QuestionsAdapter
import com.example.dailytriivvia.models.QuizQuestion
import com.example.dailytriivvia.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference


class ResultsActivity : AppCompatActivity() {

    private var amount = 10 // Default value
    private var difficulty = 0
    private var category = 0
    private var trueOrFalse: Boolean = false
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userSettingsRef: DocumentReference

    private val PREFERENCES_NAME = "SettingPreferences"
    private val SEEK_BAR_VALUE_KEY = "SeekBarValue"
    private val SWITCH_BAR_VALUE_KEY = "SwitchBarValue"
    private val QUESTION_DIFFICULTY_KEY = "DifficultySelection"
    private val CATEGORY_SELECTION_KEY = "CategorySelection"


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_results)
        firestore = FirebaseFirestore.getInstance()

        // Get the passed data
        val questions = intent.getSerializableExtra("questions") as? ArrayList<QuizQuestion>
        val correctAnswers = intent.getStringArrayListExtra("correctAnswers") // Retrieve correct answers
        val selectedOptions = intent.getStringArrayListExtra("selectedOptions") // Retrieve user-selected options
        val finalScore = intent.getIntExtra("score", 0)
        val numOfQuestions = questions?.size

        if (questions == null || correctAnswers == null || selectedOptions == null) {
            // Handle null case to prevent crash
            Toast.makeText(this, "Error: Data not received properly.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Display the final score
        val scoreTextView = findViewById<TextView>(R.id.results_text_view)
        scoreTextView.text = "Final Score: $finalScore/$numOfQuestions"

        // Prepare the data for the RecyclerView (question, correct answer, and selected option)
        val questionAnswerList = questions.mapIndexed { index, question ->
            "Q: ${Constants.decodeHtmlString(question.question)}\n" +
                    "Your Answer: ${selectedOptions[index]}\n" +
                    "Correct Answer: ${correctAnswers[index]}"
        }

        // Set up the RecyclerView to display the questions, user answers, and correct answers
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewQuestions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = QuestionsAdapter(questionAnswerList)

        // Set up buttons for navigation
        val mainMenu = findViewById<Button>(R.id.main_menu_button)
        val playAgain = findViewById<Button>(R.id.play_again_button)

        mainMenu.setOnClickListener { mainMenu() }
        playAgain.setOnClickListener { playAgain() }
    }

    // Navigate to Main Menu
    private fun mainMenu() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // Restart the quiz
    private fun playAgain() {
        val intent = Intent(this, QuizActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loadSettingsFromFirestore(seekBar: SeekBar, numOfQuestionsTextView: TextView,
                                          switch: Switch, difficultySpinner: Spinner, categorySpinner: Spinner
    ) {
        userSettingsRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    amount = document.getLong(SEEK_BAR_VALUE_KEY)?.toInt() ?: 10
                    trueOrFalse = document.getBoolean(SWITCH_BAR_VALUE_KEY) ?: false
                    difficulty = document.getLong(QUESTION_DIFFICULTY_KEY)?.toInt() ?: 0
                    category = document.getLong(CATEGORY_SELECTION_KEY)?.toInt() ?: 0

                    // Apply settings to UI
                    seekBar.progress = amount
                    numOfQuestionsTextView.text = "Number of questions: $amount"
                    switch.isChecked = trueOrFalse
                    difficultySpinner.setSelection(difficulty)
                    Log.e("SettingActivity","Difficulty saved is: $difficulty")
                    categorySpinner.setSelection(category)
                    Log.e("SettingActivity","Category saved is: $category")
                } else {
                    // Use default values if no data found
                    seekBar.progress = amount
                    numOfQuestionsTextView.text = "Number of questions: $amount"
                    switch.isChecked = trueOrFalse
                    difficultySpinner.setSelection(0)
                    categorySpinner.setSelection(0)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SettingsFragment", "Failed to load settings from Firestore", e)
            }
    }


}

