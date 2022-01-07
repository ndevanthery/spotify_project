package ServerSide;

import models.ClientInfos;
import models.MusicModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AcceptClient implements Runnable {


    private PrintWriter pout;
    private BufferedReader buffin;
    private Socket clientSocketOnServer;
    private int clientNumber;
    private String username;
    public Log log = new Log();
    //Constructor
    public AcceptClient (Socket clientSocketOnServer, int clientNo)
    {
        this.clientSocketOnServer = clientSocketOnServer;
        this.clientNumber = clientNo;

    }
    //overwrite the thread run()
    public void run() {

        try {
            System.out.println("Client Nr "+clientNumber+ " is connected");


            //opens read and write for the server to communicate with the client
            buffin = new BufferedReader (new InputStreamReader(clientSocketOnServer.getInputStream()));
            pout = new PrintWriter(clientSocketOnServer.getOutputStream());


            //recup username
            username = buffin.readLine();
            System.out.println(username);

            //create empty list to store musics url of the client
            List<MusicModel> listofSongs = new ArrayList<>();
            String message_distant="";

            //read the urls the user sent and store them in the list, while the endlist keyword isn't sent
            System.out.printf("%40s %20s %20s\n","title" , "album","artiste");

            while(!message_distant.equals("endList"))
            {
                //read the url
                message_distant = buffin.readLine();

                //if this isn't the end of the list
                if(!message_distant.equals("endList"))
                {
                    //split the full url to find the music name, possible album and artist
                    //album and artists are accurate only if the architecture matches
                    // artist/album/musicname.wav
                    // or artist/musicname.wav
                    String[] relativeURL= message_distant.split("Music\\\\")[1].split("\\\\");
                    //in case the hierarchy doesn't match, unknown
                    String artiste = "unknown";
                    String album = "unknowm";
                    String musicName ;
                    if(relativeURL.length==3)//hierarchy artist/album/music.wav
                    {
                        artiste = relativeURL[0];
                        album = relativeURL[1];

                    }
                    if(relativeURL.length==2) {//hierarchy artist/music.wav
                        artiste = relativeURL[0];
                    }

                    //get music name
                    int lastPointIndex = relativeURL[relativeURL.length-1].lastIndexOf('.');
                    musicName = relativeURL[relativeURL.length-1].substring(0,lastPointIndex);

                    //create the music model
                    MusicModel myMusic = new MusicModel(message_distant , musicName,artiste,album);

                    //add the music to the list
                    listofSongs.add(myMusic);
                    System.out.printf("%40s %20s %20s\n",myMusic.getMusicName(),myMusic.getAlbum(),myMusic.getArtiste());
                }


            }


            //create a new client infos with all the infos of the client that just connected
            ClientInfos newClient = new ClientInfos(clientNumber ,username, clientSocketOnServer.getInetAddress().getHostAddress(),listofSongs);

            //add it to the clients list
            ServerMultiThread.connectedClients.add(newClient);
            log.info("User : "+username+" is connected.");


            boolean isRunning =true;
            //while the client doesn't send exit
            while (isRunning)
            {
                //wait for a client command
                message_distant = buffin.readLine();

                //split the command to take only the first word
                String keyWord = message_distant.split(" ")[0];

                //switch between commands
                switch (keyWord)
                {
                    case "exit":
                        exit();
                        //end the loop
                        isRunning=false;
                        log.info("User : "+username+" disconnected");
                        break;
                    case "listUsers":
                        listUsers();
                        log.info("User : "+username+" listed users");
                        break;
                    case "help":
                        help();
                        log.info("User : "+username+" asked for help");
                        break;

                    case "listMusics":
                        try {
                            //get the username in argument
                           String username_request =message_distant.split(" ")[1];
                            listMusics(username_request);
                            log.info("User : "+username+" listed musics from "+username_request);
                        }
                        catch (Exception e)
                        {
                            //if the user doesn't write a username
                            pout.println("you must enter a username. write listUsers to see those who are connected");
                            pout.flush();
                            log.severe("User : "+username+" entered a invalid username argument");
                        }
                        break;
                case "stream":
                    try {
                        //get the username in arguments
                        String username_request = message_distant.split(" ")[1];
                        //get the music id in arguments
                        int idMusic = Integer.parseInt(message_distant.split(" ")[2]);
                        stream(username_request,idMusic);
                        log.info("User : "+ username+" started to stream music id : "+idMusic+" from : "+username_request);
                    }
                    catch (Exception e)
                    {
                        //if there is no argument or not the 2 required
                        pout.println("the stream inputs are not valids");
                        pout.flush();
                        log.severe("EXCEPTION : "+e);
                    }
                    break;
                    default:
                        //if the command is not in the usable list
                        pout.println("command not recognized. type help if you need more infos");
                        pout.flush();
                }
            }

            //close the connection when the loop ends
            clientSocketOnServer.close();
            System.out.println("end of connection to the client " + clientNumber);

            //remove the client from the list of connected ones
            for(int i=0;i<ServerMultiThread.connectedClients.size();i++)
            {
                if(ServerMultiThread.connectedClients.get(i).getId()==clientNumber)
                {
                    ServerMultiThread.connectedClients.remove(i);
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.severe("EXCEPTION : "+e);

        }
    }

    public void listUsers()
    {

        pout.println(String.format("%20s %20s","username", "ip"));

        System.out.println("username\tip");

        //send the list of all connected users
        for(int i=0;i<ServerMultiThread.connectedClients.size();i++)
        {
            ClientInfos myClient =ServerMultiThread.connectedClients.get(i);
            //send a client to the client who asked the list
            pout.println(String.format("%20s %20s",myClient.getUsername() , myClient.getIp()));


        }
        pout.flush();
    }

    public void exit()
    {
        pout.println("GOODBYE");
        pout.flush();

    }

    public void help()
    {
        pout.println("listUsers : gives a list of all connected users");
        pout.println("listMusics [username] : gives you the list of the songs of a user");
        pout.println("exit : disconnects you from the server");
        pout.println("stream [username] [musicID] : streams a music from a specific user");
        pout.flush();

    }

    public void listMusics(String username)
    {

        //get my client
        ClientInfos myClient = getClient(username);

        //if there is no correspondances
        if(myClient==null)
        {
            pout.println("you must enter a valid username. write listUsers to see those who are connected");
            pout.flush();
        }
        else
        {
            //get the list of musics of the client
            List<MusicModel> myList = myClient.getListOfSongs();
            pout.println(String.format("%3s %50s %20s %20s","ID" , "TITLE","ALBUM","ARTIST"));
            int idMusic = 0;
            //send the music list to the client with an id for each to make it more usable
            for(int i=0;i<myList.size();i++)
            {
                MusicModel myMusic = myList.get(i);


                pout.println(String.format(" %3s %50s %20s %20s",idMusic ,myMusic.getMusicName(),myMusic.getAlbum(),myMusic.getArtiste()));
                idMusic++;
            }
            pout.flush();
        }
    }
    public ClientInfos getClient(String username)
    {
        ClientInfos myClient = null;

        //parse the list of clients
        for(int i=0;i<ServerMultiThread.connectedClients.size();i++)
        {
            if(ServerMultiThread.connectedClients.get(i).getUsername().equals(username))
            {
                //if there is a correspondance , get the client and returns it
                myClient = ServerMultiThread.connectedClients.get(i);
                break;
            }
        }
        return myClient;
    }

    public void stream(String username , int idMusic)
    {
        //get my client
        ClientInfos myClient = getClient(username);

        if(myClient==null)
        {
            pout.println("the username you entered corresponds to no one");
            pout.flush();
        }
        else
        {
            try
            {

                //get the asked music
                MusicModel myMusic = myClient.getListOfSongs().get(idMusic);
                //send ip and URL of the song with the keyword STREAM for the client to know what's coming next
                pout.println("STREAM");
                pout.println(myClient.getIp());
                pout.println(myMusic.getUrl());
                pout.flush();
            }
            catch (Exception e)
            {
                pout.println("this music id doesn't exist for this user");
                pout.flush();
                log.severe("EXCEPTION : "+e);
            }


        }
    }

}