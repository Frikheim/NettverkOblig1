import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailExtratorMultipleClient {
    public static void main(String[] args) throws IOException
    {
        int portNumber = 5555; // Default port to use

        if (args.length > 0)
        {
            if (args.length == 1)
                portNumber = Integer.parseInt(args[0]);
            else
            {
                System.err.println("Usage: java EmailExtractorServerMultiClients [<port number>]");
                System.exit(1);
            }
        }

        System.out.println("Hi, I am the EmailExtractor Multi-client TCP server.");

        try (
                // Create server socket with the given port number
                ServerSocket serverSocket =
                        new ServerSocket(portNumber);
        )
        {
            String receivedText;
            // continuously listening for clients
            while (true)
            {
                // create and start a new ClientServer thread for each connected client
                ClientService clientserver = new ClientService(serverSocket.accept());
                clientserver.start();
            }
        } catch (IOException e)
        {

            System.out.println("Exception occurred when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }


    /***
     * This class serves a client in a separate thread
     */
    static class ClientService extends Thread
    {
        Socket connectSocket;
        InetAddress clientAddr;
        int serverPort, clientPort;

        public ClientService(Socket connectSocket)
        {
            this.connectSocket = connectSocket;
            clientAddr = connectSocket.getInetAddress();
            clientPort = connectSocket.getPort();
            serverPort = connectSocket.getLocalPort();
        }

        public void run()
        {
            try (
                    // Stream writer to the socket
                    PrintWriter out =
                            new PrintWriter(connectSocket.getOutputStream(), true);
                    // Stream reader from the connection socket
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connectSocket.getInputStream()));
            )
            {

                String receivedText;
                // read from the connection socket
                while (((receivedText = in.readLine()) != null))
                {
                    // extract emails here
                    URL url; //Store the url
                    StringBuilder contents;
                    String outText = "";
                    String pattern = "\\b[æøåÆØÅa-zA-Z0-9.-]+@[æøåÆØÅa-zA-Z0-9.-]+\\.[æøåÆØÅa-zA-Z0-9.-]+\\b"; //Email Address Pattern
                    Set<String> emailAddresses = new HashSet<>(); //Contains unique email addresses

                    try{
                        url = new URL(receivedText);

                        BufferedReader inUrl = new BufferedReader(new InputStreamReader(url.openStream()));
                        contents = new StringBuilder();

                        String input = "";
                        while((input = inUrl.readLine()) != null) {
                            contents.append(input);
                        }

                        Pattern pat = Pattern.compile(pattern);
                        Matcher match = pat.matcher(contents);
                        //If match found, append to emailAddresses
                        while(match.find()) {
                            emailAddresses.add(match.group());
                        }
                    } catch (MalformedURLException e) {
                        System.out.println("2: Please include Protocol in your URL e.g. http://www.google.com");
                        outText = "2";
                    } catch (IOException e) {
                        System.out.println("2: Unable to read URL due to Unknown Host..");
                        outText = "2";
                    }
                    if(outText.isEmpty()){
                        if(emailAddresses.isEmpty()) {
                            System.out.println("1: Web page does not contain any email adresses");
                            outText = "1";

                        }
                        else {
                            outText = "0 \n";
                            for (String email : emailAddresses) {
                                outText += email + "\n";
                            }
                        }
                    }
                    // Write the response message string to the connection socket
                    System.out.println("I (Server) [" + connectSocket.getLocalAddress().getHostAddress() + ":" + serverPort +"] > " + outText);
                }

                // close the connection socket
                connectSocket.close();

            } catch (IOException e)
            {
                System.out.println("Exception occurred when trying to communicate with the client " + clientAddr.getHostAddress());
                System.out.println(e.getMessage());
            }
        }
    }
}
