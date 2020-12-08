package com.example.filmportapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

;

import com.example.filmportapp.utils.Constants;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class QuizDoneActivity  extends AppCompatActivity {


    String connType = "";
    MqttAndroidClient client;
    int score = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizdone);

        //get intent parameters
        Intent intent = getIntent();
        connType = intent.getStringExtra("CONN_TYPE");
        score = intent.getIntExtra("SCORE", 0);

        //set up mqtt connection
        //broker
        String host1 = "tcp://broker.hivemq.com:1883";
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(QuizDoneActivity.this, host1, clientId);
        MqttConnectOptions options = new MqttConnectOptions();

        //connect to the broker
        try {
            IMqttToken token = client.connect(options);
            //set callback to handle messages
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(QuizDoneActivity.this , "Connected", Toast.LENGTH_SHORT).show();

                    //subcribe to receive msgs
                    subscription();

                    //pass the name of the user and their score as a message to the server
                    String name = Constants.KEY_EMAIL;
                    String msg = name+"&"+score;
                    pubMSG(msg);

                    //call back message handler
                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // filter through by topic
                            //if the receive host message
                            if ((message.toString()).equalsIgnoreCase("host")){
                                //they will be going to the movie selection page
                                Intent intent = new Intent(QuizDoneActivity.this,MoviesActivity.class);
                                startActivity(intent);
                            }
                            //if they receive client
                            else if ((message.toString()).equalsIgnoreCase("client")){
                                //they will go to the watching page and wait for the host
                                Intent intent = new Intent(QuizDoneActivity.this,ClientActivity.class);
                                startActivity(intent);
                            }


                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }
                    });


                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(QuizDoneActivity.this , "Failed to Connect", Toast.LENGTH_SHORT).show();

                }
            });


        } catch (MqttException e) {
            e.printStackTrace();
        }




    }



    //publish message to server with room ID in the topic so that they do not collide with other rooms msgs
    private void pubMSG(String msg){
        String topic = "filmport/trivia/whowon/"+Constants.ROOM_ID;
        String payload = msg;
        byte[] encodedPayload = new byte[0];
        try {

            client.publish(topic, payload.getBytes(), 0, false);
            Toast.makeText(this , "MSG Sent", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //subscribe to receive msgs from server with their room id and name, so that every instance gets an unique msg
    private void subscription (){
        String topic = "filmport/trivia/" + Constants.ROOM_ID + "/" +Constants.KEY_EMAIL;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    
}
