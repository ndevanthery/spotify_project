package ServerSide;

import models.MusicModel;

import javax.swing.text.MutableAttributeSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AcceptClient implements Runnable {


    private PrintWriter pout;
    private BufferedReader buffin;
    private Socket clientSocketOnServer;
    private int clientNumber;
    private boolean isRunning=true;

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
            System.out.println("Socket is available for connection"+ clientSocketOnServer);

            System.out.println();

            /*======================= DEVELOP HERE ============================*/


            //create an input stream to read data from the server
            buffin = new BufferedReader (new InputStreamReader(clientSocketOnServer.getInputStream()));
            List<MusicModel> listofSongs = new ArrayList<MusicModel>();

            pout = new PrintWriter(clientSocketOnServer.getOutputStream());


            //recup list of musics and create a clientInfo
            String message_distant="";
            System.out.printf("%20s %20s %20s\n","title" , "album","artiste");

            while(!message_distant.equals("endList"))
            {
                message_distant = buffin.readLine();

                if(!message_distant.equals("endList"))
                {
                    String[] relativeURL= message_distant.split("Music\\\\")[1].split("\\\\");
                    String artiste = "unknown";
                    String album = "unknowm";
                    String musicName = "unknown";
                    if(relativeURL.length==3)
                    {
                        artiste = relativeURL[0];
                        album = relativeURL[1];
                        int lastPointIndex = relativeURL[relativeURL.length-1].lastIndexOf('.');
                        musicName = relativeURL[relativeURL.length-1].substring(0,lastPointIndex);
                    }
                    if(relativeURL.length==2)
                    {
                        artiste = relativeURL[0];
                        int lastPointIndex = relativeURL[relativeURL.length-1].lastIndexOf('.');
                        musicName = relativeURL[relativeURL.length-1].substring(0,lastPointIndex);                    }
                    else
                    {
                        int lastPointIndex = relativeURL[relativeURL.length-1].lastIndexOf('.');
                        musicName = relativeURL[relativeURL.length-1].substring(0,lastPointIndex);
                    }
                    MusicModel myMusic = new MusicModel(message_distant , musicName,artiste,album);
                    listofSongs.add(myMusic);
                    System.out.printf("%20s %20s %20s\n",myMusic.getMusicName(),myMusic.getAlbum(),myMusic.getArtiste());
                }


            }


            ClientInfos newClient = new ClientInfos(clientNumber , clientSocketOnServer.getInetAddress().getHostAddress(),clientSocketOnServer.getPort(),listofSongs);
            ServerMultiThread.connectedClients.add(newClient);
            System.out.println(newClient);
            boolean isRunning =true;
            while (isRunning)
            {
                message_distant = buffin.readLine();
                String keyWord = message_distant.split(" ")[0];
                switch (keyWord)
                {
                    case "exit":
                        exit();
                        isRunning=false;
                        break;
                    case "listUsers":
                        listUsers();
                        break;
                    case "help":
                        help();
                        break;

                    case "listMusics":
                        try {
                           int id = Integer.parseInt(message_distant.split(" ")[1]);
                            listMusics(id);
                        }
                        catch (Exception e)
                        {
                            pout.println("you must enter a valid id of the person you want to see songs");
                            pout.flush();
                        }
                        break;
                case "stream":
                    try {
                        int idUser = Integer.parseInt(message_distant.split(" ")[1]);
                        int idMusic = Integer.parseInt(message_distant.split(" ")[2]);
                        stream(idUser,idMusic);
                    }
                    catch (Exception e)
                    {
                        pout.println("the stream inputs are not valids");
                        pout.flush();
                    }
                    break;
                    default:
                        pout.println("command not recognized. type help if you need more infos");
                        pout.flush();


                }
            }




            clientSocketOnServer.close();
            System.out.println("end of connection to the client " + clientNumber);
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
        }
    }

    public void listUsers()
    {

        pout.println("ID\tIP");

        System.out.println("id\tip");
        System.out.println(ServerMultiThread.connectedClients.size());

        for(int i=0;i<ServerMultiThread.connectedClients.size();i++)
        {
            ClientInfos myClient =ServerMultiThread.connectedClients.get(i);
            pout.println(myClient.getId() + "\t" + myClient.getIp());
            System.out.println(myClient.getId() + "\t" + myClient.getIp());


        }
        pout.flush();
    }

    public void exit()
    {
        System.out.println("welcome to the exit function");
        pout.println("welcome to the exit function");
        pout.flush();

    }

    public void help()
    {
        System.out.println("welcome to the help function");
        pout.println("welcome to the help function");

        pout.println("listUsers : gives a list of all connected users");
        pout.println("listMusics [idUser] : gives you the list of the songs of a user");
        pout.println("exit : disconnects you from the server");
        pout.println("stream [idUser] [musicID] : streams a music from a specific user");
        pout.flush();

    }

    public void listMusics(int userID)
    {
        ClientInfos myClient = null;
        for(int i=0;i<ServerMultiThread.connectedClients.size();i++)
        {
            if(ServerMultiThread.connectedClients.get(i).getId()==userID)
            {
                myClient = ServerMultiThread.connectedClients.get(i);
                break;
            }
        }
        if(myClient==null)
        {
            pout.println("the id you entered corresponds to no one");
            pout.flush();
        }
        else
        {
            List<MusicModel> myList = myClient.getListOfSongs();
            pout.println(String.format("%5s %20s %20s %20s","ID" , "TITLE","ALBUM","ARTIST"));
            int idMusic = 0;
            for(int i=0;i<myList.size();i++)
            {
                MusicModel myMusic = myList.get(i);


                pout.println(String.format(" %5s %20s %20s %20s",idMusic ,myMusic.getMusicName(),myMusic.getAlbum(),myMusic.getArtiste()));
                idMusic++;
            }
            pout.flush();
        }



    }

    public void stream(int idUser , int idMusic)
    {
        ClientInfos myClient = null;
        for(int i=0;i<ServerMultiThread.connectedClients.size();i++)
        {
            if(ServerMultiThread.connectedClients.get(i).getId()==idUser)
            {
                myClient = ServerMultiThread.connectedClients.get(i);
                break;
            }
        }
        if(myClient==null)
        {
            pout.println("the id you entered corresponds to no one");
            pout.flush();
        }
        else
        {
            MusicModel myMusic = myClient.getListOfSongs().get(idMusic);
            //send ip and URL of the song
            pout.println("STREAM");
            pout.println(myClient.getIp());
            pout.println(myMusic.getUrl());
            pout.flush();
        }
    }

}