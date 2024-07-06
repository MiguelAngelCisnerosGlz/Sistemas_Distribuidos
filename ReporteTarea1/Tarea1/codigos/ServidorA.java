import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorA{
    static class Worker extends Thread {
        Socket conexion;

        Worker(Socket conexion) {
            this.conexion = conexion;
        }

        public void run() {
            try {
                BufferedReader entrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
                PrintWriter salida = new PrintWriter(conexion.getOutputStream());

                String[] partesNumeros = entrada.readLine().split(",");
                int kInicial = Integer.parseInt(partesNumeros[0]);
                int kFinal = Integer.parseInt(partesNumeros[1]);

                // Calcular la suma de la serie de Ramanujan
                BigDecimal suma = calcularSumaRamanujan(kInicial, kFinal);

                // Enviar la suma al cliente
                salida.println(suma);
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

    public static void main(String[] args) throws Exception {
        // Puerto fijo para el servidor A
        int puerto = 8080;
        ServerSocket servidor = new ServerSocket(puerto);

        for (;;) {
            Socket conexion = servidor.accept();
            new Worker(conexion).start();
        }
    }

    // Método para calcular la suma de la serie de Ramanujan
    public static BigDecimal calcularSumaRamanujan(int inicio, int fin) {
        BigDecimal suma = BigDecimal.ZERO;
        BigDecimal a, b;

        for (int k = inicio; k <= fin; k++) {
            a = factorial(4 * k).multiply(BigDecimal.valueOf(1103 + 26390 * k));
            b = factorial(k).pow(4).multiply(BigDecimal.valueOf(396).pow(4 * k));
            suma = suma.add(a.divide(b, 100, BigDecimal.ROUND_HALF_UP));
        }

        return suma;
    }

    // Método para calcular el factorial
    public static BigDecimal factorial(int n) {
        BigDecimal result = BigDecimal.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigDecimal.valueOf(i));
        }
        return result;
    }
}
