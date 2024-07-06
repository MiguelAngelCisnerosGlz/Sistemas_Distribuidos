package Nodo1;

import java.rmi.Naming;

//probar de esta manera
public class ServidorMatrices {
    public static void main(String[] args) {
        try {
            //Esta es la instancia de la clase matrizv2
            Matricesv2 nodo1= new Matricesv2();

            //vinculamos la instancia al registro
            String url = "rmi://localhost/Nodo1";
            Naming.rebind(url, nodo1);

            System.out.println("La instanica"+nodo1 + "Ha sido registrada");


        } catch (Exception e) {
            System.err.println("Excepción en el cliente: " + e.toString());
            e.printStackTrace();
        }
    }
}
