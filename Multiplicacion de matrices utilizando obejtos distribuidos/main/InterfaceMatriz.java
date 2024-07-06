package main;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceMatriz  extends Remote {
    
    public long[][] multiplica_matrices(long[][] A, long[][] B, int N, int M) throws RemoteException;

}
