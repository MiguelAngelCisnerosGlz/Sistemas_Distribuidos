import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class servidor1 {

    static class Worker extends Thread {
        Socket connection;

        Worker(Socket connection) {
            this.connection = connection;
        }

        public void run() {
            BufferedReader input = null;
            PrintWriter output = null;
            BufferedOutputStream dataOut = null;
            try {
                input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                output = new PrintWriter(connection.getOutputStream());
                dataOut = new BufferedOutputStream(connection.getOutputStream());

                String req = input.readLine();
                System.out.println(req);

                String ifModifiedSinceHeader = "";

                for (;;) {
                    String header = input.readLine();
                    if (header == null || header.equals(""))
                        break;
                    if (header.startsWith("If-Modified-Since")) ifModifiedSinceHeader = header.split(":")[1].trim();
                }

                SimpleDateFormat rfc1123 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);

                if (req.startsWith("GET")) {
                    String[] requestParts = req.split(" ");
                    String requestedFile = requestParts[1];

                    if (requestedFile.equals("/")) {
                        requestedFile = "/index.html";
                    }

                    File file = new File("." + requestedFile);
                    if (file.exists() && !file.isDirectory()) {
                        long lastModified = file.lastModified();
                        Date lastModifiedDate = new Date(lastModified);

                        if (!ifModifiedSinceHeader.isEmpty()) {
                            try {
                                Date ifModifiedSinceDate = rfc1123.parse(ifModifiedSinceHeader);
                                if (!lastModifiedDate.after(ifModifiedSinceDate)) {
                                    System.out.println("Response: 304 Not Modified");
                                    output.println("HTTP/1.1 304 Not Modified");
                                    output.println("Server: servidor1.java");
                                    output.println();
                                    output.flush();
                                    return;
                                }
                            } catch (ParseException e) {
                                System.err.println("Error al analizar la fecha: " + e.getMessage());
                            }
                        }

                        output.println("HTTP/1.1 200 OK");
                        output.println("Content-Type: application/octet-stream");
                        output.println("Content-Length: " + file.length());
                        output.println("Last-Modified: " + rfc1123.format(lastModifiedDate));
                        output.println();
                        output.flush();

                        sendFile(output, dataOut, file);
                    } else {
                        System.out.println("Response: 404 Not Found");
                        output.println("HTTP/1.1 404 Not Found");
                        output.println();
                        output.println("404 Not Found");
                        output.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("404 - Archivo no encontrado: " + e.getMessage());
                output.println("HTTP/1.1 404 Not Found");
                output.flush();
            } catch (Exception e) {
                System.err.println("Error en la solicitud: " + e.getMessage());
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                    if (dataOut != null) {
                        dataOut.close();
                    }
                    connection.close();
                } catch (Exception e2) {
                    System.err.println(e2.getMessage());
                }
            }
        }

        private void sendFile(PrintWriter output, BufferedOutputStream dataOut, File file) throws IOException {
            byte[] buffer = new byte[4096];
            int bytesRead;
            FileInputStream fileInputStream = new FileInputStream(file);
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                dataOut.write(buffer, 0, bytesRead);
            }
            dataOut.flush();
            fileInputStream.close();
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("javax.net.ssl.keyStore", "keystore_servidor.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "1234567");
        SSLServerSocketFactory socket_factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket server = socket_factory.createServerSocket(8445);

        System.out.println("HTTPS server running on port: " + 8445);

        while (true) {
            Socket connection = server.accept();
            Worker w = new Worker(connection);
            w.start();
        }
    }
}
