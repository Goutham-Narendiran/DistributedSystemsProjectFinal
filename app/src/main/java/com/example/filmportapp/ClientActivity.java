package com.example.filmportapp;


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

public class ClientActivity extends YouTubeBaseActivity {
    YouTubePlayerView mYoutubePlayerView;
    Button btnSync, btnSend;
    TextView tvMSG;
    EditText etMSG;
    YouTubePlayer.OnInitializedListener mOnInitialListener;
    String videoS2 = "RnqAXuLZlaE";
    String allMSG = "";
    String inputMSG = "";
    MqttAndroidClient client;
    YouTubePlayer youTubePlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        //init buttons
        btnSync = (Button)findViewById(R.id.btnSync);
        btnSend = (Button)findViewById(R.id.btnSend);
        tvMSG = (TextView)findViewById(R.id.tvMSG);
        //innit textview
        tvMSG.setMovementMethod(new ScrollingMovementMethod());
        tvMSG.setText(allMSG);
        //innit edit text for users input
        etMSG = (EditText)findViewById(R.id.etMSG);
        etMSG.setHint("Enter message...");
        //all msgs prior to movie starting handled here
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send user msg and name to all clients and host in room
                inputMSG = Constants.KEY_EMAIL+ ": " + etMSG.getText().toString();
                pubMSG(inputMSG, (Constants.ROOM_ID+"convo"));
                etMSG.getText().clear(); //clear input
                etMSG.onEditorAction(EditorInfo.IME_ACTION_DONE); //dismiss keyboard

            }
        });


        //broker
        String host1 = "tcp://broker.hivemq.com:1883";
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(ClientActivity.this, host1, clientId);
        MqttConnectOptions options = new MqttConnectOptions();

        //establish connection and set call back to handle incoming msgs
        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(ClientActivity.this , "Connected", Toast.LENGTH_SHORT).show();
                    //subscrtibe to topics
                    subscription();


                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(ClientActivity.this , "Failed to Connect", Toast.LENGTH_SHORT).show();

                }
            });


        } catch (MqttException e) {
            e.printStackTrace();
        }

        //handles incoming messages prior to movie starting
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            //msg to start the movie
                if (topic.equalsIgnoreCase("video/sync/update/"+ Constants.ROOM_ID + "start")){
                    Toast.makeText(ClientActivity.this , "MSG RECEIVED: START", Toast.LENGTH_SHORT).show();
                    mYoutubePlayerView.initialize("AIzaSyBMkd0FZt260WBoQGT2vCJk4qQFbixHzTE", mOnInitialListener); //start video
                }
                //msg to receive chat from all clients and host
                else if (topic.equalsIgnoreCase("video/sync/update/"+ Constants.ROOM_ID+"convo")){
                    Toast.makeText(ClientActivity.this , "MSG RECEIVED: NEW CHAT", Toast.LENGTH_SHORT).show();
                    tvMSG.append("\n"+message.toString());
                }
                //msg to receive movie selected by host
                else if (topic.equalsIgnoreCase("video/sync/update/" + Constants.ROOM_ID+"movie")){
                    Toast.makeText(ClientActivity.this , "MSG RECEIVED: MOV RECVD", Toast.LENGTH_SHORT).show();
                    videoS2 = message.toString();
                }



            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        //start the video playback
        mYoutubePlayerView = (YouTubePlayerView)findViewById(R.id.YTvid);

        mOnInitialListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(videoS2);

                //button to request sync update
                btnSync.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pubMSG("SYNC REQ",  Constants.ROOM_ID+ "syncreq"); //publish in a topic for host to receive

                    }
                });
                //send chat after the movie started
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //get client msg and name and publish to rooms convo topic for everyone in topic to receive
                        etMSG = (EditText)findViewById(R.id.etMSG);
                        inputMSG = Constants.KEY_EMAIL+ ": " + etMSG.getText().toString();
                        pubMSG(inputMSG, (Constants.ROOM_ID+"convo"));
                        etMSG.getText().clear();//clear input
                        etMSG.onEditorAction(EditorInfo.IME_ACTION_DONE);//dismiss keyboard

                    }
                });

                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }
                    //handles incoming msgs after movie starts
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        //messages filtered by topic

                        //topic for movies in room to play
                        if (topic.equalsIgnoreCase("video/sync/update/" + Constants.ROOM_ID+ "play")){
                            Toast.makeText(ClientActivity.this , "MSG RECEIVED: PLAY", Toast.LENGTH_SHORT).show();
                            youTubePlayer.play();
                        }
                        //topic to skip 10 seconds
                        else if (topic.equalsIgnoreCase("video/sync/update/" +  Constants.ROOM_ID+ "skip")){
                            Toast.makeText(ClientActivity.this , "MSG RECEIVED: SKIP", Toast.LENGTH_SHORT).show();

                            youTubePlayer.seekToMillis(youTubePlayer.getCurrentTimeMillis()+ 10000); //add 10 seconds to current time
                        }
                        //topic to rewind
                        else if (topic.equalsIgnoreCase("video/sync/update/" +   Constants.ROOM_ID+ "rwnd")){
                            Toast.makeText(ClientActivity.this , "MSG RECEIVED: RWND", Toast.LENGTH_SHORT).show();

                            youTubePlayer.seekToMillis(youTubePlayer.getCurrentTimeMillis()- 10000); //rewind 10 secs from current time
                        }

                        //topic to pause movie
                        else if (topic.equalsIgnoreCase("video/sync/update/" + Constants.ROOM_ID+ "pause")){
                            Toast.makeText(ClientActivity.this , "MSG RECEIVED: PAUSE", Toast.LENGTH_SHORT).show();

                            youTubePlayer.pause();
                        }
                        //sync now topic after clien pubs a sync request
                        else if (topic.equalsIgnoreCase("video/sync/update/" + Constants.ROOM_ID+ "syncnow")){
                            Toast.makeText(ClientActivity.this , "MSG RECEIVED: SYNCING", Toast.LENGTH_SHORT).show();
                            //convert the sync now message from a byte to String then to integer
                            int timeSync = Integer.parseInt(String.valueOf(message));
                            //update current time with time for sync
                            youTubePlayer.seekToMillis(timeSync+1000); //added 1 second to sync time to adress for delay in transit of messages between host and client
                        }
                        //topic to handle incoming msgs
                        else if (topic.equalsIgnoreCase("video/sync/update/" + Constants.ROOM_ID+"convo")){
                            Toast.makeText(ClientActivity.this , "MSG RECEIVED: NEW CHAT", Toast.LENGTH_SHORT).show();
                            tvMSG.append("\n"+message.toString());//append textview to show new msg
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

    }

    //publish by topic with an update message
    //used to communicate to host and other clients
    private void pubMSG(String msg, String topicid){
        String topic = "video/sync/update/"+topicid;
        String payload = msg;
        byte[] encodedPayload = new byte[0];
        try {
            client.publish(topic, payload.getBytes(), 0, false);
            Toast.makeText(this , "MSG Sent", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    //subscribe to all topics in "video/sync/update/" to enable communcation between all clients and host for chat and video sync
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
