import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailExtractorServer {
    public static void main(String[] args)
    {
        int portNumber = 5555;

        if (args.length > 0)
        {
            if (args.length == 1)
                portNumber = Integer.parseInt(args[0]);
            else
            {
                System.err.println("Usage: java EmailExtractorServerTCP [<port number>]");
                System.exit(1);
            }
        }

        System.out.println("EmailExtractor TCP server");

        //Prøver å åpne server socket
        try (
                // Lager ny server socket med portnummeret
                ServerSocket serverSocket = new ServerSocket(portNumber);
                // Venter på tcp client
                Socket connectSocket = serverSocket.accept();

                // Stream writer for socketen
                PrintWriter out = new PrintWriter(connectSocket.getOutputStream(), true);
                // Stream reader for socketen
                BufferedReader in = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()))
        )
        {
            InetAddress clientAddr = connectSocket.getInetAddress();
            int clientPort = connectSocket.getPort();
            String receivedText;
            // leser fra socket
            while ((receivedText = in.readLine())!=null)
            {
                System.out.println("Client [" + clientAddr.getHostAddress() +  ":" + clientPort +"] > " + receivedText);

                // extract emails
                URL url; //Lagrer URL
                StringBuilder contents;
                String outText = "";
                String pattern = "\\b[æøåÆØÅa-zA-Z0-9.-]+@[æøåÆØÅa-zA-Z0-9.-]+\\.[æøåÆØÅa-zA-Z0-9.-]+\\b"; //Email mønster
                Set<String> emailAddresses = new HashSet<>(); //Lagrer bare unike emails

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
                    //hvis den finner match, legger til i EmailAdresses
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

                // Skriver til socket
                out.println(outText);
                System.out.println("I (Server) [" + connectSocket.getLocalAddress().getHostAddress() + ":" + portNumber + "] > " + outText);
            }

            System.out.println("Done");
        } catch (IOException e)
        {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

    }
}

