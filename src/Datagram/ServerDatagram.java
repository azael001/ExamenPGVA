package Datagram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerDatagram extends Thread {
    private static DatagramSocket datagramSocket = null;
    public static String direccionIPGrupo = "225.10.10.10";
    public static int puerto = 6998;
    public static MulticastSocket socketMulticast = null;
    public static InetSocketAddress grupo = null;
    public static NetworkInterface netIf = null;
    private static ArrayList<InetAddress> arrayDir = new ArrayList<>();
    private static ArrayList<Integer> arrayPuertos = new ArrayList<>();
    private static ArrayList<String> arrayNombres = new ArrayList<>();
    public static final int privado = 3;
    public static final int enviar = 2;
    public static final int recibir = 1;
    public int tipoHilo = -1;
    private static boolean ejecucion = true;
    public static String nombre = "";


    private static void pedirNombre() {
        Scanner name = new Scanner(System.in);
        System.out.print("Escribe de nombre: ");
        nombre = name.nextLine();
    }
    public ServerDatagram(int tipoHilo) {
        this.tipoHilo = tipoHilo;
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
            ServerDatagram privadoC = new ServerDatagram(privado);
            ServerDatagram enviarC = new ServerDatagram(enviar);
            ServerDatagram recibirC = new ServerDatagram(recibir);
            enviarC.start();
            privadoC.start();
            recibirC.start();
            enviarC.join();
            privadoC.join();
            recibirC.join();

            socketMulticast.leaveGroup(grupo, netIf);
            socketMulticast.close();
            System.out.println("Socket cerrado");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void run() {

        switch (tipoHilo) {
            case enviar:
                runEnviar();
                break;
            case privado:
                privadoDatagram();
                break;
            case recibir:
                runRecibir();
                break;
        }
    }

    private void privadoDatagram() {
        try {
            datagramSocket = new DatagramSocket(7668);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        Thread escuchaMensajes = new Thread(this::escucharMensajes);
        escuchaMensajes.start();

    }

    private void escucharMensajes() {
            while (ejecucion) {
                try {
                    byte[] entrada = new byte[1024];
                    DatagramPacket datagrama1 = new DatagramPacket(entrada, entrada.length);
                    datagramSocket.receive(datagrama1);
                    String mensaje = new String(datagrama1.getData(), 0, datagrama1.getLength());
                    InetAddress dircliente = datagrama1.getAddress();
                    int puertocliente = datagrama1.getPort();
                    String[] msg = mensaje.split(":");

                    if (msg[0].equals("Nombre")) {
                        arrayDir.add(dircliente);
                        arrayPuertos.add(puertocliente);
                        arrayNombres.add(msg[1]);
                    } else {
                        System.out.println(mensaje);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Fin del hilo de escucha.");
        }
    private void runEnviar() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String mensaje;
        while (ejecucion) {
            try {
                System.out.println("Mensaje a enviar: ");
                mensaje = bufferedReader.readLine();
                String[] msg =  mensaje.split(":");

                if (mensaje.trim().equals("/")) {
                    ejecucion = false;
                    break;
                }
                if(msg[0].startsWith("Privado")){
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
    //Multicast
    private void runRecibir() {
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
    private static int posicionamientoNombre(String nombreUser) {
        for (int i = 0; i < arrayNombres.size(); i++) {
            if (arrayNombres.get(i).equals(nombreUser)) {
                return i;
            }
        }
        return 9999;
    }
}
