package Nodo1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public interface InterfaceMatriz  extends Remote{

public long[][] multiplica_matrices(long[][] A, long[][] BT, int N, int M) throws RemoteException;

}


