package rmiclient;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import rmiinterface.Wordle;

public class Client {

    public static Scanner scanner = new Scanner(System.in);

    // Parámetros de control
    public static ArrayList<Integer> ids = new ArrayList<>();
    public static int id = 0;
    public static String solution = " ";

    public static void main(String[] args) {

        // Parametros conexion server introducidos por consola
        String IP_servidor = args[0];
        int port = Integer.parseInt(args[1]);
        String nameServer = args[2];

        try {

            Registry reg = LocateRegistry.getRegistry(IP_servidor, port);
            Wordle stub = (Wordle) reg.lookup(nameServer);
            clear();
            sendID(stub);
            menu(stub);

        } catch (RemoteException e) {

            System.out.println("Host not rechable!!" + e);

        } catch (NotBoundException e) {

            System.out.println("Name not bound to any object!! " + e);
        }

    }

    /**
     * Se genera un ID aleatorio. Tambien se comprueba que este no se haya
     * utilizado.
     * 
     * @param stub objeto remoto del cual usa funciones
     * @throws RemoteException
     */
    public static void sendID(Wordle stub) throws RemoteException {
        Random random = new Random();
        id = random.nextInt(1000);
        while (ids.contains(id)) {
            id = random.nextInt(1000);
        }
        try {
            stub.login(id);
        } catch (RemoteException e) {
            System.out.println("Error a la hora de generar el id de sesion");
        }
    }

    /**
     * Implementación del juego, se llaman al las funciones correspondientes para
     * que este funcione correctamente
     * 
     * @param stub objeto remoto del cual usa funciones
     * @throws RemoteException
     */
    public static void play(Wordle stub) throws RemoteException {

        if (solution.equals(stub.sendWord())) {
            System.out.println("Ya has jugado, debes esperar a que cambien la word");
        } else {
            solution = stub.sendWord();
            System.out.println(solution);
            boolean validWord = false;
            String word;
            try {
                do {
                    do {
                        word = readWord(scanner);
                        word = word.toUpperCase();
                        if (stub.validateWord(word).equals("OK")) {
                            validWord = true;
                        } else {
                            System.out.println(stub.validateWord(word));
                        }
                    } while (!validWord);
                    validWord = false;
                    stub.compareWord(word, solution);

                    for (int i = 0; i < 5; i++) {
                        if (i < stub.sizeUsed()) {
                            System.out.println(stub.getUsed(i));
                        } else {
                            System.out.println("  |   |   |   |   |");
                        }
                    }
                    System.out.println(stub.incrementTry(id));

                } while (stub.controlTry(id) && !isSolution(word, solution));

                stub.clearUsed();

                if (!stub.controlTry(id)) {
                    System.out.println("\t\t---------- Has perdido, mas suerte la proxima vez! la word correcta era "
                            + solution + " ----------");
                } else {
                    System.out.println("\t\t---------- Felicidades, has ganado!!! ----------");
                }

                stub.endGame(id);
            } catch (RemoteException e) {
                System.out.println("Host not reachable // comunication failure!! " + e);
            }
        }

    }

    /**
     * Menu para que el usuario decida que accion quiere realizar
     * 
     * @param stub objeto remoto del cual usa funciones
     * @throws RemoteException
     */
    public static void menu(Wordle stub) throws RemoteException {
        int option;
        do {
            do {
                System.out
                        .println("Bienvenido a Wordle! Que quieres hacer? [1]Jugar - [2]Leer Intrucciones - [3]Salir");
                option = Integer.parseInt(scanner.nextLine());
            } while (option != 1 && option != 2 && option != 3);
            if (option == 1) {
                play(stub);
            } else if (option == 2) {
                instructions();
            }
            clear();

        } while ((option == 1 || option == 2) && option != 3);
    }

    /**
     * Funcion encargada de la limpieza de la consola
     */
    public static void clear() {
        System.out.flush();
    }

    /**
     * Comprueba si la palabra introducida es la solucion
     * 
     * @param tries    intento introducido por usuario
     * @param solution palabra solucion
     * @return solution == tries
     */
    public static boolean isSolution(String tries, String solution) {
        return tries.equals(solution);
    }

    /**
     * Lectura de la palabra por consola.
     * 
     * @param scanner objeto para introducir parametros por consola en medio de la
     *                ejecucion
     * @return word introducida por consola, como String.
     */
    public static String readWord(Scanner scanner) {
        System.out.println("Introduce una word: ");
        return scanner.nextLine();
    }

    /**
     * Se visualizan las reglas del juego por pantalla.
     * 
     * @throws RemoteException
     */
    public static void instructions() throws RemoteException {
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("\t\t\tWORDLE");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("REGLAS DEL JUEGO:");
        System.out.println("\t1. Intenta adivinar una word de 5 letras");
        System.out.println("\t2. Tienes 5 triess");
        System.out.println("\t3. Las letras que coincidan con su posicion se pintaran en verde");
        System.out.println("\t4. Las letras que no esten en su posicion se pintaran en amarillo");
        System.out.println("\t5. Las letras que no coincidan no tendran color y se mostraran debajo");

        System.out.println("----------------------------------------------------------------------------------");

    }

}
