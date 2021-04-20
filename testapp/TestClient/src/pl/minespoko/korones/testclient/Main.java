package pl.minespoko.korones.testclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		String hostname = "34.107.106.99";
        int port = 20001;
 
        /*
         * Tworzenie po³¹czenia z serwerem
         * */
        try (Socket socket = new Socket(hostname, port)) {
        	System.out.println("Polaczono...");
        	
        	/*
        	 * Tworzenie nowego w¹tku potrzebnego do asynchronicznego dzia³ania.
        	 * Strumieñ wejœcia z kodowaniem UTF-8 - odczytywany i wypisywany
        	 * */
        	new Thread(new Runnable() {
        		@Override
 				public void run() {
 					try {
 			            InputStream input = socket.getInputStream();
 			            BufferedReader reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));			 
 			            String line;
 			            while ((line = reader.readLine()) != null) {
 			                System.out.println("[Svr-IN] "+line);
 			            }
 					}catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}).start();
        	
        	/*
        	 * Tworzenie nowgo w¹tku informuj¹cego o stanie po³¹czenia,
        	 * przydatne podczas restartu serwera
        	 * */
        	new Thread(new Runnable() {
        		@Override
 				public void run() {
 					try {
 						while (true) {
 							System.out.println("#####\nConnStats:\n  Closed: "+socket.isClosed());
 	 						System.out.println("  Bound: "+socket.isBound());
 	 						System.out.println("  Connected: "+socket.isConnected());
 	 						System.out.println("  InputDown: "+socket.isInputShutdown());
 	 						System.out.println("  OutputDown: "+socket.isOutputShutdown());
 	 			            System.out.println("#####");
 	 			            Thread.sleep(10000);
						}
 					}catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}).start();
        	
        	/*
        	 * Strumieñ wyjœcia z kodowaniem UTF-8 i skaner czytaj¹cy strumieñ wejœcia programu.
 			 * Nastêpnie przepisanie ka¿dej lini wejœcia programu na wyjœcie po³¹czenia z informacj¹
        	 * */
        	OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream(),"UTF-8");
            PrintWriter writer = new PrintWriter(output, true);
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
				String ln = (String) scanner.nextLine();
				System.out.println("[OUT] "+ln);
				writer.println(ln);
			}
            System.out.println("Wylaczanie...");
            writer.close();
            socket.close();
            scanner.close();
        } catch (UnknownHostException ex) {
        	/*
        	 * Podano z³e ip serwera
        	 * */
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
        	/*
        	 * Serwer jest nieosi¹galny - firewall, zablokowane porty lub wy³¹czony serwer
        	 * */
            System.out.println("I/O error: " + ex.getMessage());
        }
	}
}
