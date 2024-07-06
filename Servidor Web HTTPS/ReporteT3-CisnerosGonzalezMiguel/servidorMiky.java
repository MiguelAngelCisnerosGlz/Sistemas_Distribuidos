import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class servidorMiky {

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

                SimpleDateFormat rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));

                if (req.startsWith("GET")) {
                    String[] requestParts = req.split(" ");
                    String requestedFile = requestParts[1];

                    if (requestedFile.equals("/")) {
                        requestedFile = "/archivos/prueba.txt";
                    }

                    File file = new File("." + requestedFile);
                    if (file.exists() && !file.isDirectory()) {
                        long lastModified = file.lastModified(); // Usar la fecha de modificación del archivo

                        Date lastModifiedDate = new Date(lastModified);

                        // Convertir la fecha del encabezado a un objeto Date
                        Date ifModifiedSinceDate = null;
                        if (!ifModifiedSinceHeader.isEmpty()) {
                            ifModifiedSinceDate = rfc1123.parse(ifModifiedSinceHeader);
                        }

                        // Comparar las fechas
                        if (ifModifiedSinceDate != null && lastModified <= ifModifiedSinceDate.getTime()) {
                            // El archivo no ha sido modificado, devolver 304 Not Modified
                            System.out.println("Response: 304 Not Modified");
                            output.println("HTTP/1.1 304 Not Modified");
                            output.println("Server: servidorMiky");
                            output.println();
                            output.flush();
                            connection.close();
                            return;
                        }

                        // Devolver el archivo al cliente
                        int fileLength = (int) file.length();
                        String contentMimeType = getContentType(file.getName());
                        byte[] fileData = readFileData(file, fileLength);

                        System.out.println("Response: 200 OK");
                        output.println("HTTP/1.1 200 OK");
                        output.println("Content-Type: " + contentMimeType);
                        output.println("Content-Length: " + fileLength);
                        output.println("Date: " + rfc1123.format(new Date()));
                        output.println("Last-Modified: " + rfc1123.format(lastModifiedDate)); // Usar la fecha de modificación del archivo
                        output.println("Content-Disposition: attachment; filename=\"" + file.getName() + "\"");
                        output.println();
                        output.flush();

                        dataOut.write(fileData, 0, fileLength);
                        dataOut.flush();
                    } else {
                        // Si el archivo no existe, devolver 404 Not Found
                        System.out.println("Response: 404 Not Found");
                        output.println("HTTP/1.1 404 Not Found");
                        output.println();
                        output.println("404 Not Found");
                        output.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                // Manejar la excepción de archivo no encontrado
                System.err.println("404 - Archivo no encontrado: " + e.getMessage());
                output.println("HTTP/1.1 404 Not Found");
                output.flush();
            } catch (Exception e) {
                // Manejar otras excepciones
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

        private byte[] readFileData(File file, int fileLength) throws IOException {
            byte[] fileData = new byte[fileLength];
            FileInputStream fileIn = new FileInputStream(file);
            fileIn.read(fileData);
            fileIn.close();
            return fileData;
        }

        private String getContentType(String fileName) {
            if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                return "text/html";
            } else if (fileName.endsWith(".css")) {
                return "text/css";
            } else if (fileName.endsWith(".js")) {
                return "application/javascript";
            } else if (fileName.endsWith(".png")) {
                return "image/png";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (fileName.endsWith(".gif")) {
                return "image/gif";
            } else if (fileName.endsWith(".pdf")) {
                return "application/pdf";
            } else if (fileName.endsWith(".txt")) {
                return "text/plain";
            } else {
                return "application/octet-stream";
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // Configurar SSL para el servidor
        int puerto = 8445;
        System.setProperty("javax.net.ssl.keyStore", "keystore_servidor.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "1234567");
        SSLServerSocketFactory socket_factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket server = socket_factory.createServerSocket(puerto);

        System.out.println("HTTPS server running on port: " + puerto);

        while (true) {
            Socket connection = server.accept();
            Worker w = new Worker(connection);
            w.start();
        }
    }
}
