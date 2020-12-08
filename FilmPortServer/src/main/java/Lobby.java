import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Lobby  implements MqttCallback {
    final IMqttClient client;
    int totalCount = 2;
    int count = 0;
    String[] uName = new String[10];
    int[] uScore = new int[10];
    String roomID = "";
    int winner = 0;
    Random rand = new Random();


    public static void main(String[] args) throws Exception {
        String broker = "tcp://broker.hivemq.com:1883"; //broker
        Lobby lobby = new Lobby(broker);
    }


    public Lobby(String broker) throws Exception {
        //start a new conenction
        final String publisherId = UUID.randomUUID().toString();
        client = new MqttClient(broker, publisherId);
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        client.connect(options);
        client.setCallback(this);//set call back to this

        //sub to topic to receive room updates
        client.subscribe("filmport/trivia/#");

    }

    //Thread to award winner in each room
   class LobbyThread extends Thread implements MqttCallback{

        int counter;
        int totalC;
        String[] Name = new String[10];
        int [] Score = new int[10];
       Random random = new Random();
        IMqttClient tClient;
        String room = "";
        String broker = "tcp://broker.hivemq.com:1883";
        public LobbyThread(IMqttClient client, int total, int count, String [] names, int [] scores, String roomid){
            this.counter = count;
            this.totalC = total;
            this.Name = names;
            this.Score = scores;
            //this.tClient = client;
            this.room = roomid;



        }

        public void run(){

            try {
                //create a new MQTT conenction to communicate winner information to application
                //need two conenctions to stay independant from server listening for more rooms
                final String publisherId = UUID.randomUUID().toString();
                tClient = new MqttClient(broker, publisherId);
                final MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(true);
                options.setConnectionTimeout(10);
                tClient.connect(options);
                tClient.setCallback(this);

                //sub to receive people joining infro
                tClient.subscribe("filmport/trivia/#");

            }
            catch(MqttException e){

                e.printStackTrace();
            }

            System.out.println("Thread started for " + room); //output to console
            tClient.setCallback(this); //callback for connection in thread


        }
        //method to check for winner
       public void checkWinner() throws MqttException {

            //set a temp variable hiscore to first score in the score array
           int hiScore = Score[0];
           //loop to find the highest value ins core array
           for (int i = 0; i < totalC; i++){

               if (Score[i] >  hiScore){
                   hiScore = Score[i];
               }
           }
        //at the end of the loop, the highest number in the array will be saved in hiScore


           //make a list to save all winners
           List<String> winners = new ArrayList<>();
            //if the users score is equal to the highscore, add their name to to list off winners
           for (int j = 0; j<totalC; j++){
               if (Score[j] == hiScore){
                   winners.add(Name[j]);

               }
           }

           //generate a random number from 0 to winner array size
           int rando = rand.nextInt(winners.size());

           //the final winner will be set randomly if there is more than 1 winner
           String finalWinner = winners.get(rando);

           //loop to publish back to the application
           for (int k = 0; k < totalC; k++){

               //if the name of person k is the final winner
               // they receive a host msg
               if (Name[k].equalsIgnoreCase(finalWinner)){
                   MqttMessage message = new MqttMessage();
                   message.setPayload("host".getBytes()); //convert to byte
                   message.setQos(1);//set level
                   client.publish("filmport/trivia/"+ room+ "/" + Name[k], message);
                   System.out.println("Host Message Sent to " + Name[k] + " in " + room); //print to console
               }
               //if not, they receive a client message
               else {
                   MqttMessage message = new MqttMessage();
                   message.setPayload("client".getBytes()); //convert to byte
                   message.setQos(1);//set level
                   client.publish("filmport/trivia/"+ room + "/" + Name[k], message);
                   System.out.println("Client Message Sent to " + Name[k]+ " in " + room); //print to console
               }
           }
           resetServer();









       }

       public  void resetServer(){
           this.counter = 0;
           this.totalC = 2;

       }
       @Override
       public void connectionLost(Throwable throwable) {

       }
//messages will be handled here for the Lobby Thread
       @Override
       public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

            //when host and client finish quiz, this willr receive their score and name
           //in a topic that will be flexible based on their room
           if (s.equalsIgnoreCase("filmport/trivia/whowon/" + room)) {

               //print to console
               System.out.println("Current Count: " + counter);
               //split the message into name and score
               String[] iMSG = (mqttMessage.toString()).split("&", 2);
               //save in variables
               uName[counter] = iMSG[0];
               uScore[counter] = Integer.parseInt(iMSG[1]);
               //if the amount of clients and host joined is equal to total allowed
               if ((counter+1) == totalC) {
                   checkWinner();//call the check winner method
               }
               else {
                   counter++;//if not increase counter
               }

           }
       }

       @Override
       public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

       }
   }







    @Override
    public void connectionLost(Throwable throwable) {

    }

    //topic to receive the room ID to start the thread
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

        if (s.equalsIgnoreCase("filmport/trivia/lobby")) {
//split msg
            String[] iMSG = (mqttMessage.toString()).split("&", 2);
            //save in correct variables
            this.totalCount = Integer.parseInt(iMSG[0]);
            roomID = iMSG[1];

        //print to console
            System.out.println("Total Count Set To: " + totalCount);
            System.out.println("Room: " + roomID);
            //start new thread
            new LobbyThread(client, totalCount, count, uName, uScore, roomID).start();


        }

    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
