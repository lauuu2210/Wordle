package rmiserver;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import rmiinterface.Hello;

public class Server implements Hello {
    @Override
    public String say_hi() throws RemoteException {
        return "Hello everyone!!";
    }

    public static void main(String[] args) {
        Server obj = new Server();

        try {

            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0); // 0; puerto por default, solo debemos
                                                                           // exportar la parte del codigo que esta
                                                                           // marcada para uso remoto

            Registry reg = LocateRegistry.getRegistry("localhost", 1099); // Se puede usar sin agrumentos.
            reg.bind("MundoNombreServer", stub); // Se usa rebind en muchos ejemplos, aunque podemos pisar métodos.

            System.out.println("Server up and running!");

        } catch (RemoteException e) {

            System.out.println("Host not rechable//communication faillure!! " + e);

        } catch (AlreadyBoundException e) {

            System.out.println("Name already bound to another object!!" + e);
        }
    }

}
