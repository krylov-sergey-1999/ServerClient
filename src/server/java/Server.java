package server.java;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

public class Server {
    private static int port = 8080;

    public static void main(String[] args) throws Throwable {
        ServerSocket ss = new ServerSocket(port);
        while (true) {
            System.out.println("В ожидании клиента...");
            Socket s = ss.accept();
            System.err.println("Клиент принял");
            new Thread(new SocketProcessor(s)).start();
        }
    }

    private static class SocketProcessor implements Runnable {

        private Socket socket;
        private InputStream sin;
        private OutputStream sout;

        private SocketProcessor(Socket s) throws Throwable {
            this.socket = s;
            this.sin = socket.getInputStream();
            this.sout = socket.getOutputStream();
        }

        private String getStringFromHtml(String path){
            try(FileInputStream fin = new FileInputStream(path)){
                String result = new BufferedReader(new InputStreamReader(fin))
                        .lines().collect(Collectors.joining("\n"));
                return result;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return path;
        }

        public void run() {
            try {

                readInputHeaders();
                writeResponse(getStringFromHtml("D:\\IT\\Java\\Box\\Server\\src\\server\\java\\index.html"));
            } catch (Throwable t) {
                /*do nothing*/
            } finally {
                try {
                    socket.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
            System.err.println("Client processing finished");
        }

        private void writeResponse(String s) throws Throwable {
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Server: YarServer/2009-09-09\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + s.length() + "\r\n" +
                    "Connection: close\r\n\r\n";
            String result = response + s;
            sout.write(result.getBytes());
            sout.flush();
        }

        private void readInputHeaders() throws Throwable {
            BufferedReader br = new BufferedReader(new InputStreamReader(sin));
            while(true) {
                String s = br.readLine();
                if(s == null || s.trim().length() == 0) {
                    break;
                }
            }
        }

    }
}