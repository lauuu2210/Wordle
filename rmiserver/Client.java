package rmiclient;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import rmiinterface.Hello;

public class Client {
    public static void main(String[] args) {

        try {

            Registry reg = LocateRegistry.getRegistry("localhost", 1099);

            Hello stub = (Hello) reg.lookup("MundoNombreServer");

            String msg = stub.say_hi();

            System.out.println(msg);

        } catch (RemoteException e) {

            System.out.println("Host not rechable!!" + e);

        } catch (NotBoundException e) {

            System.out.println("Name not bound to any object!! " + e);
        }

    }

}
