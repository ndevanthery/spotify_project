package ClientSide;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

public class Client2 {

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    private static String[] autorizedExtensions = {"mp3" , "wav"};
    private static ServerSocket mySkServer;


    public static void main(String[] args) {

        serverSocketInit();






        PrintWriter pout;
        BufferedReader buffin;
        InetAddress serverAddress;
        String serverName = "127.0.0.1"; // TO CHANGE IF NOT LOCAL HOST

        try {
            serverAddress = InetAddress.getByName(serverName);
            System.out.println("Get the address of the server : "+ serverAddress);

            //try to connect to the server
            Socket mySocket = new Socket(serverAddress,45000);

            System.out.println("We got the connexion to  "+ serverAddress);

            /*======================= DEVELOP HERE ============================*/



            /*=================== GET MUSICS AND SEND TO SERVER ============================*/

            List<String> myList = getMusicList("D:\\music",0);


            //open the output data stream to write on the client
            pout = new PrintWriter(mySocket.getOutputStream());
            buffin = new BufferedReader (new InputStreamReader(mySocket.getInputStream()));












                //write the message on the output stream
            for(int i=0;i<myList.size();i++)
            {
                pout.println(myList.get(i));
                pout.flush();
            }
            pout.println("endList");
            pout.flush();


            /*==================== COMMANDS PART ============================*/
            boolean isConnected = true;
            Scanner sc = new Scanner(System.in);

            String message_distant ="";

            while(isConnected)
            {

                //user input
                System.out.println("Spotify Command :");
                String message = sc.nextLine();

                pout.println(message);
                pout.flush();

                //server answer
                System.out.println("waiting for server answer");
                System.out.print(ANSI_GREEN);
                String line = buffin.readLine();
                boolean isStreamInfos = false;
                int streamLine = 0;
                String streamURL = "";
                String streamIP="";
                while(buffin.ready())
                {

                    if(line.equals("STREAM"))
                    {
                        isStreamInfos = true;
                        streamLine=0;
                    }
                    if(isStreamInfos)
                    {
                        if(streamLine==1)
                        {
                            streamIP = line;
                        }
                        if(streamLine==2)
                        {
                            streamURL = line;
                        }
                        streamLine++;
                    }
                    else
                    {
                        System.out.println(line);
                    }



                    line = buffin.readLine();


                }

                if(!isStreamInfos)
                {
                    System.out.println(line +ANSI_RESET);

                }
                else
                {
                    System.out.println("STREAM INFOS");
                    streamURL = line;

                    //connect to the other client and stream his file

                    System.out.println(streamIP);
                    System.out.println(streamURL);



                    streamMusic(streamIP , streamURL);


                }





                if(message.equals("exit"))
                {
                    isConnected=false;
                }
            }











            /*====================== END OF DEVELOPMENT =======================*/




            System.out.println("\nTerminate client program...");
            mySocket.close();


        }catch (UnknownHostException e) {

            e.printStackTrace();
        }catch (IOException e) {
            System.out.println("server connection error, dying.....");
        }catch(NullPointerException e){
            System.out.println("Connection interrupted with the server");
        }
    }

    public static List<String> getMusicList(String path , int indent)
    {
        List<String> listOfMusic = new ArrayList<String>();
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        String indentation = "";
        for(int i=0;i<indent;i++)
        {
            indentation+="   ";
        }

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                int lastPointIndex = listOfFiles[i].getName().lastIndexOf('.');
                String extension = listOfFiles[i].getName().substring(lastPointIndex+1);

                if(possibleExtension(extension))
                {
                    //System.out.println(indentation + "File : " + listOfFiles[i].getName());

                    //add in a list the path
                    listOfMusic.add(listOfFiles[i].getAbsolutePath());




                }
            } else if (listOfFiles[i].isDirectory()) {

                //System.out.println(indentation + listOfFiles[i].getName());
                List<String> recursiveList = getMusicList(path+"/"+listOfFiles[i].getName() , indent+1);
                listOfMusic.addAll(recursiveList);
            }
        }

        return listOfMusic;
    }


    public static boolean possibleExtension(String extension)
    {
        for(int i=0;i<autorizedExtensions.length;i++)
        {
            if(autorizedExtensions[i].equalsIgnoreCase(extension))
            {
                return true;
            }
        }
        return false;
    }

    private static void serverSocketInit()
    {
        //SERVERSOCKET INITIALISATION
        Socket srvSocket = null ;
        InetAddress localAddress = null;
        String interfaceName = "lo";

        int ClientNo = 1;

        try {
            NetworkInterface ni = NetworkInterface.getByName(interfaceName);
            Enumeration<InetAddress> inetAddresses =  ni.getInetAddresses();
            while(inetAddresses.hasMoreElements()) {
                InetAddress ia = inetAddresses.nextElement();

                if(!ia.isLinkLocalAddress()) {
                    if(!ia.isLoopbackAddress()) {
                        System.out.println(ni.getName() + "->IP: " + ia.getHostAddress());
                        localAddress = ia;
                    }
                }
            }

            //Warning : the backlog value (2nd parameter is handled by the implementation
            mySkServer = new ServerSocket(46000,10,localAddress);
            System.out.println("Default Timeout :" + mySkServer.getSoTimeout());
            System.out.println("Used IpAddress :" + mySkServer.getInetAddress());
            System.out.println("Listening to Port :" + mySkServer.getLocalPort());

            //wait for a client connection
            Thread t = new Thread(new OtherClientConnection(mySkServer));
            t.start();


        } catch (IOException e) {

            e.printStackTrace();
        }

        //END OF SERVERSOCKET INIT
    }

    public static void streamMusic(String streamIP , String streamURL) {
        InetAddress serverAddress;

        try {
            serverAddress = InetAddress.getByName(streamIP);
            System.out.println(ANSI_RESET+ "Get the address of the server : " + serverAddress);

            //try to connect to the server
            Socket mySocket = new Socket(serverAddress, 46000);

            PrintWriter pout = new PrintWriter(mySocket.getOutputStream());
            BufferedReader buffin = new BufferedReader (new InputStreamReader(mySocket.getInputStream()));

            System.out.println("We got the connexion to  " + serverAddress);



            //ask the file to the other client
            pout.println(streamURL);
            pout.flush();


            //receive the file



            // STREAM AUDIO
            System.out.println("streaming the audio : " + streamURL);





            mySocket.close();
        }
        catch (IOException e)
        {
            System.out.println("PROBLEM WITH CONNECTION TO STREAM IP");
        }
    }



}
