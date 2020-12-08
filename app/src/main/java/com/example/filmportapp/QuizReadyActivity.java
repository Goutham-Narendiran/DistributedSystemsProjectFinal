package com.example.filmportapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

;

import com.example.filmportapp.utils.Constants;

public class QuizReadyActivity  extends AppCompatActivity {
    private static final int REQUEST_CODE_QUIZ = 1;

    private TextView myScore;

    private int Score = 0;

    String connType = "";



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready);

        //get intent parameters
        Intent intent = getIntent();
        connType = intent.getStringExtra("CONN_TYPE");

        Button start = findViewById(R.id.btnStartQuiz);

        //once everyone joins, they will be in this activity, where they can start the quiz whenever they are ready
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuiz();
            }
        });

    }

    private void startQuiz(){
        Intent intent = new Intent(this,QuestionPage.class);
        intent.putExtra("CONN_TYPE", connType);
        startActivityForResult(intent, REQUEST_CODE_QUIZ);
    }

    private void updateScore(int newScore) {
        Score = newScore;
        myScore.setText("Your Score: " + Score);

        Constants.KEY_MYSCORE= Score;
    }
}
