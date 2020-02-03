import org.w3c.dom.ls.LSOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class EmailExtractorClient {

    public static void main(String[] args) throws IOException {

        String hostName = "127.0.0.1"; // Default host, localhost
        int portNumber = 5555; // Default port to use
        if (args.length > 0) {
            hostName = args[0];
            if (args.length > 1) {
                portNumber = Integer.parseInt(args[1]);
                if (args.length > 2) {
                    System.err.println("Usage: java EmailExtractorClientTCP [<host name>] [<port number>]");
                    System.exit(1);
                }
            }
        }
        System.out.println("Email Extractor, provide url with http/https!");

        try
            (
                // create TCP socket for the given hostName, remote port PortNumber
                Socket clientSocket = new Socket(hostName, portNumber);

                // Stream writer to the socket
                PrintWriter out =
                    new PrintWriter(clientSocket.getOutputStream(), true);
                // Stream reader from the socket
                BufferedReader in =
                    new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                // Keyboard input reader
                BufferedReader stdIn =
                    new BufferedReader(
                        new InputStreamReader(System.in))
            )
        {
            String userInput;
            // Loop until null input string
            System.out.print("I (Client) [" + InetAddress.getLocalHost()  + ":" + clientSocket.getLocalPort() + "] > ");
            while ((userInput = stdIn.readLine()) != null && !userInput.isEmpty())
            {
                // write keyboard input to the socket
                out.println(userInput);


                // read from the socket and display
                String receivedText = in.readLine();

                if(receivedText.equals("1")){
                    System.out.println("NO email found on the webpage!!!!");
                }else if(receivedText.equals("2")){
                    System.out.println("!!!Server couldn't find the web page!!!");
                }else{
                    while((receivedText = in.readLine()) != null){
                        System.out.println(receivedText);
                    }

                }
                System.out.println("Server [" + hostName +  ":" + portNumber + "] > " + receivedText);
                System.out.print("I (Client) [" + clientSocket.getLocalAddress().getHostAddress() + ":" + clientSocket.getLocalPort() + "] > ");

            }
        } catch (UnknownHostException e)
        {
            System.err.println("Unknown host " + hostName);
            System.exit(1);
        } catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}

