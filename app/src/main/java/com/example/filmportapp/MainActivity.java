package com.example.filmportapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.filmportapp.utils.Constants;

public class MainActivity extends AppCompatActivity {

    Button btnNew, btnJoin;
    EditText etRoom, etCount;
    String roomid;
    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init buttons
        btnNew = (Button) findViewById(R.id.btnNew);
        btnJoin = (Button) findViewById(R.id.btnJoin);
        //init edittext
        etCount = (EditText)findViewById(R.id.etPCount);
        etRoom = (EditText)findViewById(R.id.etRoom);
        etCount.setHint("Enter Capacity of the room");
        etRoom.setHint("Room ID");
        //init client username
        username = (TextView) findViewById(R.id.username);
        username.setText("Welcome to FilmPort " +Constants.KEY_EMAIL+ "!\n What would you like to do?");

        //btn on click listeners
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get room id from edit text
                 roomid = etRoom.getText().toString();
                 //go to activity lobby for quiz
                Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                intent.putExtra("COUNT", 0);
                intent.putExtra("ROOM_ID", roomid);
                intent.putExtra("CONN_TYPE", "client");
                startActivity(intent);
            }
        });

        //if they want to start a new room
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //generate new room id
               int count = Integer.parseInt(etCount.getText().toString()); //getting count of people joining
                 roomid = genRoom(); //generate new room id
                //start an intent and pass through informationw
                Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                intent.putExtra("COUNT", count);
                intent.putExtra("CONN_TYPE", "host");
                intent.putExtra("ROOM_ID", roomid);
                startActivity(intent);

            }
        });



    }


    public String genRoom() {
        String roomID = ""; //to save roomID

        try{
            
          
            //All possible characters of the roomID
            //String AllChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789!?@#$%^&*()";
            String AllChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            //to make a string of 8 random characters
            StringBuilder captchaMaker = new StringBuilder(8);

            //for loop, will create a rabndom number between 0 and wthe length of our AllChars String
            //it will then locate the character at that point in the String and add it to our  roomID
            for (int i = 0; i < 3; i ++){
                int randomNum = (int)(AllChars.length()*Math.random());
                captchaMaker.append(AllChars.charAt(randomNum));
            }
            roomID = captchaMaker.toString();
        }
        catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return roomID; //send roomID
    }
}