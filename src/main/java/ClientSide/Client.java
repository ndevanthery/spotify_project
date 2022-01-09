package ClientSide;

import ServerSide.Log;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

public class Client {


    //two lines to use the colors in the command line
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    private static Log log = new Log();


    //list of accepted file extensions
    //useful if you want to implement more file extensions with audio players that supports it
    private static String[] autorizedExtensions = {"wav"};



    //this is the socket used to use the peer to peer. other clients will connect to it
    private static ServerSocket mySkServer;

    //used to send informations on the socket
    private static PrintWriter pout;

    //used to receive informations from the socket
    private static BufferedReader buffin;

    //used to store the spotify server adress
    private static InetAddress serverAddress;

    private static Socket mySocket;


    public static void main(String[] args) {

        //initialisation of the socket used for peer to peer
        serverSocketInit();

        //initialisation of the scanner used for user input
        Scanner sc = new Scanner(System.in);


        mySocket = null;


        try {

            boolean adressOk=false;
            //while the adress is wrong, continue asking before continuing
            while (!adressOk)
            {
                //try catch in case the address is wrong
                try{
                    //server adress used. asked to the client each time he wants to connect
                    System.out.println("what is the spotify server adress?");
                    String serverName = sc.nextLine();
                    //convert the string address to a usable by socket address
                    serverAddress = InetAddress.getByName(serverName);

                    //try to connect to the server
                    mySocket = new Socket(serverAddress,45000);

                    //if no exception was sent, connexion is good, end of loop
                    adressOk =true;
                }catch (IOException e)
                {
                    System.out.println("the address you gave is incorrect. please try again");
                }

            }



            System.out.println("We got the connexion to  "+ serverAddress);

            //open the input and output stream to communicate with spotify server
            pout = new PrintWriter(mySocket.getOutputStream());
            buffin = new BufferedReader (new InputStreamReader(mySocket.getInputStream()));



            //get username
            System.out.println("what is your username? ");
            String username = sc.nextLine();
            pout.println(username);
            pout.flush();


            /*=================== GET MUSIC LIST AND SEND IT TO SERVER ============================*/
            List<String> myList = getMusicList(System.getProperty("user.home")+"/Music",0);
            //write the message on the output stream
            for(int i=0;i<myList.size();i++)
            {
                pout.println(myList.get(i));
                pout.flush();
            }
            //endlist is to know when there is no more music urls to read for the server
            pout.println("endList");
            pout.flush();


            /*==================== COMMANDS PART ============================*/

            //is used to end the connection if you type exit
            boolean isConnected = true;

            //loop while exit isn't written by the user
            while(isConnected)
            {

                //get user command
                System.out.println("Spotify Command :");
                String message = sc.nextLine();

                //send it to the server
                pout.println(message);
                pout.flush();


                //server answer
                System.out.println("waiting for server answer");
                System.out.print(ANSI_GREEN);//to get a distinct view of what is from the server and what isn't


                String line = buffin.readLine();

                //STREAM key word is sent in first place by the server to make the client know what is following(IP and URL)
                boolean isStreamInfos = false;
                int streamLine = 0;

                //to store URL AND IP for stream functionality
                String streamURL;
                String streamIP="";

                //while there is more infos waiting on the buffer
                while(buffin.ready())
                {
                    //if the first word sent is STREAM then get url and IP
                    if(line.equals("STREAM"))
                    {
                        isStreamInfos = true;
                        streamLine=0;
                    }
                    //used if the first word is STREAM only
                    if(isStreamInfos)
                    {
                        //get stream IP
                        if(streamLine==1)
                        {
                            streamIP = line;
                        }
                        //since stream url is the last line, it is assigned out of the loop
                        streamLine++;
                    }
                    else
                    {
                        System.out.println(line);
                    }


                    //at the end to make the while condition work
                    //takes the nextline sent by the server
                    line = buffin.readLine();


                }

                //if this isn't a stream, write what was send in the command line for the client to read it
                if(!isStreamInfos)
                {
                    System.out.println(line +ANSI_RESET);

                }
                //if this is a stream, the ip and url aren't printed for the user. it is direclty used to connect peer to peer to the client
                else
                {
                    streamURL = line;

                    //connect to the other client and stream his file

                    streamMusic(streamIP , streamURL);


                }

                //if the command sent is exit : end the while to terminate client connection to the server
                if(message.equals("exit"))
                {
                    isConnected=false;
                }
            }




        }
        catch (IOException e) {
            //in case there is an error with the server
            System.out.println("server connection error, dying.....");
            log.severe("EXCEPTION :server connection error"+ e );
        }catch(NullPointerException e){
            e.printStackTrace();
            System.out.println("Connection interrupted with the server");
            log.severe("EXCEPTION :Connection interrupted with the server"+ e );
        }

        //end of the client connection to the spotify server
        System.out.println("\nTerminate client program...");
        try {
            mySocket.close();
        } catch (IOException e) {
                log.severe("EXCEPTION :"+e);
        }
    }

    //recursive method to get all songs in the music repository and his childs
    public static List<String> getMusicList(String path , int indent)
    {
        //create an empty list to store urls
        List<String> listOfMusic = new ArrayList<String>();

        //create a file for the folder a the path
        File folder = new File(path);

        //list files present in the folder
        File[] listOfFiles = folder.listFiles();

        //parse all the files present in the folder
        for (int i = 0; i < listOfFiles.length; i++) {

            //if the file is a file
            if (listOfFiles[i].isFile()) {

                //get what is the extension of the file
                int lastPointIndex = listOfFiles[i].getName().lastIndexOf('.');
                String extension = listOfFiles[i].getName().substring(lastPointIndex+1);


                //test if the extension is present in the list of possible extensions playable by the audio player
                if(possibleExtension(extension))
                {

                    //add in a list the path
                    listOfMusic.add(listOfFiles[i].getAbsolutePath());




                }
                //if the file is a directory
            } else if (listOfFiles[i].isDirectory()) {

                //call recursive method on this directory to get all his files
                List<String> recursiveList = getMusicList(path+"/"+listOfFiles[i].getName() , indent+1);
                //add all the files in the main list
                listOfMusic.addAll(recursiveList);
            }
        }

        return listOfMusic;
    }

    //check if the extension is present in autorized extensions
    public static boolean possibleExtension(String extension)
    {
        for(int i=0;i<autorizedExtensions.length;i++)
        {
            if(autorizedExtensions[i].equalsIgnoreCase(extension))
            {
                //if this is present
                return true;
            }
        }
        //if no correspondances is found
        return false;
    }


    //server socket initialisation
    private static void serverSocketInit()
    {
        InetAddress localAddress = null;
        String interfaceName = "wlan2";

        try {

            //get the address of the client
            NetworkInterface ni = NetworkInterface.getByName(interfaceName);
            Enumeration<InetAddress> inetAddresses =  ni.getInetAddresses();
            while(inetAddresses.hasMoreElements()) {
                InetAddress ia = inetAddresses.nextElement();

                if(!ia.isLinkLocalAddress()) {
                    if(!ia.isLoopbackAddress()) {
                        System.out.println(ni.getName() + "->IP: " + ia.getHostAddress());
                        localAddress = ia;
                        break;
                    }
                }
            }

            //Warning : the backlog value (2nd parameter is handled by the implementation
            mySkServer = new ServerSocket(46000,10,localAddress);

            //thread for handling peer to peer connections
            Thread t = new Thread(new OtherClientConnection(mySkServer));
            t.start();


        } catch (IOException e) {

            e.printStackTrace();
            log.severe("EXCEPTION : "+e);
        }

    }

    public static void streamMusic(String streamIP , String streamURL) {
        InetAddress serverAddress;

        try {
            //string ip to usable address
            serverAddress = InetAddress.getByName(streamIP);

            //try to connect to the server
            Socket mySocket = new Socket(serverAddress, 46000);


            //open read/write with the other client to communicate with him
            PrintWriter pout = new PrintWriter(mySocket.getOutputStream());
            BufferedReader buffin = new BufferedReader (new InputStreamReader(mySocket.getInputStream()));

            System.out.println(ANSI_RESET+ "CONNECTED TO " + serverAddress);



            //ask the file to the other client
            pout.println(streamURL);
            pout.flush();


            //receive the file
            int totalsize = Integer.parseInt(buffin.readLine());

            //get the audio file in an inputStream
            InputStream is = new BufferedInputStream(mySocket.getInputStream(),totalsize);
            try {
                //create the audio player to play the audio file
                SimpleAudioPlayer myPlayer = new SimpleAudioPlayer(is);

                //play the audio
                myPlayer.play();

                //since the file is received,we can close the socket
                mySocket.close();
                String status="";

                //open a scanner to read user command for the audio player
                Scanner sc = new Scanner(System.in);

                //while the user doesn't want to end the audio play, loops
                while (!status.equals("exit"))
                {

                    //read user command
                    System.out.println("play/pause/exit");
                    status = sc.nextLine();

                    //switch for each commands
                    switch (status)
                    {
                        case "exit":
                        case "pause": myPlayer.pause();
                            break;
                        case "play" : myPlayer.play();
                            break;
                    }
                }




            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
                log.severe("EXCEPTION : "+e);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                log.severe("EXCEPTION : "+e);
            }

        }
        catch (IOException e)
        {
            System.out.println("PROBLEM WITH CONNECTION TO STREAM IP");
            log.severe("EXCEPTION : "+e);
        }
    }



}
