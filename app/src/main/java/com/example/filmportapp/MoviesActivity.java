package com.example.filmportapp;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.filmportapp.utils.Constants;
//import com.example.filmportapp.utils.PreferenceUtils;



//Class used by trivia winner to chose which movie to watch
public class MoviesActivity extends AppCompatActivity {
    public static DatabaseHelper sqLiteHelper;
    ImageButton card_nature;
    ImageButton card_urban;
    ImageButton card_artsy;
    ImageButton card_spooky;
    String category;
    //movie links to be used with Youtube API
    public String hp = "jfdZd0yx05o";
    public String terminator = "1JPxRU4y19w";
    public String javamovie = "RnqAXuLZlaE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        //init textviw and buttons
        final TextView seekBarValue = findViewById(R.id.distanceText);
        TextView welcomeMsg = findViewById(R.id.welcomeText);
        welcomeMsg.setText("CONGRATULATIONS " + Constants.KEY_EMAIL +"! You Won the Quiz!\nPick a movie to watch:");
        int userScore = Constants.KEY_MYSCORE;
        seekBarValue.setText("Your Score: " + userScore);
        card_nature = findViewById(R.id.ib_terminator);
        card_urban = findViewById(R.id.ib_java);
        card_artsy = findViewById(R.id.ib_artsy);
        card_spooky = findViewById(R.id.ib_hp);


        //wait for user input for movie selection
        //call appropriate movie for the button they pick
        card_nature.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MoviesActivity.this, HostActivity.class);
                intent.putExtra("MOVIE_ID", terminator);
                startActivity(intent);

            }
        });

        card_urban.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //category = "urban";
                Intent intent = new Intent(MoviesActivity.this, HostActivity.class);
                intent.putExtra("MOVIE_ID", javamovie);
                startActivity(intent);

            }
        });

        card_artsy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                category = "artsy";
            }
        });

        card_spooky.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //category = "spooky";
                Intent intent = new Intent(MoviesActivity.this, HostActivity.class);
                intent.putExtra("MOVIE_ID", hp);
                startActivity(intent);
            }
        });

    }

}

