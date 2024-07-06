import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorB {
    public static void main(String[] args) throws Exception {
        // Puerto fijo para el servidor B
        int puerto = 80;
        ServerSocket servidor = new ServerSocket(puerto);

        for (;;) {
            Socket conexion = servidor.accept();
            new Worker(conexion).start();
        }
    }

    static class Worker extends Thread {
        Socket conexion;

        Worker(Socket conexion) {
            this.conexion = conexion;
        }

        public void run() {
            try {
                BufferedReader entrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                PrintWriter salida = new PrintWriter(conexion.getOutputStream());

                String req = entrada.readLine();
                System.out.println(req);

                for (;;) {
                    String encabezado = entrada.readLine();
                    System.out.println(encabezado);
                    if (encabezado.equals("")) {
                        break;
                    }
                }

                // Dividir el intervalo [0,3000] en tres intervalos
                int intervalo = 3000 / 3;
                BigDecimal sumaTotal = BigDecimal.ZERO;

                // Direcciones IP y puertos de las máquinas virtuales con los servidores A
                String[] ipsServidoresA = {"74.249.93.176", "172.210.193.50", "74.249.88.22"};
                int[] puertosServidoresA = {8080, 8081, 8082};

                // Conectar a las tres instancias del servidor A
                for (int i = 0; i < 3; i++) {
                    int kInicial = i * intervalo;
                    int kFinal = (i + 1) * intervalo - 1;

                    // Enviar el intervalo [K inicial,K FINAL] al servidor A
                    Socket socketA = new Socket(ipsServidoresA[i], puertosServidoresA[i]);
                    PrintWriter salidaA = new PrintWriter(socketA.getOutputStream());
                    BufferedReader entradaA = new BufferedReader(new InputStreamReader(socketA.getInputStream()));

                    salidaA.println(kInicial + "," + kFinal);
                    salidaA.flush();

                    // Recibir la suma parcial del servidor A
                    BigDecimal sumaParcial = new BigDecimal(entradaA.readLine());
                    sumaTotal = sumaTotal.add(sumaParcial);

                    socketA.close();
                }

                // Calcular la aproximación de PI
                BigDecimal pi = BigDecimal.ONE.divide(sumaTotal.multiply(BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(Math.sqrt(2))).divide(BigDecimal.valueOf(9801), 100, BigDecimal.ROUND_HALF_UP)), 100, BigDecimal.ROUND_HALF_UP);

                // Enviar la aproximación de PI al cliente
                salida.println("HTTP/1.1 200 OK");
                salida.println("Content-type: text/plain");
                salida.println();
                salida.println(pi);
                salida.flush();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            } finally {
                try {
                    conexion.close();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}
