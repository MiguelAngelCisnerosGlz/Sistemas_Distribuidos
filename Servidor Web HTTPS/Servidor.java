import javax.net.ssl.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Servidor{

    static class Worker extends Thread {
        Socket connection;

        Worker(Socket connection) {
            this.connection = connection;
        }

        public void run() {
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                PrintWriter output = new PrintWriter(connection.getOutputStream(), true);

                String req = input.readLine();
                if (req == null) {
                    connection.close();
                    return;
                }
                String[] parts = req.split(" ");
                String method = parts[0];
                String path = parts[1];

                if ("GET".equals(method)) {
                    handleGET(path, output);
                } else {
                    sendResponse(404, "Not Found", "<html><p>Route not found :(</p></html>", output);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleGET(String path, PrintWriter output) throws IOException {
            File file = new File("." + path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    sendResponse(200, "OK", "<html><p>Directories not supported</p></html>", output);
                } else {
                    sendFileResponse(file, output);
                }
            } else {
                sendResponse(404, "Not Found", "<html><p>File not found :(</p></html>", output);
            }
        }

        private void sendFileResponse(File file, PrintWriter output) throws IOException {
            SimpleDateFormat rfc1123 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
            String contentType = getContentType(file);
            String lastModified = rfc1123.format(new Date(file.lastModified()));
            String content = new String(Files.readAllBytes(file.toPath()));

            output.println("HTTP/1.1 200 OK");
            output.println("Content-Type: " + contentType);
            output.println("Content-Length: " + content.length());
            output.println("Last-Modified: " + lastModified);
            output.println();
            output.println(content);
        }

        private String getContentType(File file) throws IOException {
            String fileName = file.getName();
            String contentType;
            if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                contentType = "text/html";
            } else if (fileName.endsWith(".txt")) {
                contentType = "text/plain";
            } else if (fileName.endsWith(".css")) {
                contentType = "text/css";
            } else if (fileName.endsWith(".js")) {
                contentType = "application/javascript";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (fileName.endsWith(".gif")) {
                contentType = "image/gif";
            } else {
                contentType = "application/octet-stream";
            }
            return contentType;
        }

        private void sendResponse(int statusCode, String statusMessage, String content, PrintWriter output) {
            output.println("HTTP/1.1 " + statusCode + " " + statusMessage);
            output.println("Content-Type: text/html; charset=utf-8");
            output.println("Content-Length: " + content.length());
            output.println();
            output.println(content);
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("javax.net.ssl.keyStore", "keystore_server.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "1234567");
        
        SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket serverSocket = sslServerSocketFactory.createServerSocket(8443);

        System.out.println("HTTPS server running on port: 8443");
        System.out.println("Open: https://localhost:8443");

        while (true) {
            Socket connection = serverSocket.accept();
            Worker worker = new Worker(connection);
            worker.start();
        }
    }
}
