package edu.miracosta.cs134.jmoebius;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.miracosta.cs134.jmoebius.model.JSONLoader;
import edu.miracosta.cs134.jmoebius.model.Superhero;

/**
 * Quiz style learning tool for us superheroes to get to know each other a little better. It has 3
 * different quiz settings: 1) Guess Superhero Name 2) Guess Superpower and 3) Guess The One Thing
 * 
 * @author Jacob Moebius
 * @version 1.0
 */
public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "Superheroes";
    private static final int SUPERHEROES_IN_QUIZ = 10;
    public static final String PREF_QUIZ_TYPE = "pref_quizType";

    // Keeps track of the defaults for current number of choices and quiz type
    private int mChoices = 4;
    private String mQuizType = "Superhero Name";

    // Keeps track of the current superhero selected
    private Button[] mButtons = new Button[4];
    private List<Superhero> mAllSuperheroesList; // all the superheroes loaded from JSON
    private List<Superhero> mQuizSuperheroesList; // superheroes in current quiz (just 10 of them)
    private Superhero mCorrectSuperhero; // correct superhero for the current question
    private String mCorrectSuperHeroAnswer = "";
    private int mTotalGuesses; // number of total guesses made
    private int mCorrectGuesses; // number of correct guesses
    private SecureRandom rng; // used to randomize the quiz
    private Handler handler; // used to delay loading next superhero
    private TextView mQuestionNumberTextView; // shows current question #
    private ImageView mSuperheroImageView; // displays a superhero
    private TextView mAnswerTextView; // displays correct answer
    private TextView mGuessSuperHeroTextView;

    // Create MediaPlayer objects to handle the correct and incorrect answer sounds
    private MediaPlayer mediaPlayerCorrectAnswer;
    private MediaPlayer mediaPlayerIncorrectAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mQuizSuperheroesList = new ArrayList<>(SUPERHEROES_IN_QUIZ);
        rng = new SecureRandom();
        handler = new Handler();

        // Get references to GUI components (textviews and imageview)
        mQuestionNumberTextView = findViewById(R.id.questionNumberTextView);
        mSuperheroImageView = findViewById(R.id.superheroImageView);
        mAnswerTextView = findViewById(R.id.answerTextView);
        mGuessSuperHeroTextView = findViewById(R.id.guessSuperHeroTextView);

        // Put all buttons in the array (mButtons)
        mButtons[0] = findViewById(R.id.button);
        mButtons[1] = findViewById(R.id.button2);
        mButtons[2] = findViewById(R.id.button3);
        mButtons[3] = findViewById(R.id.button4);

        //  Set mQuestionNumberTextView's text to the appropriate strings.xml resource
        mQuestionNumberTextView.setText(getString(R.string.question, 1, SUPERHEROES_IN_QUIZ));

        //  Load all the superheroes from the JSON file using the JSONLoader
        try {
            mAllSuperheroesList = JSONLoader.loadJSONFromAsset(this);
        } catch (IOException e) {
            Log.e(TAG, "Error loading from JSON", e);
        }

        // Attach preference listener
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

        // Instantiate MediaPlayer objects with the correct and incorrect answer sounds
        mediaPlayerCorrectAnswer = MediaPlayer.create(this, R.raw.success);
        mediaPlayerIncorrectAnswer = MediaPlayer.create(this, R.raw.failed);

        // Call the method resetQuiz() to start the quiz.
        resetQuiz();
    }

    /**
     * Sets up and starts a new quiz.
     */
    private void resetQuiz() {
        // Reset the number of correct guesses made
        mCorrectGuesses = 0;

        // Reset the total number of guesses the user made
        mTotalGuesses = 0;

        // Clear list of quiz superheroes (for prior games played)
        mQuizSuperheroesList.clear();

        // Randomly add FLAGS_IN_QUIZ (10) superheroes from the mAllSuperheroesList into the
        // mQuizSuperheroesList
        Superhero random;
        while (mQuizSuperheroesList.size() < SUPERHEROES_IN_QUIZ) {
            random = mAllSuperheroesList.get(rng.nextInt(mAllSuperheroesList.size()));
            // Ensure no duplicate superheroes (e.g. don't add a superhero if it's already in
            // mQuizSuperheroesList)
            if (!mQuizSuperheroesList.contains(random)) {
                mQuizSuperheroesList.add(random);
            }
        }

        switch(mQuizType) {
            case "Superhero Name":
                mGuessSuperHeroTextView.setText(R.string.guess_superhero);
                break;

            case "Superpower":
                mGuessSuperHeroTextView.setText(R.string.guess_superpower);
                break;

            case "One Thing":
                mGuessSuperHeroTextView.setText(R.string.guess_one_thing);
                break;
        }

        // Start the quiz by calling loadNextFlag
        loadNextSuperhero();
    }

    /**
     * Method initiates the process of loading the next superhero for the quiz, showing
     * the superhero's image and then all buttons, one of which contains the correct answer.
     */
    private void loadNextSuperhero() {
        // Initialize the mCorrectSuperhero by removing the item at position 0 in the
        // mQuizSuperheroesList
        mCorrectSuperhero = mQuizSuperheroesList.remove(0);

        // Get the correct superhero's info based on the quiz type
        switch(mQuizType) {
            case "Superhero Name":
                mCorrectSuperHeroAnswer = mCorrectSuperhero.getName();
                break;

            case "Superpower":
                mCorrectSuperHeroAnswer = mCorrectSuperhero.getSuperpower();
                break;

            case "One Thing":
                mCorrectSuperHeroAnswer = mCorrectSuperhero.getOneThing();
                break;
        }

        // Clear the mAnswerTextView so that it doesn't show text from the previous question
        mAnswerTextView.setText("");

        // Display current question number in the mQuestionNumberTextView
        mQuestionNumberTextView.setText(getString(
                R.string.question,
                SUPERHEROES_IN_QUIZ - mQuizSuperheroesList.size(),
                SUPERHEROES_IN_QUIZ));

        // Use AssetManager to load next image from assets folder
        AssetManager am = getAssets();

        // Get an InputStream to the asset representing the next flag
        try {
            InputStream stream = am.open(mCorrectSuperhero.getFileName());

            // Try to use the InputStream to create a Drawable
            // The file name can be retrieved from the correct superhero's file name
            Drawable image = Drawable.createFromStream(stream, mCorrectSuperhero.getName());

            // Set the image drawable to the correct flag
            mSuperheroImageView.setImageDrawable(image);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image from file: " + mCorrectSuperhero.getFileName(), e);
        }

        // Shuffle the order of all the superheroes (use Collections.shuffle)
        Collections.shuffle(mAllSuperheroesList);

        // Call updateBasedOnQuizType() to update the button text reflecting the new quiz type
        updateBasedOnQuizType();
    }

    /**
     * Handles the click event of one of the buttons indicating the guess of a superhero's name to
     * match the superhero image displayed. If the guess is correct, the superhero's name,
     * superpower, or one thing (in GREEN) will be shown, followed by a slight delay of 2 seconds,
     * then the next flag will be loaded. Otherwise, the phrase "Incorrect Guess" will be shown in
     * RED and the button will be disabled.
     *
     * @param v     Button the user clicked
     */
    public void makeGuess(View v) {

        // Downcast the View v into a Button (since it's one of the buttons)
        Button clickedButton = (Button) v;

        // Get the superhero's name from the text of the button
        String guess = clickedButton.getText().toString();

        // increment the total number of guesses
        mTotalGuesses++;

        // If the guess matches the correct superhero's name, increment the number of correct
        // guesses, then display correct answer in green text. Also, disable all buttons (can't keep
        // guessing once it's correct)
        if (guess.equals(mCorrectSuperHeroAnswer)) {
            // Increment the number of correct guesses
            mCorrectGuesses++;

            // Disable all buttons (can't keep guessing once it's correct)
            for (int i = 0; i < mChoices; i++) {
                mButtons[i].setEnabled(false);
            }

            // Display correct answer in green text
            mAnswerTextView.setTextColor(getResources().getColor(R.color.correct_answer));
            mAnswerTextView.setText(mCorrectSuperhero.getName());

            // Play the mediaPlayerCorrectAnswer sound
            mediaPlayerCorrectAnswer.start();

            if (mCorrectGuesses < SUPERHEROES_IN_QUIZ) {
                // Code a delay (2000ms = 2 seconds) using a handler to load the next flag
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextSuperhero();
                    }
                }, 2000);
            }

            // Nested in this decision, if the user has completed all 10 questions, show an
            // AlertDialog with the statistics and an option to Reset Quiz
            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(
                        R.string.results,
                        mTotalGuesses,
                        (double) mCorrectGuesses / mTotalGuesses * 100));

                // Set positive button of the dialog
                // positive button = reset quiz
                builder.setPositiveButton(
                        getString(R.string.reset_quiz),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                resetQuiz();
                            }
                        });

                // To prevent user from getting stuck!
                builder.setCancelable(false);
                builder.create();
                builder.show();
            }
        }

        // Else, the answer is incorrect, so display "Incorrect Guess!" in red and disable just the
        // incorrect button.
        else
        {
            // Set the clicked button to disabled
            clickedButton.setEnabled(false);

            // Set the incorrect text and text color in the TextView
            mAnswerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
            mAnswerTextView.setText(getString(R.string.incorrect_answer));

            // Play the mediaPlayerIncorrectAnswer sound
            mediaPlayerIncorrectAnswer.start();
        }
    }

    /**
     * Changes the button text between Name, Superpower, and One Thing based on the quiz type
     * selected.
     */
    private void updateBasedOnQuizType() {
        // Loop through all buttons, enable them all and set them to the first mChoices superheroes
        // in the mAllSuperheroesList
        for (int i = 0; i < mChoices; i++) {
            mButtons[i].setEnabled(true);

            // Make sure not to pull mCorrectSuperhero info
            Superhero randomSuperhero = mAllSuperheroesList.get(i);
            if (mAllSuperheroesList.get(i).equals(mCorrectSuperhero)) {
                randomSuperhero = mAllSuperheroesList.get(i + 1);
            }

            // Get random superhero's info based on the quiz type
            switch(mQuizType) {
                case "Superhero Name":
                    mButtons[i].setText(randomSuperhero.getName());
                    break;

                case "Superpower":
                    mButtons[i].setText(randomSuperhero.getSuperpower());
                    break;

                case "One Thing":
                    mButtons[i].setText(randomSuperhero
                            .getOneThing()
                            .replaceAll("_", " "));
                    break;
            }
        }

        // After the loop, randomly replace one of the buttons with the name of the correct
        // superhero
        mButtons[rng.nextInt(mChoices)].setText(mCorrectSuperHeroAnswer);
    }

    /**
     * SHARED_PREFERENCES METHODS FOR SETTINGS MENU
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        return super.onOptionsItemSelected(item);
    }

    SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                // Constant that stores "pref_quizType" to match preferences.xml
                case PREF_QUIZ_TYPE:
                    mQuizType = sharedPreferences.getString(PREF_QUIZ_TYPE, mQuizType);
                    updateBasedOnQuizType();
                    break;
            }
            resetQuiz();
            Toast.makeText(QuizActivity.this, R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
        }
    };
}
