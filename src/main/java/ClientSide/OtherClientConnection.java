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
                Socket clientSocket = mySkServer.accept();
                PrintWriter pout = new PrintWriter(clientSocket.getOutputStream());
                BufferedReader buffin = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));

                System.out.println("WAITING for request");
                String url = buffin.readLine();
                System.out.println("RECEIVED REQUEST");
                System.out.println(url);

                File myFile = new File(url);
                long myFileSize = Files.size(Paths.get(url));

                PrintWriter Pout2 = new PrintWriter(clientSocket.getOutputStream(), true);
                Pout2.println(myFileSize);
                Pout2.println(url);

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
