package rmiserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import rmiinterface.Wordle;

public class Server implements Wordle {

    // Definición de los colores que utilizaremos para imprimir por pantalla
    public static final String TEXT_GREEN = "\u001B[32m";
    public static final String TEXT_YELLOW = "\u001B[33m";
    public static final String TEXT_WHITE = "\u001B[37m";

    // Registro de palabras que ha introducido el usuario en la partida actual
    public static final ArrayList<String> usedword = new ArrayList<>();

    // Control de las sesiones y clientes conectados.
    public static final Map<Integer, Integer> onlineClient = new HashMap<>();

    // Diccionario de las palabras las cuales pueden ser palabra a adivinar, además
    // del diccionario contra el que se validan las palabras introducidas
    private static List<String> wordList = readDictionary("diccionario.txt");

    // Control para evitar que una palabra pueda salir 2 días seguidos como palabra
    // a adividar
    private static Queue usedWord = new LinkedList();

    public static Scanner scanner = new Scanner(System.in);
    public static String solution;

    public static void main(String[] args) {

        // Se inicializa el server.
        Server srv = new Server();

        // Se parametrizan las entradas por consola para iniciar el servidor.
        String server_IP = args[0];
        int port = Integer.parseInt(args[1]);
        String nameServer = args[2];

        try {

            Wordle stub = (Wordle) UnicastRemoteObject.exportObject(srv, port);

            Registry reg = LocateRegistry.getRegistry(server_IP, port);
            reg.bind(nameServer, stub);

            // Se obtiene la 'palabra del dia', es decir, la palabra seleccionada por el
            // servidor para el juego actual.
            solution = getWord();

            System.out.println("Server up and running!");

            // Cada 5000 segundos se cambia la palabra que se juega, simulando esto los
            // diferentes días de juego.
            int duracion = 5000;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    solution = getWord();
                }
            }, 0, TimeUnit.MINUTES.toSeconds(duracion));

        } catch (RemoteException e) {
            System.out.println("Host not rechable//communication faillure!! " + e);

        } catch (AlreadyBoundException e) {
            System.out.println("Name already bound to another object!!" + e);
        }
    }

    /**
     * Obtención de la palabra la cual va a estar en juego ese día, común a todos
     * los usuarios que jueguen ese día
     * 
     * @return Una palabra aleatoria del diccionario.
     */
    private static String getWord() {
        List<String> posibleWords = readDictionary("diccionario.txt");
        Random random = new Random();
        int size = posibleWords.size();
        int selection = random.nextInt(size);
        String word = posibleWords.get(selection);
        while (usedWord.contains(word)) {
            selection = random.nextInt(size);
            word = posibleWords.get(selection);
        }
        repeatedWord(word);
        usedWord.add(word);
        return word.toUpperCase();
    }

    /**
     * Se controla que las palabras que has sido usadas se actualicen.
     * 
     * @param word Palabra utilizada.
     *
     */
    private static void repeatedWord(String word) {
        if (usedWord.size() == 5)
            usedWord.remove();
    }

    /**
     * Se envia la solution actual al Cliente.
     */
    public String sendWord() {
        return solution;
    }

    /**
     * Registramos la sesion actual, con el id del cliente y el número de intentos.
     * 
     * @param id Id del Cliente.
     */
    public void login(int id) {
        onlineClient.put(id, 0);
    }

    /**
     * Control de intentos.
     * 
     * @param id Id del Cliente.
     * @return tries < 5.
     */
    public boolean controlTry(int id) {
        return onlineClient.get(id) < 5;
    }

    /**
     * Incremento de los intentos.
     * 
     * @param id Id del Cliente.
     * @return tries
     */
    public int incrementTry(int id) {
        int tries = onlineClient.get(id);
        tries++;
        onlineClient.replace(id, tries);
        return tries;
    }

    /**
     * Se lee un archivo para crear una lista de palabras disponibles para el juego
     * y diccionario sobre el que se validan las palabras.
     * 
     * @param archivo Fichero a leer.
     * @return Lista de palabras del diccionario.
     */
    public static List<String> readDictionary(String archivo) {

        List<String> wordList = new ArrayList<>();

        try {
            File obj = new File(archivo);
            Scanner myReader = new Scanner(obj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                wordList.add(data.toUpperCase());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("No se encontro el archivo!");
            e.printStackTrace();
        }

        return wordList;
    }

    /**
     * Se realizan comprobaciones sobre el formato de la palabra introducida por el
     * usuario.
     * Si la palabra tiene el formato adecuado, devolvemos 'OK', caso contrario, el
     * mensaje apropiado según el problema de la palabra introducida.
     * 
     * @param word Palabra a validar
     * @return 'OK' o mensaje indicando el error de formato.
     */
    public String validateWord(String word) {
        String msg = "OK";
        if (word.length() < 5) {
            msg = "La palabra es demasiado pequenha... Introduce otra!!\n";
        } else if (word.length() > 5) {
            msg = "La palabra es demasiado GRANDE... Introduce otra!!\n";
        } else if (!wordList.contains(word.toUpperCase())) {
            msg = "La palabra no existe en el diccionario! :(\n";
        }
        return msg;
    }

    /**
     * Colorear las letras.
     * 
     * @param letter Letra que se desea colorear.
     * @param color  Color deseado.
     * @return Se colorea la letra del color deseado y se resetea el color para
     *         evitar fallos
     */
    public static String colorLetters(char letter, String color) {

        String ANSI_RESET = "\u001B[0m";

        return color + letter + ANSI_RESET;
    }

    /**
     * Funcion encargada de comparar la palabra introducida con la que hay que
     * adivinar.
     * 
     * @param attempt Palabra intento por parte del usuario.
     * @param word    Palabra la cual hay que adivinar, oculta para el usuario.
     * @return String que informa al usuario sobre su intento, con colores.
     */
    public ArrayList<String> compareWord(String attempt, String word) {
        String print = "";
        for (int i = 0; i < word.length(); i++) {
            char letter = attempt.charAt(i);

            if (letter == word.charAt(i)) {
                print += colorLetters(letter, TEXT_GREEN);
            } else if (word.indexOf(letter) != -1) {

                print += colorLetters(letter, TEXT_YELLOW);
            } else {

                print += colorLetters(letter, TEXT_WHITE);
            }
            if (i != print.length() - 1) {
                print += " | ";
            }

        }
        usedword.add(print);
        return usedword;
    }

    /**
     * Se obtiene la palabra indicada.
     * 
     * @param index index del intento a obtener
     * @return intento correspondiente al indice
     * 
     */
    public String getUsed(int index) {
        return usedword.get(index);
    }

    /**
     * @return tamaño de usedWord
     */
    public int sizeUsed() {
        return usedword.size();
    }

    /**
     * Vacía usedWord.
     */
    public void clearUsed() {
        usedword.clear();
    }

    /**
     * Reinicia los tries si el cliente acaba la partida, para preparar la siguiente
     */
    public void endGame(int id) throws RemoteException {
        onlineClient.replace(id, 0);
    }

}
