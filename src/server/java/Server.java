package server.java;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

public class Server {
    private static int port = 6666; // порт который открывает сервер

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
            File file = new File(path);
            if (file.exists()) {
                try (FileInputStream fin = new FileInputStream(path)) {
                    String result = new BufferedReader(new InputStreamReader(fin)).lines().collect(Collectors.joining("\n")); // преобразуем InputStream в строку
                    return result;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else {
                return "File missing";
            }
            return path;
        }

        private String buildPath(String str) {
            File file = new File("exemple.txt");
            String s = file.getAbsolutePath();
            s = s.substring(0, s.indexOf("exemple.txt"));
            s = s + "\\src\\resources\\" + str;
            return s;
        }

        public void run() {
            try {
                writeResponse(readInputHeaders());
            } catch (Throwable t) {
                /*do nothing*/
            } finally {
                try {
                    socket.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
            System.err.println("Обработка клиента завершена.");
        }

        private void writeResponse(String s) throws Throwable {
            // Стандарт
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Server: Server/2017-12-20\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + s.length() + "\r\n" +
                    "Connection: close\r\n\r\n";
            String result = response + s;
            sout.write(result.getBytes()); // отсылаем клиенту обратно ту самую строку текста
            sout.flush(); // заставляем поток закончить передачу данных.
        }

        private String readInputHeaders() throws Throwable {
            BufferedReader in = new BufferedReader(new InputStreamReader(sin));
            int k = 0;
            while(true) {
                String s = in.readLine();
                if (k == 0) {
                    int l = s.indexOf('/') + 1;
                    int r = s.indexOf("html");
                    int help = s.lastIndexOf('/');
                    if (help-l == 5){
                        return getStringFromHtml(buildPath("index.html"));
                    }
                    if (r < l) {
                        return getStringFromHtml(buildPath("error.html"));
                    } else {
                        s = s.substring(l, r) + "html";
                        return getStringFromHtml(buildPath(s));
                    }
                }

                if(s == null || s.trim().length() == 0) {
                    break;
                }
                k++;
            }
            return "Error";
        }

    }
}