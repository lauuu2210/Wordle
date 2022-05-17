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

    //Control de todos los ides utilizados
    public static ArrayList<Integer> ides = new ArrayList<>();
    public static int id = 0; // Puede ser static, ya que no haremos multiples instancias en la misma maquina.
                              // Si no que sera una instancia por maquina.
    public static String solucion = "estropajo";

    public static void main(String[] args) {

        // Se parametrizan las entradas por consola.
        String IP_servidor = args[0];
        int puerto = Integer.parseInt(args[1]);
        String nombreServidor = args[2];

        try {

            Registry reg = LocateRegistry.getRegistry(IP_servidor, puerto);

            Wordle stub = (Wordle) reg.lookup(nombreServidor);

            //Se limpia la consola.
            clear();
            //Envia una id generada aleatoriamente.
            enviarID(stub);


            //Se hace display del menu.
            menu(stub);

        } catch (RemoteException e) {

            System.out.println("Host not rechable!!" + e);

        } catch (NotBoundException e) {

            System.out.println("Name not bound to any object!! " + e);
        }

    }

    /**
     * Se genera un ID aleatorio. Tambien se comprueba que este no se haya utilizado. 
     * @param stub Se pasa el objeto remoto para poder utilizar las funciones de este.
     * @throws RemoteException
     */
    public static void enviarID(Wordle stub) throws RemoteException {
        Random random = new Random();
        id = random.nextInt(1000);
        while (ides.contains(id)) {
            id = random.nextInt(1000);
        }
        try {
            stub.iniciarSesion(id);
        } catch (RemoteException e) {
            System.out.println("Error a la hora de generar el id de sesion");
        }
    }

    /**
     * Funcion encargada de 'realizar' el juego. Esto quiere decir, presentarle el 'entorno grafico' al usuario,
     * comunicarse con el Servidor para realizar validaciones y obtener 'feedback' sobre la palabra. 
     * @param stub Se pasa el objeto remoto para poder utilizar las funciones de este.
     * @throws RemoteException
     */
    public static void play(Wordle stub) throws RemoteException {

        if (solucion.equals(stub.enviarPalabra())) {
            System.out.println("Ya has jugado, debes esperar a que cambien la palabra");
        } else {
            solucion = stub.enviarPalabra();
            System.out.println(solucion);
            boolean palabraValida = false;
            String palabra;
            try {
                do {
                    do {
                        palabra = leerPalabra(scanner);
                        palabra = palabra.toUpperCase();
                        if (stub.validarPalabra(palabra).equals("OK")) {
                            palabraValida = true;
                        } else {
                            System.out.println(stub.validarPalabra(palabra));
                        }
                    } while (!palabraValida);
                    stub.compareWord(palabra, solucion);

                    for (int i = 0; i < 5; i++) {
                        if (i < stub.sizeIntroducidas()) {
                            System.out.println(stub.getIntroducidas(i));
                        } else {
                            System.out.println("  |   |   |   |   |");
                        }
                    }
                    System.out.println(stub.incrementarIntentos(id));

                } while (stub.controlIntentos(id) && !esSolucion(palabra, solucion));

                stub.clearIntroducidas();

                if (!stub.controlIntentos(id)) {
                    System.out.println("\t\t---------- Has perdido, mas suerte la proxima vez! la palabra correcta era "
                            + solucion + " ----------");
                } else {
                    System.out.println("\t\t---------- Felicidades, has ganado!!! ----------");
                }

                stub.finPartida(id);
            } catch (RemoteException e) {
                // Muy importante detallar nuestros errores para saber de donde vienen nuestros
                // errores
                System.out.println("Host not reachable // comunication failure!! " + e);
            }
        }

    }

    /**
     * Menu el cual nos permite jugar, leer las instruciones o salir del juego.
     * @param stub Se pasa el objeto remoto para poder utilizar las funciones de este.
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
     * Se comprueba si la entrada/intento es la solucion correcta.
     * @param intento
     * @param solucion Afirmativo si la solucion es correcta, caso contrario devuelve negativo.
     * @return
     */
    public static boolean esSolucion(String intento, String solucion) {
        return intento.equals(solucion);
    }

    /**
     * Lectura de palabra por consola.
     * 
     * @param scanner
     * @return palabra introducida por consola, como String.
     */
    public static String leerPalabra(Scanner scanner) {
        System.out.println("Introduce una palabra: ");
        return scanner.nextLine();
    }

    /**
     * Se visualizan las reglas del juego por pantalla.
     * @throws RemoteException
     */
    public static void instructions() throws RemoteException {
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("\t\t\tWORDLE");
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println("REGLAS DEL JUEGO:");
        System.out.println("\t1. Intenta adivinar una palabra de 5 letras");
        System.out.println("\t2. Tienes 5 intentos");
        System.out.println("\t3. Las letras que coincidan con su posicion se pintaran en verde");
        System.out.println("\t4. Las letras que no esten en su posicion se pintaran en amarillo");
        System.out.println("\t5. Las letras que no coincidan no tendran color y se mostraran debajo");

        System.out.println("----------------------------------------------------------------------------------");

    }

}
