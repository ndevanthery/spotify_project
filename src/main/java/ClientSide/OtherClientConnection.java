package ClientSide;

import ServerSide.AcceptClient;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OtherClientConnection implements Runnable{
    private ServerSocket mySkServer;

    public OtherClientConnection (ServerSocket mySkServer) {
        this.mySkServer = mySkServer;
    }

    @Override
    public void run() {
        try
        {
            while (true)
            {
                System.out.println("WAITING FOR A CLIENT CONNEXION");

                //wait until a client wants to connect
                Socket clientSocket = mySkServer.accept();


                //open read/write on the socket to communicate with the other client
                PrintWriter pout = new PrintWriter(clientSocket.getOutputStream(),true);
                BufferedReader buffin = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));

                //read the requested url sent by the other client
                String url = buffin.readLine();
                System.out.println("FILE REQUESTED : "+url);

                //get the file
                File myFile = new File(url);
                //get the length of the file
                long myFileSize = Files.size(Paths.get(url));

                //send the file size
                pout.println(myFileSize);

                //send the binary file to the client
                byte[] mybytearray = new byte[(int)myFileSize];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                bis.read(mybytearray, 0, mybytearray.length);
                OutputStream os = clientSocket.getOutputStream();
                os.write(mybytearray, 0, mybytearray.length);
                os.flush();



            }



        }
        catch (IOException e) {

        e.printStackTrace();
    }




}
}
