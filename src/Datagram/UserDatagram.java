package Datagram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Scanner;

public class UserDatagram extends Thread {
    public static String direccionIPGrupo = "225.10.10.10";
    public static int puerto = 6998;
    public static MulticastSocket socketMulticast = null;
    public static InetSocketAddress grupo = null;
    public static NetworkInterface netIf = null;
    public static String nombre = "";
    private static boolean ejecucion = true;
    public int tipoHilo = -1;
    public static final int ENVIAR = 1;
    public static final int RECIBIR = 2;
    public static final int PRIVADO = 3;
    public UserDatagram(int tipoHilo) {
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
            try {
                socketMulticast = new MulticastSocket(puerto);
                InetAddress dir = InetAddress.getByName(direccionIPGrupo);
                grupo = new InetSocketAddress(dir, puerto);
                netIf = NetworkInterface.getByInetAddress(dir);
                socketMulticast.joinGroup(grupo, netIf);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            UserDatagram enviarC = new UserDatagram(ENVIAR);
            UserDatagram privadoC = new UserDatagram(PRIVADO);
            UserDatagram recibirC = new UserDatagram(RECIBIR);

            enviarC.start();
            privadoC.start();
            recibirC.start();

            enviarC.join();
            privadoC.join();
            recibirC.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        switch (tipoHilo) {
            case PRIVADO:
                recibirMensajePrivado();
                break;
            case ENVIAR:
                enviarMensajePrivado();
                break;
            case RECIBIR:
                runRecibir();
                break;
        }
    }

    private void recibirMensajePrivado() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            InetAddress dirservidor = InetAddress.getByName("192.168.56.1");

            // Enviar nombre al servidor
            String mensaje = "Nombre:" + nombre;
            DatagramPacket datagrama1 = new DatagramPacket(mensaje.getBytes(),
                    mensaje.getBytes().length, dirservidor, 7668);
            datagramSocket.send(datagrama1);

            while (ejecucion) {
                byte[] respuesta = new byte[100];
                DatagramPacket datagrama2 = new DatagramPacket(respuesta, respuesta.length);
                datagramSocket.receive(datagrama2);
                String recibido = new String(respuesta, 0, datagrama2.getLength());
                System.out.println("Mensaje privado: " + recibido);

                if (recibido.equalsIgnoreCase("end")) {
                    ejecucion = false;
                    break;
                }

                // Confirmación de recepción al servidor
                String mensaje2 = "Ok recibido mensaje privado";
                DatagramPacket datagrama3 = new DatagramPacket(mensaje2.getBytes(),
                        mensaje2.getBytes().length, dirservidor, 7668);
                datagramSocket.send(datagrama3);
            }
            datagramSocket.close();
            System.out.println("El cliente terminó.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensajePrivado() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            InetAddress dirservidor = InetAddress.getByName("192.168.56.1");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String mensaje;

            while (ejecucion) {
                mensaje = bufferedReader.readLine();
                String[] msg =  mensaje.split(":");
                if(mensaje.startsWith("Privado")) {
                    DatagramPacket datagrama4 = new DatagramPacket(msg[1].getBytes(),
                            msg[1].getBytes().length, dirservidor, 7668);
                    datagramSocket.send(datagrama4);
                }
                else{
                    mensaje = nombre + ":" + mensaje;
                    DatagramPacket paquete = new DatagramPacket(mensaje.getBytes(), mensaje.length(), grupo.getAddress(), puerto);
                    socketMulticast.send(paquete);
                }

            }
            datagramSocket.close();
            System.out.println("Finalizando envío de mensajes.");

        } catch (IOException e) {
            e.printStackTrace();
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
}
