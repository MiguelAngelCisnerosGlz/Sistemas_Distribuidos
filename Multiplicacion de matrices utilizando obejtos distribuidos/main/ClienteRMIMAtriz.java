package main;

import java.rmi.Naming;

////REvisar esto
public class ClienteRMIMAtriz {
    public static void main(String[] args) {
        try {
            int N = 3000;
            int M = 2000;
            //Se inician las matrices A y B
            long[][] matriza = iniciaMatrizA(N, M);
            long[][] matrizb = iniciaMatrizB(N, M);

//      imprimeMatriz("matrizA",matriza);
//      imprimeMatriz("matrizB",matrizb);

            //Traspuesta de la matrib B
            long[][] matrizbt = transpuestaMatrix(matrizb);
            //Dividimos la matriz A y B en 6 pedazos
            long[][][] pedazosmatrizA = divideMatriz(matriza, N, M);
            long[][][] pedazosmatrizBt = divideMatriz(matrizbt, N, M);

  //          imprimeMatriz("matrizA", matriza);
    ///        imprimeMatriz("matrizBT", matrizbt);

            String url0 = "rmi://localhost/Nodo0";
            String url1 = "rmi://74.249.24.240/Nodo1";
            String url2 = "rmi://20.36.133.9/Nodo2";

            InterfaceMatriz nodo0 = (InterfaceMatriz) Naming.lookup(url0);
            InterfaceMatriz nodo1 = (InterfaceMatriz) Naming.lookup(url1);
            InterfaceMatriz nodo2 = (InterfaceMatriz) Naming.lookup(url2);

            // Inicializar la matriz c
            long[][][] matrizresultado = new long[36][N / 6][N / 6];

    Thread[] threads = new Thread[36];
    int threadIndex = 0;

    for (int i = 0; i < 6; i++) {
        for (int j = 0; j < 6; j++) {
            final int x = i;
            final int y = j;
            int finalThreadIndex = threadIndex;
            threads[threadIndex] = new Thread(() -> {
                try {
			// Obtener la referencia al nodo correspondiente
                    InterfaceMatriz nodo;
                    if (finalThreadIndex < 12) {
                        nodo = nodo0;
                    } else if (finalThreadIndex < 24) {
                        nodo = nodo1;
                    } else {
                        nodo = nodo2;
                    }

                    // Multiplicar las matrices y almacenar el resultado en la matriz c
                    long[][] resultanteCubo = nodo.multiplica_matrices(pedazosmatrizA[x], pedazosmatrizBt[y], 12, M);

                    for (int a = 0; a < resultanteCubo.length; a++) {
                        for (int b = 0; b < resultanteCubo[a].length; b++) {
                            matrizresultado[finalThreadIndex][a][b] += resultanteCubo[a][b];
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            threads[threadIndex].start();
            threadIndex++;
 }
    }

    // Esperar a que todos los threads terminen
    for (Thread thread : threads) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Imprimir el checksum de la matriz C
    System.out.println("EL checksum de la matriz C = " + calcularChecksum(matrizresultado));
} catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static long[][] iniciaMatrizA(int N, int M) {
        long[][] A = new long[N][M];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
 A[i][j] = i + 2 * j;
            }
        }
        return A;
    }

    // NODO 0
    public static long[][] iniciaMatrizB(int N, int M) {
        long[][] B = new long[M][N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                B[i][j] = 3 * i - j;
            }
        }
        return B;
    }

    // Método para mostrar la matriz en la consola
    public static void imprimeMatriz(String name, long[][] matrix) {
        System.out.println(name + ":");
        for (long[] row : matrix) {
            for (long element : row) {
                System.out.print(element + "\t");
            }
            System.out.println();
 }
        System.out.println();
    }

    // NODO 0
// Método para sacar la transpuesta de la matriz B
    public static long[][] transpuestaMatrix(long[][] B) {
        int M = B.length; // filas de B
        int N = B[0].length; // columnas de B
        long[][] BT = new long[N][M];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                BT[i][j] = B[j][i];
            }
        }

        return BT;
    }

public static long[][][] divideMatriz(long[][] A, int N, int M) {
        long[][][] subMatrices = new long[6][N/6][M];
 for (int i = 0; i < 6; i++) {
            for (int j = 0; j < N / 6; j++) {
                System.arraycopy(A[i * (N / 6) + j], 0, subMatrices[i][j], 0, M);
            }
        }

        return subMatrices;
    }

    public static long[][] combinaMatrices(long[][][] subMatrices, int N) {
        long[][] C = new long[N][N];

        int n = N / 6;

        for (int i = 0; i < 6; i++) { // Itera sobre las filas de las submatrices
            for (int j = 0; j < 6; j++) { // Itera sobre las columnas de las submatrices
                long[][] subMatrix = subMatrices[i * 6 + j];
                for (int k = 0; k < n; k++) { // Itera sobre las filas de las submatrices
                    for (int l = 0; l < n; l++) { // Itera sobre las columnas de las submatrices
                        // Calcula la posición en C y asigna el valor
                        C[i * n + k][j * n + l] = subMatrix[k][l];
                    }
                }
            }
        }
     return C;
    }

    public static double calcularChecksum(long[][][] cubo) {
        double checksum = 0;

        for (int i = 0; i < cubo.length; i++) {
            for (int j = 0; j < cubo[i].length; j++) {
                for (int k = 0; k < cubo[i][j].length; k++) {
                    checksum += cubo[i][j][k];
                }
            }
        }

return checksum;
}

}
