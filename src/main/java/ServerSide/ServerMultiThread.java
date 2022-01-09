package ServerSide;


import models.ClientInfos;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ServerMultiThread {
    public static List<ClientInfos> connectedClients;
    private static InetAddress localAddress;
    private static ServerSocket mySkServer;
    public static Log log = new Log();
    public static void main(String[] args){

        localAddress = null;
        String interfaceName = "wlan2";
        //create an empty list for connected clients
        connectedClients = new ArrayList<ClientInfos>();


        //intitiale value for the client number. increments each time someone connects
        int ClientNo = 1;

        try {

            //get ip address of the local machine
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
            mySkServer = new ServerSocket(45000,10,localAddress);
            System.out.println("Used IpAddress :" + mySkServer.getInetAddress());
            System.out.println("Listening to Port :" + mySkServer.getLocalPort());
            log.info("Server online = port :"+mySkServer.getLocalPort()+" address :" + mySkServer.getInetAddress());
            //loops forever to wait new clients
            while(true)
            {
                System.out.println("waiting for new client");
                //wait for a client connection
                Socket clientSocket = mySkServer.accept();

                //create a new thread for the client that just connected
                Thread t = new Thread(new AcceptClient(clientSocket,ClientNo));

                //increase the client ID
                ClientNo++;
                //starting the thread
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
            log.warning("EXCEPTION : "+e );
        }
    }
}