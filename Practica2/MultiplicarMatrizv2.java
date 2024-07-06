static int[][] multiplica_matrices(int[][] A,int[][] B){
    int [][] C = new int [N/6][N/6];
    for (int i = 0; i ‹ N/6; i++)
        for (int j = 0; j < N/6; j++)
            for (int k = 0; k < N; k++)
                C[i][j] += A[i][k] * B[j][k];
    return C;
}

static int[][] separa_matriz(int[][] A, int inicio) {
    int[][] M = new int[N / 6][N];
    for (int i = 0; i < N / 6; i++)
        for (int j = 0; j < N; j++)
            M[i][j] = A[i + inicio][j];
    return M;
}

static void acomoda_matriz(int[][] C, int[][] A, int renglon, int columna) {
    for (int i = 0; i < N / 6; i++)
        for (int j = 0; j < N / 6; j++)
            C[i + renglon][j + columna] = A[i][j];
}

int[][] A1 = separa_matriz(A, 0);
int[][] A2 = separa_matriz(A, N / 2);
int[][] B1 = separa_matriz(B, 0);
int[][] B2 = separa_matriz(B, N / 2);

int[][] C1 = multiplica_matrices(A1, B1);
int[][] C2 = multiplica_matrices(A1, B2);
int[][] C3 = multiplica_matrices(A2, B1);
int[][] C4 = multiplica_matrices(A2, B2);

int[][] C = new int[N][N];
acomoda_matriz(C,C1,0,0);
acomoda_matriz(C,C2,0,N/2);
acomoda_matriz(C,C3,N/2,0);
acomoda_matriz(C,C4,N/2,N/2);