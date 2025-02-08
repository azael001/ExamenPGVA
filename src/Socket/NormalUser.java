package Socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Scanner;
public class NormalUser extends Thread {
    public static String direccionIPGrupo = "225.10.10.10";
    public static int puerto = 6998;
    public static MulticastSocket socketMulticast = null;
    public static InetSocketAddress grupo = null;
    public static NetworkInterface netIf = null;
    public static String nombre = "";
    private static boolean ejecucion = true;
    public int tipoHilo = -1;
    public static final int enviar = 1;
    public static final int recibir = 2;
    public static final int recibirPrivadoNum = 3;
    static Socket serverSocket;
    static InetSocketAddress serverSocketAddress;
    static String IP = "localhost";


    public NormalUser(int tipoHilo) {
        this.tipoHilo = tipoHilo;
    }

    private static void pedirNombre() {
        Scanner name = new Scanner(System.in);
        System.out.print("Escribe tu nombre: ");
        nombre = name.nextLine();
    }

    public static void main(String[] args) {
        try {
            ejecucion = true;
            pedirNombre();
            socketMulticast = new MulticastSocket(puerto);
            InetAddress dir = InetAddress.getByName(direccionIPGrupo);
            grupo = new InetSocketAddress(dir, puerto);
            netIf = NetworkInterface.getByInetAddress(dir);
            socketMulticast.joinGroup(grupo, netIf);


            NormalUser enviarC = new NormalUser(enviar);
            NormalUser recibirC = new NormalUser(recibir);


            enviarC.start();
            recibirC.start();


            enviarC.join();
            recibirC.join();

            socketMulticast.leaveGroup(grupo, netIf);
            socketMulticast.close();
            System.out.println("Socket cerrado");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        switch (tipoHilo) {
            case enviar:
                runEnviarPrivado();
                break;

        }
    }



    private void runEnviarPrivado() {
        try {
            Socket clientSocket = new Socket();
            InetSocketAddress addr = new InetSocketAddress("localhost", 6668);
            //establece la conexión con el servidor en la dirección y puerto especificados.
            clientSocket.connect(addr);

            //Obtiene el flujo de salida del socket para enviar datos al servidor.
            OutputStream os = clientSocket.getOutputStream();
            InputStream is = clientSocket.getInputStream();
            Scanner scanner = new Scanner(System.in);
            String mensajePrivado = "";
            byte[] mensaje = new byte[1024];
            while (true) {
                mensajePrivado = scanner.nextLine();
                if (mensajePrivado.equals("fin")) {
                    os.write(mensajePrivado.getBytes());
                    is.read(mensaje);
                    String smens = (new String(mensaje)).trim();
                    System.out.println(smens);
                    break;
                } else {
                    os.write(mensajePrivado.getBytes());
                }
                is.read(mensaje);
                String smens = (new String(mensaje)).trim();
                System.out.println(smens);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}











