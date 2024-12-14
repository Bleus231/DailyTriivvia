package com.example.dailytriivvia.activities

import android.app.appsearch.StorageInfo
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytriivvia.R
import com.example.dailytriivvia.databinding.ActivityQuizBinding
import com.example.dailytriivvia.models.Picture
import com.example.dailytriivvia.models.QuestionsAdapter
import com.example.dailytriivvia.models.QuizQuestion
import com.example.dailytriivvia.utils.Constants
import com.example.dailytriivvia.utils.PictureClass
import com.example.dailytriivvia.utils.Utils
import com.squareup.picasso.Picasso

class QuizActivity : AppCompatActivity() {

    private lateinit var questionList: ArrayList<QuizQuestion>
    private var position = 0
    private var result = 0
    private lateinit var correctAnswer: String
    private var selectedOption: Boolean = false // Tracks if an option is selected
    private val correctAnswersList = ArrayList<String>() // Stores all correct answers
    private val selectedOptionsList = ArrayList<String>() // Stores all user-selected options
    private val pictureClass = PictureClass() // Instance of PictureClass for fetching images
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_quiz)

        imageView = findViewById(R.id.ivImage)

        val questionListSerializable = intent.getSerializableExtra("questionList")
        if (questionListSerializable is ArrayList<*>) {
            questionList = questionListSerializable.filterIsInstance<QuizQuestion>() as ArrayList<QuizQuestion>
        } else {
            Log.e("QuizActivity", "Question list is null or not of the expected type.")
            finish()
            return
        }

        setQuestion()
        setOptions()

        findViewById<Button>(R.id.btnNext).setOnClickListener {
            onNext()
        }
    }

    private fun setQuestion() {
        val triviaQuestion = findViewById<TextView>(R.id.tvQuestion)
        val decodedQuestion = Constants.decodeHtmlString(questionList[position].question)
        triviaQuestion.text = decodedQuestion

        // Fetch and display an image related to the question
        fetchImage("soccer")
    }

    private fun setOptions() {
        val question = questionList[position]
        val temp = Constants.getRandomOptions(question.correct_answer, question.incorrect_answers)
        val optionList = temp.second
        correctAnswer = temp.first

        val option1 = findViewById<TextView>(R.id.option1)
        val option2 = findViewById<TextView>(R.id.option2)
        val option3 = findViewById<TextView>(R.id.option3)
        val option4 = findViewById<TextView>(R.id.option4)

        option1.text = optionList[0]
        option2.text = optionList[1]

        if (question.type == "multiple") {
            option3.visibility = View.VISIBLE
            option4.visibility = View.VISIBLE
            option3.text = optionList[2]
            option4.text = optionList[3]
        } else {
            option3.visibility = View.GONE
            option4.visibility = View.GONE
        }

        resetOptionColors(option1, option2, option3, option4)

        val optionsTrueOrFalse = arrayOf(option1, option2)
        val optionsTrueOrFalseTexts = arrayOf(optionList[0], optionList[1])

        for (i in optionsTrueOrFalse.indices) {
            setOptionClickListenerTrueOrFalse(optionsTrueOrFalse, i, optionsTrueOrFalseTexts)
        }

        if (question.type == "multiple") {
            val optionsMultipleChoice = arrayOf(option1, option2, option3, option4)
            val optionsMultipleChoiceTexts = arrayOf(optionList[0], optionList[1], optionList[2], optionList[3])
            for (i in optionsMultipleChoice.indices) {
                setOptionClickListenerTrueOrFalse(optionsMultipleChoice, i, optionsMultipleChoiceTexts)
            }
        }
    }

    private fun fetchImage(keyword: String) {
        pictureClass.fetchImage(keyword, object : PictureClass.DataCallback {
            override fun onSuccess(picture: Picture) {
                runOnUiThread {
                    Picasso.get().load(picture.imageUrl).into(imageView)
                }
            }

            override fun onFailure(errorMessage: String) {
                runOnUiThread {
                    Toast.makeText(this@QuizActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun resetOptionColors(vararg options: TextView) {
        for (option in options) {
            option.setBackgroundColor(Color.WHITE) // Reset to default color
        }
    }

    private fun setOptionClickListenerTrueOrFalse(options: Array<TextView>, optionIndex: Int, optionTexts: Array<String>) {
        options[optionIndex].setOnClickListener {
            selectedOption = true
            for (i in options.indices) {
                if (optionTexts[i] == correctAnswer) {
                    options[i].setBackgroundColor(Color.GREEN)
                    if (i == optionIndex) {
                        result++
                    }
                } else {
                    options[i].setBackgroundColor(Color.RED)
                }
            }
            disableAllOptions()
        }
    }

    private fun disableAllOptions() {
        findViewById<TextView>(R.id.option1).isClickable = false
        findViewById<TextView>(R.id.option2).isClickable = false
        findViewById<TextView>(R.id.option3).isClickable = false
        findViewById<TextView>(R.id.option4).isClickable = false
    }

    private fun onNext() {
        if (!checkOptionSelected()) {
            return
        } else {
            selectedOption = false
        }

        correctAnswersList.add(correctAnswer)
        val selectedOptionText = findViewById<TextView>(R.id.option1).text.toString() // Replace with logic for selected option
        selectedOptionsList.add(selectedOptionText)

        if (position < questionList.size - 1) {
            position++
            setQuestion()
            setOptions()
        } else {
            Utils.showToast(this, "Final score is $result")
            val resultsPage = Intent(this, ResultsActivity::class.java)
            resultsPage.putExtra("questions", ArrayList(questionList))
            resultsPage.putExtra("correctAnswers", correctAnswersList)
            resultsPage.putExtra("selectedOptions", selectedOptionsList)
            resultsPage.putExtra("score", result)
            startActivity(resultsPage)
            finish()
        }
    }

    private fun checkOptionSelected(): Boolean {
        if (!selectedOption) {
            Toast.makeText(this, "You must select an option!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}


