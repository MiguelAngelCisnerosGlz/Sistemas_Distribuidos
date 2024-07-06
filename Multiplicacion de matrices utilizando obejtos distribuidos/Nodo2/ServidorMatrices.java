package Nodo2;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

//probar de esta manera
public class ServidorMatrices {
    public static void main(String[] args) {
        try {
            //Esta es la instancia de la clase matrizv2
            Matricesv2 nodo2= new Matricesv2();

            //vinculamos la instancia al registro
            String url = "rmi://localhost/Nodo2";
            Naming.rebind(url, nodo2);

            System.out.println("La instanica"+ nodo2 + "Ha sido registrada");


        } catch (Exception e) {
            System.err.println("Excepción en el cliente: " + e.toString());
            e.printStackTrace();
        }
    }
}