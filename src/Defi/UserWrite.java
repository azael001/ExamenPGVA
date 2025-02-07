package Defi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
public class UserWrite extends Thread {
    private static DatagramSocket datagramSocket = null;
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
    public static final int privado = 3;
    private static ArrayList<InetAddress> arrayDir = new ArrayList<>();
    private static ArrayList<Integer> arrayPuertos = new ArrayList<>();
    private static ArrayList<String> arrayNombres = new ArrayList<>();



    public UserWrite(int tipoHilo) {
        this.tipoHilo = tipoHilo;
    }

    private static void pedirNombre() {
        Scanner name = new Scanner(System.in);
        System.out.print("Escribe de nombre: ");
        nombre = name.nextLine();
    }

    public static void main(String[] args) {
        try {
            ejecucion = true;
            //Pedimos el nombre  del cuál será nuestro usuario
            pedirNombre();
            //Entramos al grupo multicast
            socketMulticast = new MulticastSocket(puerto);
            InetAddress dir = InetAddress.getByName(direccionIPGrupo);
            grupo = new InetSocketAddress(dir, puerto);
            netIf = NetworkInterface.getByInetAddress(dir);
            socketMulticast.joinGroup(grupo, netIf);
            //Ejecutamos para recibir, enviar y enviar mensajes privados
            UserWrite enviarC = new UserWrite(enviar);
            UserWrite recibirC = new UserWrite(recibir);
            UserWrite privadoC = new UserWrite(privado);

            enviarC.start();
            recibirC.start();
            privadoC.start();

            enviarC.join();
            recibirC.join();
            privadoC.join();
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
                runEnviar();
                break;

            case recibir:
                runRecibir();
                break;
            case privado:
                privadoDatagram();
                break;
        }
    }

    private void runRecibir() {
        final int tamanoBufferMensaje = 1000;
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

    private void runEnviar() {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String mensaje;
        while (ejecucion) {
            try {
                // no podemos tener 9999 usuarios conectados si no daría error
                System.out.println("Mensaje a enviar: ");
                mensaje = bufferedReader.readLine();
                String[] msg =  mensaje.split(":");


                if (mensaje.trim().equals("/")) {
                    ejecucion = false;
                    break;
                }
                if(msg[0].equals("Privado")){
                    int x = posicionamientoNombre(msg[1]);
                    if(x==9999){
                        System.out.println("Persona no encontrada");
                    }
                    else {
                        String d = msg[2];
                        byte[] salida = d.toString().getBytes();
                        DatagramPacket datagrama2 = new DatagramPacket(salida,
                                salida.length, arrayDir.get(x), arrayPuertos.get(x));
                        datagramSocket.send(datagrama2);
                    }
                }
                else if(mensaje.equalsIgnoreCase("Descargar")){
                    Random random = new Random();
                    int numeroAleatorio = random.nextInt(100) + 1;
                    String mensajeDescarga="Se han descargado " + numeroAleatorio + " Archivos";
                    DatagramPacket paquete = new DatagramPacket(mensajeDescarga.getBytes(), mensajeDescarga.length(), grupo.getAddress(), puerto);
                    socketMulticast.send(paquete);
                }
                else{
                    mensaje = nombre + ":" + mensaje;
                    DatagramPacket paquete = new DatagramPacket(mensaje.getBytes(), mensaje.length(), grupo.getAddress(), puerto);
                    socketMulticast.send(paquete);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void privadoDatagram() {
        try {
            datagramSocket = new DatagramSocket(7668);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (datagramSocket != null) {
            try {
                byte[] entrada = new byte[100];
                DatagramPacket datagrama1 = new DatagramPacket(entrada, entrada.length);
                datagramSocket.receive(datagrama1);
                String mensaje = new String(datagrama1.getData(), 0, datagrama1.getLength());
                InetAddress dircliente = datagrama1.getAddress();
                int puertocliente = datagrama1.getPort();
                String[] comrpobacion = mensaje.split(" ");
                String[] msg = mensaje.split(":");
                //El cliente solo puede enviar 2 mensajes por privado uno es el nombre y el otro confirmación de mensaje privado
                if(comrpobacion[0].equals("Ok")){
                    System.out.println(mensaje);
                }
                else {
                    //añadimos al array para buscar el puerto de cada cliente
                    arrayDir.add(dircliente);
                    arrayPuertos.add(puertocliente);
                    arrayNombres.add(msg[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Fin");
    }
    private static int posicionamientoNombre(String nombreUser){
        for(int i=0; i<arrayNombres.size(); i++){
            if(arrayNombres.get(i).equals(nombreUser)){
                return i;
            }
        }
        return 9999;
    }
}



