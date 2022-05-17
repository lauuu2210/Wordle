package rmiinterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface Wordle extends Remote {

    public String sendWord() throws RemoteException;

    public String validateWord(String word) throws RemoteException;

    public ArrayList<String> compareWord(String attempt, String word) throws RemoteException;

    public boolean controlTry(int id) throws RemoteException;

    public void login(int id) throws RemoteException;

    public void clearUsed() throws RemoteException;

    public String getUsed(int index) throws RemoteException;

    public int sizeUsed() throws RemoteException;

    public void endGame(int option) throws RemoteException;

    public int incrementTry(int id) throws RemoteException;

}