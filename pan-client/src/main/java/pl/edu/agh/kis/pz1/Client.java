package pl.edu.agh.kis.pz1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client{
    public void start(int port) throws IOException {
        try(SocketChannel client = SocketChannel.open();
        Scanner scanner = new Scanner(System.in)) {
            client.connect(new InetSocketAddress("localhost",port));
            client.configureBlocking(false);

            Thread wczytywanie = new Thread(() -> odczytajInfo(client));
            wczytywanie.setDaemon(true); //Zapewnia zakonczenie watku po zakonczeniu watku glownego
            wczytywanie.start();

            while(scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                client.write(buffer);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private void odczytajInfo(SocketChannel client){
        ByteBuffer buffer = ByteBuffer.allocate(254);
        try{
            while(true){
                buffer.clear();
                if(client.read(buffer) > 0){
                    buffer.flip();
                    String info = new String(buffer.array(), 0, buffer.limit());
                    System.out.format(info+"\n");
                }
            }
        }
        catch(IOException e){
            System.out.println("Rozłączono!");
        }

    }

    public static void main(String[] args) throws IOException {
        int port = 0;
        if(args.length > 0){
            try{
                port = Integer.parseInt(args[0]);
            }
            catch(NumberFormatException e){
                System.err.println("Niepoprawny numer portu!");
                System.exit(1);
            }
        }
        else{
            port = 1234;
        }
        Client client = new Client();
        client.start(port);
    }
}