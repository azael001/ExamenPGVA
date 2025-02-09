package Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Scanner;
public class NormalUser extends Thread {
    public static String direccionIPGrupo = "225.10.10.10";
    public static int puerto = 6998;
    public static int puertoSocket=6668;
    public static MulticastSocket socketMulticast = null;
    public static InetSocketAddress grupo = null;
    public static NetworkInterface netIf = null;
    public static String nombre = "";
    private static boolean ejecucion = true;
    public int tipoHilo = -1;
    public static final int enviar = 1;
    public static final int recibir = 2;
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
            case recibir:
                runRecibirMulticast();
                break;
                case enviar:
                    socket();
                    break;
        }
    }
    private void runRecibirMulticast() {
        final int tamanoBufferMensaje = 1024;
        String mensaje;
        byte[] bufferMensajeRecibido = new byte[tamanoBufferMensaje];
        while (ejecucion) {
            try {
                DatagramPacket paquete = new DatagramPacket(bufferMensajeRecibido, bufferMensajeRecibido.length);
                socketMulticast.receive(paquete);
                mensaje = new String(paquete.getData(), 0, paquete.getLength());
                System.out.println(mensaje);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void socket(){
        try {
            Socket clientSocket = new Socket();
            InetSocketAddress addr = new InetSocketAddress(IP, puertoSocket);
            clientSocket.connect(addr);
            OutputStream os = clientSocket.getOutputStream();
            os.write(nombre.getBytes());
            new Thread(() -> runEnviar(clientSocket)).start();
            new Thread(() -> runRecibir(clientSocket)).start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private void runEnviar(Socket clientSocket) {
        try {
            //establece la conexión con el servidor en la dirección y puerto especificados.

            //Obtiene el flujo de salida del socket para enviar datos al servidor.
            OutputStream os = clientSocket.getOutputStream();
            InputStream is = clientSocket.getInputStream();
            byte[] mensajex = new byte[1024];
            Scanner scanner = new Scanner(System.in);

            String mensajeS = "";
            while (true) {
                mensajeS = scanner.nextLine();
                String mensaje[] = mensajeS.split(":");
                if (mensajeS.equals("fin")) {
                    break;
                }
                if (mensaje[0].equalsIgnoreCase("Privado")) {
                    os.write(mensaje[1].getBytes());
                    is.read(mensajex);
                    String smens = (new String(mensajex)).trim();
                    System.out.println(smens);
                }
                else{
                    mensajeS = nombre + ":" + mensajeS;
                    DatagramPacket paquete = new DatagramPacket(mensajeS.getBytes(), mensajeS.length(), grupo.getAddress(), puerto);
                    socketMulticast.send(paquete);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void runRecibir(Socket clientSocket){
        try {

            InputStream is = clientSocket.getInputStream();
            byte[] mensajex = new byte[1024];
            while (true) {
            is.read(mensajex);
            String smens = (new String(mensajex)).trim();
            System.out.println(smens);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}











