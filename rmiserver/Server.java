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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import rmiinterface.Wordle;

public class Server implements Wordle {

    //Color del texto definido ANSI
    public static final String TEXT_GREEN = "\u001B[32m";
    public static final String TEXT_YELLOW = "\u001B[33m";
    public static final String TEXT_WHITE = "\u001B[37m";

    //Control de las palabras que ha introducido el usuario.
    public static final ArrayList<String> palabrasIntroducidas = new ArrayList<>();
    //Control de las sesiones y clientes conectados.
    public static final Map<Integer, Integer> clientesConectados = new HashMap<>();

    //Se crea una lista con todas las palabras disponibles en el diccionario.
    private static List<String> listaPalabras = leerDiccionario("diccionario.txt");
    //Control para que no se repitan las mismas palabras seguidamente. Cada palabra debe esperar 5 palabras para volver a aparecer.
    private static Queue palabrasUsadas = new LinkedList();

    public static Scanner scanner = new Scanner(System.in);
    public static String solucion;

    public static void main(String[] args) {

        // Se inicializa el server.
        Server srv = new Server();

        // Se parametrizan las entradas por consola.
        String IP_servidor = args[0];
        int puerto = Integer.parseInt(args[1]);
        String nombreServidor = args[2];

        try {

            // Se exporta el objeto remoto 'Wordle'.
            Wordle stub = (Wordle) UnicastRemoteObject.exportObject(srv, puerto); // El 0 selecciona el puerto
                                                                                  // default (1099).

            Registry reg = LocateRegistry.getRegistry(IP_servidor, puerto);
            reg.bind(nombreServidor, stub);

            // Se obtiene la 'palabra del dia', es decir, la palabra seleccionada por el
            // servidor para el juego actual.
            solucion = getWord();

            System.out.println("Server up and running!");

            //Cada 200 segundos se cambia la palabra que se juega.
            int duracion = 200;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    solucion = getWord();
                }
            }, 0, TimeUnit.MINUTES.toSeconds(duracion));// Wait 0 ms before doing the action and do it every 1000ms (1second)

        } catch (RemoteException e) {
            System.out.println("Host not rechable//communication faillure!! " + e);

        } catch (AlreadyBoundException e) {
            System.out.println("Name already bound to another object!!" + e);
        }
    }

    /**
     * Se lee el diccionario con la funcion leerDiccionario(String archivo)
     * y se escoge una palabra aleatoria. Se asegura que la palabra no ha 
     * utilizado recientemente.
     * 
     * @return Una palabra aleatoria del diccionario.
     */
    private static String getWord() {
        List<String> posibleWords = leerDiccionario("diccionario.txt");
        Random random = new Random();
        int size = posibleWords.size();
        int selection = random.nextInt(size);
        String word = posibleWords.get(selection);
        while (palabrasUsadas.contains(word)) {
            selection = random.nextInt(size);
            word = posibleWords.get(selection);
        }
        palabrasRepetidas(word);
        palabrasUsadas.add(word);
        return word.toUpperCase();
    }

    /**
     * Se controla que las palabras que has sido usadas se actualicen.
     * @param word Palabra utilizada.
     *
     */
    private static void palabrasRepetidas(String word) {
        if (palabrasUsadas.size() == 5)
            palabrasUsadas.remove();
    }

    /**
     * Se envia la solucion actual al Cliente.
     */
    public String enviarPalabra() {
        return solucion;
    }

    /**
     * Se inicializa la sesion de un Cliente con su id y los intentos a 0.
     * @param id Id del Cliente.
     */
    public void iniciarSesion(int id) {
        clientesConectados.put(id, 0);
    }

    /**
     * Se controla que los intentos no sean superiores a 5.
     * @param id Id del Cliente.
     * @return Si el numero de intentos del Cliente solicitado es menor que 5.
     */
    public boolean controlIntentos(int id) {
        return clientesConectados.get(id) < 5;
    }

    /**
     * Se incrementan lo intentos del Cliente solicitado.
     * @param id Id del Cliente.
     * @return numero de intentos actual del Cliente solicitado.
     */
    public int incrementarIntentos(int id) {
        int intentos = clientesConectados.get(id);
        intentos++;
        clientesConectados.replace(id, intentos);
        return intentos;
    }

    /**
     * Se lee un archivo para crear una lista de palabras disponibles para el juego.
     * 
     * @param archivo Fichero a leer.
     * @return Lista de palabras del diccionario.
     */
    public static List<String> leerDiccionario(String archivo) {

        List<String> listaPalabras = new ArrayList<>();

        try {
            File myObj = new File(archivo);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                listaPalabras.add(data.toUpperCase());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("No se encontro el archivo!");
            e.printStackTrace();
        }

        return listaPalabras;
    }

    /**
     * Se realizan comprobaciones sobre el formato de la palabra introducida por el usuario.
     * Si la palabra tiene el formato adecuado, devolvemos 'OK', caso contrario, el mensaje apropiado.
     *@param word Palabra a validar
     * @return 'OK' o mensaje indicando el error de formato.
     */
    public String validarPalabra(String word) {
        String msg = "OK";
        if (word.length() < 5) {
            msg = "La palabra es demasiado pequenha... Introduce otra!!\n";
        } else if (word.length() > 5) {
            msg = "La palabra es demasiado GRANDE... Introduce otra!!\n";
        } else if (!listaPalabras.contains(word.toUpperCase())) {
            msg = "La palabra no existe en el diccionario! :(\n";
        }
        return msg;
    }

    /**
     * Funcion encargada de colorear las letras para hacer más visual el juego.
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
     * @param word  Palabra la cual hay que adivinar, oculta para el usuario.
     * @return String que informa al usuario sobre su intento.
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
        palabrasIntroducidas.add(print);
        return palabrasIntroducidas;
    }

    /** 
     * Se obtiene la palabra indicada. 
     * @param index 
     * @return El intento que indica el indice.
     * 
     */
    public String getIntroducidas(int index) {
        return palabrasIntroducidas.get(index);
    }

    /**
     * @return "Numero de intentos" que ha introducido el Cliente.
     */
    public int sizeIntroducidas() {
        return palabrasIntroducidas.size();
    }

    /**
     * Se limpian los intentos que ha realizado el Cliente.
     */
    public void clearIntroducidas() {
        palabrasIntroducidas.clear();
    }

    /**
     * Cuando el Cliente reinicia el juego o acaba la partida, limpiamos sus intentos.
     */
    public void finPartida(int id) throws RemoteException {
        clientesConectados.replace(id, 0);
    }

}
