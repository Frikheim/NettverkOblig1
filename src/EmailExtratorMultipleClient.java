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
    public static void main(String[] args)
    {
        int portNumber = 5555;

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

        System.out.println("EmailExtractor Multi-client TCP server.");

        try (
                // Lager server socket
                ServerSocket serverSocket = new ServerSocket(portNumber)
        )
        {
            // Venter på clienter
            while (true)
            {
                // Starter en egen tråd for hver client
                ClientThread clientserver = new ClientThread(serverSocket.accept());
                clientserver.start();
            }
        } catch (IOException e)
        {

            System.out.println("Exception occurred when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }


   // Tråd klasse for klienter
    static class ClientThread extends Thread
    {
        Socket connectSocket;
        InetAddress clientAddr;
        int serverPort, clientPort;

        public ClientThread(Socket connectSocket)
        {
            this.connectSocket = connectSocket;
            clientAddr = connectSocket.getInetAddress();
            clientPort = connectSocket.getPort();
            serverPort = connectSocket.getLocalPort();
        }

        public void run()
        {
            try (
                    // Stream writer
                    PrintWriter out = new PrintWriter(connectSocket.getOutputStream(), true);
                    // Stream reader
                    BufferedReader in = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()))
            )
            {
                String receivedText;
                // leser fra socket
                while (((receivedText = in.readLine()) != null))
                {
                    // extract emails
                    URL url; //lagrer URL
                    StringBuilder contents;
                    String outText = "";
                    String pattern = "\\b[æøåÆØÅa-zA-Z0-9.-]+@[æøåÆØÅa-zA-Z0-9.-]+\\.[æøåÆØÅa-zA-Z0-9.-]+\\b"; //Email mønster
                    Set<String> emailAddresses = new HashSet<>(); //Inneholder unike emails

                    try{
                        url = new URL(receivedText);

                        BufferedReader inUrl = new BufferedReader(new InputStreamReader(url.openStream()));
                        contents = new StringBuilder();

                        String input;
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
                    // Skriver emails/ feilmelding til socket
                    out.println(outText);
                    System.out.println("I (Server) [" + connectSocket.getLocalAddress().getHostAddress() + ":" + serverPort +"] > " + outText);
                }

                // Lukker socket
                connectSocket.close();

            } catch (IOException e)
            {
                System.out.println("Exception occurred when trying to communicate with the client " + clientAddr.getHostAddress());
                System.out.println(e.getMessage());
            }
        }
    }
}
