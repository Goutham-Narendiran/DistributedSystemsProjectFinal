package com.example.filmportapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filmportapp.utils.Constants;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class HostActivity extends YouTubeBaseActivity {
    YouTubePlayerView mYoutubePlayerView;
    Button btnPlay, btnSkip, btnPause, btnRewind, btnSend;
    TextView tvMSG;
    EditText etMSG;
    YouTubePlayer.OnInitializedListener mOnInitialListener;
    String videoS2 = "RnqAXuLZlaE";
    String allMSG = "";
    String inputMSG = "";

    MqttAndroidClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
//get movie info from intent
        Intent intent = getIntent();
        videoS2 = intent.getStringExtra("MOVIE_ID");

        //broker
        String host1 = "tcp://broker.hivemq.com:1883";
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(HostActivity.this, host1, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
//start connection and set callback
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //subscribe to receive messages from clients
                    subscription();
                    // We are connected
                    Toast.makeText(HostActivity.this , "Connected", Toast.LENGTH_SHORT).show();
                    pubMSG(videoS2, (Constants.ROOM_ID+"movie")); //publish movie info into the room's movie topic

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(HostActivity.this , "Failed to Connect", Toast.LENGTH_SHORT).show();

                }
            });


        } catch (MqttException e) {
            e.printStackTrace();
        }


        //innit all buttons
        btnPlay = (Button)findViewById(R.id.btnPlay);
        btnPause = (Button)findViewById(R.id.btnPause);
        btnSkip = (Button)findViewById(R.id.btnSkip);
        btnRewind = (Button)findViewById(R.id.btnRewind);
        btnSend = (Button)findViewById(R.id.btnSend);
        tvMSG = (TextView)findViewById(R.id.tvMSG);
        tvMSG.setMovementMethod(new ScrollingMovementMethod());
        tvMSG.setText(allMSG);
        //edit text to get input for msgs
        etMSG = (EditText)findViewById(R.id.etMSG);
        etMSG.setHint("Enter message...");
        //send button for msgs
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //send users msgs with their name
                inputMSG = Constants.KEY_EMAIL+ ": " +etMSG.getText().toString();
                pubMSG(inputMSG, (Constants.ROOM_ID+"convo")); //publish in rooms convo topic
                allMSG += "\n"+inputMSG;
                etMSG.getText().clear(); //clear their input for next input
                etMSG.onEditorAction(EditorInfo.IME_ACTION_DONE);//dismiss keyboard when message sent

            }
        });

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //if the app receives any msgs prior to starting the movie, it will be filtered here
                if (topic.equalsIgnoreCase("video/sync/update/" +Constants.ROOM_ID+"convo")){
                    Toast.makeText(HostActivity.this , "MSG RECEIVED: NEW CHAT", Toast.LENGTH_SHORT).show();
                    tvMSG.append("\n"+message.toString()); //append the textview here to show new msgs
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


        //start youtube player thread when it receives the signal
        mYoutubePlayerView = (YouTubePlayerView)findViewById(R.id.YTvid);

        mOnInitialListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(videoS2); //load video

                //to skip 10 seconds
                btnSkip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        youTubePlayer.seekToMillis(youTubePlayer.getCurrentTimeMillis()+10000); //skip 10 seconds from current time

                        pubMSG("SKIP", Constants.ROOM_ID+"skip"); //inform clients to skip
                    }
                });
                //rewinf 10 seconds
                btnRewind.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        youTubePlayer.seekToMillis(youTubePlayer.getCurrentTimeMillis()-10000); //rewind 10 seconds from current time

                        pubMSG("RWND", Constants.ROOM_ID+"rwnd"); //publish to client to do the same
                    }
                });
                //play buttin
                btnPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //resumes playback and pubs to client to do the same
                        youTubePlayer.play();
                        pubMSG("PLAY", Constants.ROOM_ID+"play");
                    }
                });
                //pauses video
                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //pause and pub to client to do the same
                        youTubePlayer.pause();
                        pubMSG("PAUSE", Constants.ROOM_ID+"pause");
                    }
                });

                //set call back to handle chats after movie starts
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        //if message arrives after the movie started it will be handled here
                        if (topic.equalsIgnoreCase("video/sync/update/" + Constants.ROOM_ID+"convo")){
                            Toast.makeText(HostActivity.this , "MSG RECEIVED: NEW CHAT", Toast.LENGTH_SHORT).show();
                            tvMSG.append("\n"+message.toString());//append to show new msgs
                        }
                        //to sync the movie if client requestts a sync
                        else  if (topic.equalsIgnoreCase("video/sync/update/" + Constants.ROOM_ID+"syncreq")){
                            Toast.makeText(HostActivity.this , "MSG RECEIVED: SYNC REQ", Toast.LENGTH_SHORT).show();
                            pubMSG((youTubePlayer.getCurrentTimeMillis()+""), Constants.ROOM_ID+"syncnow"); //send current time back to client

                        }


                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });

            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

        //button to start movie
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pubMSG("START", Constants.ROOM_ID+"start"); //pub to client to start video
                mYoutubePlayerView.initialize("AIzaSyBMkd0FZt260WBoQGT2vCJk4qQFbixHzTE", mOnInitialListener); //start video

            }
        });






    }

    //will publish by updating the topic to match and with a msg to communicate with clients
    private void pubMSG(String msg, String topicid){
        String topic = "video/sync/update/"+topicid;
        String payload = msg;
        byte[] encodedPayload = new byte[0];
        try {
            ;
            client.publish(topic, payload.getBytes(), 0, false);
            Toast.makeText(this , "MSG Sent", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

//subscribe to receive updates from clients
    private void subscription (){
        String topic = "video/sync/update/#";
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



