import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Server {
    // Esta es la clase Worker que hereda de Thread. Cada Worker maneja una conexión individual.
    static class Worker extends Thread {
        // Esta es la conexión del socket que este Worker manejará.
        Socket connection;
        // Este es el constructor del Worker. Toma un objeto Socket como argumento.
        Worker(Socket connection) {
            this.connection = connection;
        }
        // Este es el método run, que se ejecuta cuando se inicia el hilo.
        public void run() {
            try {
                // Aquí se crean los objetos BufferedReader y PrintWriter para leer y escribir en la conexión.
                BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                PrintWriter output = new PrintWriter(connection.getOutputStream());
                // Aquí se lee la primera línea de la solicitud HTTP, que debería ser la línea de la solicitud GET.
                String req = input.readLine();
                // Aquí se inicializa la variable ifModifiedSinceHeader, que almacenará el valor del encabezado If-Modified-Since si está presente.
                String ifModifiedSinceHeader = "";
                // Este bucle lee todas las líneas restantes de la solicitud HTTP.
                for (;;) {
                    String cabecera = input.readLine();
                    // Si la línea comienza con "If-Modified-Since", entonces se almacena el valor del encabezado.
                    if (cabecera.startsWith("If-Modified-Since")) ifModifiedSinceHeader = cabecera.split(":")[0];
                    // Si la línea está vacía, entonces se ha llegado al final de los encabezados de la solicitud HTTP, por lo que se rompe el bucle.
                    if (cabecera.isEmpty()) break;
                }
                // Si ifModifiedSinceHeader no está vacío, entonces se imprime un mensaje indicando que el contenido fue almacenado en caché y se recibió el encabezado correcto.
                if (!ifModifiedSinceHeader.isEmpty()) System.out.println("El contenido en la cache,fue el correcto");
                // Aquí se crea un objeto SimpleDateFormat para formatear las fechas en el formato correcto para el encabezado Last-Modified.
                SimpleDateFormat fecha = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
                // Aquí se maneja la ruta GET /hola.
                if (req.startsWith("GET /1")) {
                    // Aquí se crea el contenido HTML que se devolverá.
                    String content = "<html><button onClick='alert(\"Se presiono el boton\")'>Aceptar</button></html>";
                    // Aquí se envían los encabezados de la respuesta HTTP.
                    output.println("HTTP/1.1 200 OK");
                    output.println("Content-Type: text/html; charset=utf-8");
                    output.println("Content-Length: " + content.length());
                    output.println("Server: Server.java");
                    output.println("Date: " + new Date());
                    output.println("Last-Modified: " + fecha.format(Calendar.getInstance().getTime()));
                    output.println();
                    // Aquí se envía el contenido de la respuesta HTTP.
                    output.println(content);
                    // Aquí se vacía el buffer de salida, lo que hace que cualquier dato que aún no se haya enviado se envíe inmediatamente.
                    output.flush();
                }
                // Aquí se maneja la ruta GET /archivos
                else if (req.startsWith("GET /archivos")) {
                    // Aquí se obtiene la ruta del archivo de la solicitud GET.
                    String filePath = req.split(" ")[1];
                    // Aquí se crea un objeto File para el archivo solicitado.
                    File archivodescarga = new File("." + filePath);
                    // Si el archivo existe, entonces se envían los detalles del archivo.
                    if (archivodescarga.exists()) {
                        try {
                            // Aquí se obtienen los atributos del archivo.
                            Path path = archivodescarga.toPath();
                            BasicFileAttributes attribs = Files.readAttributes(path, BasicFileAttributes.class);
                            // Aquí se imprimen los detalles del archivo.
                            System.out.println("Nombre del archivo: " + archivodescarga.getName());
                            System.out.println("Tamaño del archivo: " + archivodescarga.length() + " bytes");
                            System.out.println("Última modificación: " + archivodescarga.lastModified());
                            System.out.println("Fecha de creación: " + attribs.creationTime());
                            System.out.println("Es un directorio: " + archivodescarga.isDirectory());
                            System.out.println("Es un archivo: " + archivodescarga.isFile());
                            // Aquí se obtiene la fecha de la última modificación del archivo en el formato correcto para el encabezado Last-Modified.
                            long timestampMillis = archivodescarga.lastModified();
                            ZonedDateTime zonedDateTime = Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.of("UTC"));
                            String lastModifiedFileDate = zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME);
                            // Aquí se crea el contenido HTML que se devolverá.
                            String content = "<html><p>Files paths</p></html>";
                            // Aquí se envían los encabezados de la respuesta HTTP.
                            output.println("HTTP/1.1 200 OK");
                            output.println("Content-Length: " + content.length());
                            output.println("Server: Server.java");
                            output.println("Date: " + new Date());
                            output.println("Last-Modified: " + lastModifiedFileDate);
                            output.println();
                            // Aquí se envía el contenido de la respuesta HTTP.
                            output.println(content);
                            // Aquí se vacía el buffer de salida, lo que hace que cualquier dato que aún no se haya enviado se envíe inmediatamente.
                            output.flush();
                        } catch (Exception e) {
                            // Si se produce una excepción al intentar obtener los detalles del archivo, entonces se imprime la traza de la pila de la excepción.
                            e.printStackTrace();
                        }
                    } else {
                        // Si el archivo no existe, entonces se devuelve un error 404.
                        String mensajeerror = "<html><p>File not found :(</p></html>";
                        output.println("HTTP/1.1 404 File Not Found");
                        output.println("Server: Server.java");
                        output.println("Date: " + new Date());
                        output.println();
                        output.println(mensajeerror);
                        output.flush();
                    }
                } else {
                    // Si la ruta solicitada no existe, entonces se devuelve un error 404.
                    String error = "<html><p>Route not found :(</p></html>";
                    output.println("HTTP/1.1 404 File Not Found");
                    output.println("Content-Length: " + error.length());
                    output.println("Server: Server.java");
                    output.println("Date: " + new Date());
                    output.println();
                    output.println(error);
                    output.flush();
                }
            } catch (Exception e) {
                // Si se produce una excepción en cualquier parte del método run, entonces se imprime la traza de la pila de la excepción.
                System.err.println(e.getMessage());
            } finally {
                try {
                    // Independientemente de si se produjo una excepción o no, se cierra la conexión.
                    connection.close();
                } catch (Exception e2) {
                    // Si se produce una excepción al intentar cerrar la conexión, entonces se imprime la traza de la pila de la excepción.
                    System.err.println(e2.getMessage());
                }
            }
        }
    }
    // Este es el método main, que se ejecuta cuando se inicia el programa.
    public static void main(String [] args) throws IOException {
        // Aquí se configuran las propiedades del sistema para usar el almacén de claves y la contraseña especificados.
        System.setProperty("javax.net.ssl.keyStore", "keystore_servidor.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "1234567");
        // Aquí se obtiene la fábrica de sockets del servidor SSL predeterminada.
        SSLServerSocketFactory socket_factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        // Aquí se crea el socket del servidor en el puerto 8443.
        ServerSocket server = socket_factory.createServerSocket(8443); // 443 is the standard for HTTPS
        // Aquí se imprime un mensaje indicando que el servidor está en funcionamiento y en qué puerto está escuchando.
        System.out.println("HTTPS server running on port: " + 8443);
        System.out.println("Open: https://localhost:8443/hola");
        // Este bucle infinito acepta conexiones entrantes y crea un nuevo Worker para manejar cada conexión.
        for (;;) {
            Socket connection = server.accept();
            Worker w = new Worker(connection);
            w.start();
        }
    }
}
