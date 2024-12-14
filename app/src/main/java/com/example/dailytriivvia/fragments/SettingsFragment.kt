package com.example.dailytriivvia.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.dailytriivvia.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : DialogFragment() {


    private var amount = 10 // Default value
    private var difficulty = 0
    private var category = 0
    private var multipleChoice: Boolean = true
        set(value) {
            field = value
            Log.d("SettingActivity", "MultipleChoice updated to: $value") // Log when updated
        }

    private val PREFERENCES_NAME = "SettingPreferences"
    private val SEEK_BAR_VALUE_KEY = "SeekBarValue"
    private val SWITCH_BAR_VALUE_KEY = "SwitchBarValue"
    private val QUESTION_DIFFICULTY_KEY = "DifficultySelection"
    private val CATEGORY_SELECTION_KEY = "CategorySelection"

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userSettingsRef: DocumentReference

    interface OnSeekBarValueChangeListener {
        fun onSeekBarValueChanged(value: Int)
    }

    private var seekbarListener: OnSeekBarValueChangeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        firestore = FirebaseFirestore.getInstance()

        if (context is OnSeekBarValueChangeListener) {
            seekbarListener = context
        } else {
            throw RuntimeException("$context must implement OnSeekBarValueChangeListener")
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")
        userSettingsRef = firestore.collection("users").
        document(userId).collection("settings").document("preferences")
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val settingLayout = inflater.inflate(R.layout.settings_page, container, false)

        // Find views
        val seekBar = settingLayout.findViewById<SeekBar>(R.id.seekBar)
        val numOfQuestionsTextView = settingLayout.
        findViewById<TextView>(R.id.num_of_questions_text_view)
        val trueFalseMultipleSwitch = settingLayout.
        findViewById<Switch>(R.id.true_false_multiple_switch)
        val questionDifficultySpinner = settingLayout.
        findViewById<Spinner>(R.id.question_difficulty_spinner)
        val categorySelectionSpinner = settingLayout.
        findViewById<Spinner>(R.id.category_selection_spinner)
        val saveButton = settingLayout.findViewById<Button>(R.id.saveButton)

        // Load saved settings from Firestore
        loadSettingsFromFirestore(seekBar, numOfQuestionsTextView, trueFalseMultipleSwitch,
            questionDifficultySpinner, categorySelectionSpinner)

        // Handle interactions
        handleSeekBar(seekBar, numOfQuestionsTextView)
        handleSwitch(trueFalseMultipleSwitch)
        handleSpinners(settingLayout)

        // Handle Close Button click
        saveButton.setOnClickListener {
            saveSettingsToFirestore()
            dismiss()
        }

        return settingLayout
    }

    override fun onResume() {
        super.onResume()
        val window = dialog?.window
        val params = window?.attributes
        val displayMetrics = requireContext().resources.displayMetrics

        params?.width = (displayMetrics.widthPixels * 0.85).toInt()
        params?.height = (displayMetrics.heightPixels * 0.85).toInt()

        window?.attributes = params
    }

    //Handling Values
    private fun handleSeekBar(seekBar: SeekBar, numOfQuestionsTextView: TextView) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                numOfQuestionsTextView.text = "Number of questions: $progress"
                amount = progress
                seekbarListener?.onSeekBarValueChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    private fun handleSwitch(switch: Switch) {
        switch.setOnCheckedChangeListener { _, isChecked ->
            multipleChoice = isChecked
        }
    }
    private fun handleSpinners(settingLayout: View) {
        val questionDifficultySpinner = settingLayout.
        findViewById<Spinner>(R.id.question_difficulty_spinner)
        val categorySelectionSpinner = settingLayout.
        findViewById<Spinner>(R.id.category_selection_spinner)

        questionDifficultySpinner.onItemSelectedListener=
            object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected
                        (parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                difficulty = position
                saveDifficultyValue(QUESTION_DIFFICULTY_KEY, position)
                Log.d("SettingActivity", "Difficulty spinner value saved: $position")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        categorySelectionSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected
                        (parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                category = position // Update the member variable
                saveSpinnerValue(CATEGORY_SELECTION_KEY, position)
                Log.d("SettingActivity", "Category spinner value saved: $position")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }



    //Saving values
    private fun saveSettingsToFirestore() {
        val settings = mapOf(
            SEEK_BAR_VALUE_KEY to amount,
           SWITCH_BAR_VALUE_KEY to multipleChoice,
            QUESTION_DIFFICULTY_KEY to difficulty,
            CATEGORY_SELECTION_KEY to category
        )

        userSettingsRef.set(settings)
            .addOnSuccessListener {
                Log.d("SettingsFragment", "Settings saved successfully to Firestore")
                Log.d("SettingsFragment", "Num of Questions saved: $amount")
                Log.d("SettingsFragment", "MultipleChoice status: $multipleChoice")
                Log.d("SettingsFragment", "difficulty saved: $difficulty")
                Log.d("SettingsFragment", "category saved: $category")

            }
            .addOnFailureListener { e ->
                Log.e("SettingsFragment", "Failed to save settings to Firestore", e)
            }
    }
    private fun saveSeekBarValue(value: Int) {
        val sharedPreferences = requireContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(SEEK_BAR_VALUE_KEY, value)
            commit() // Use commit for debugging
        }
    }
    private fun saveSwitchValue(value: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(SWITCH_BAR_VALUE_KEY, value)
            commit() // Use commit for debugging
        }
    }
    private fun saveDifficultyValue(key: String, value: Int) {
        val sharedPreferences = requireContext().
        getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(key, value)
            commit() // Use commit for debugging
        }
    }
    private fun saveSpinnerValue(key: String, value: Int) {
        val sharedPreferences = requireContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(key, value)
            commit() // Use commit for debugging
        }
    }


    //Loading saved values from Firestore
    private fun loadSettingsFromFirestore(seekBar: SeekBar, numOfQuestionsTextView: TextView,
        switch: Switch, difficultySpinner: Spinner, categorySpinner: Spinner) {
        userSettingsRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    amount = document.getLong(SEEK_BAR_VALUE_KEY)?.toInt() ?: 10
                    multipleChoice = document.getBoolean(SWITCH_BAR_VALUE_KEY) ?: false
                    difficulty = document.getLong(QUESTION_DIFFICULTY_KEY)?.toInt() ?: 0
                    category = document.getLong(CATEGORY_SELECTION_KEY)?.toInt() ?: 0

                    // Apply settings to UI
                    seekBar.progress = amount
                    numOfQuestionsTextView.text = "Number of questions: $amount"
                    switch.isChecked = multipleChoice
                    difficultySpinner.setSelection(difficulty)
                    Log.e("SettingActivity","Difficulty saved is: $difficulty")
                    categorySpinner.setSelection(category)
                    Log.e("SettingActivity","Category saved is: $category")
                } else {
                    // Use default values if no data found
                    seekBar.progress = amount
                    numOfQuestionsTextView.text = "Number of questions: $amount"
                    switch.isChecked = multipleChoice
                    difficultySpinner.setSelection(0)
                    categorySpinner.setSelection(0)
                }
            }
            .addOnFailureListener { e ->
                Log.e("SettingsFragment", "Failed to load settings from Firestore", e)
            }
    }

    /**
     *  override fun onDismiss(dialog: DialogInterface) {
     *         super.onDismiss(dialog)
     *         (activity as? MainActivity)?.refreshPreferences()
     *     }
     */

}


