package rmiinterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Hello extends Remote {
    public String say_hi() throws RemoteException;
}