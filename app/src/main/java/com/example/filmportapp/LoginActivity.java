package com.example.filmportapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filmportapp.utils.Constants;

public class LoginActivity extends AppCompatActivity {
    DatabaseHelper db;
    EditText e1;
    Button b1;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = new DatabaseHelper(this);
        Constants.KEY_EMAIL= null;
        initViews();
        initListeners();


    }
    //initialize XML objects
    private void initViews(){
        e1 = findViewById(R.id.username);
        b1 = findViewById(R.id.Login_btn);
    }

    private void initListeners() {
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText e1;
                e1 = (EditText) findViewById(R.id.username);

                //get the user input for name
                String id = e1.getText().toString().trim();

                //pass the name through an intent
                Intent intent = new Intent(getBaseContext(), MainActivity.class);

                //save name in constants
                Constants.KEY_EMAIL = id;
                intent.putExtra("username", id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.KEY_EMAIL = null;
    }
}
