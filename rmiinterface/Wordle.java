package rmiinterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface Wordle extends Remote {

    public String enviarPalabra() throws RemoteException;

    public String validarPalabra(String word) throws RemoteException;

    public ArrayList<String> compareWord(String attempt, String word) throws RemoteException;

    public boolean controlIntentos(int id) throws RemoteException;

    public void iniciarSesion(int id) throws RemoteException;

    public void clearIntroducidas() throws RemoteException;

    public String getIntroducidas(int index) throws RemoteException;

    public int sizeIntroducidas() throws RemoteException;

    public void finPartida(int option) throws RemoteException;

    public int incrementarIntentos(int id) throws RemoteException;

}