GNU nano 4.8                                     ServidorMatrices.java                                                package main;

import java.rmi.Naming;

//probar de esta manera
public class ServidorMatrices {
    public static void main(String[] args) {
        try {
            //Esta es la instancia de la clase matrizv2
            Matricesv2 Nodo0= new Matricesv2();

            //vinculamos la instancia al registro
            String url = "rmi://localhost/Nodo0";
            Naming.rebind(url, Nodo0);

            System.out.println("La instanica"+ Nodo0 + "Ha sido registrada");


        } catch (Exception e) {
            System.err.println("Excepción en el cliente: " + e.toString());
            e.printStackTrace();
        }
    }
}
