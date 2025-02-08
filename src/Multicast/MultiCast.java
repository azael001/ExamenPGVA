package Multicast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Scanner;

public class MultiCast extends Thread {
    public static String direccionIPGrupo = "225.10.10.10";
    public static int puerto = 6998;
    public static MulticastSocket socketMulticast = null;
    public static InetSocketAddress grupo = null;
    public static NetworkInterface netIf = null;
    public static String nombre = "";
    public static boolean ejecucion = true;
    public static final int enviar = 1;
    public static final int recibir = 2;
    public int tipoHilo = 0;

    private static void pedirNombre() {
        Scanner name = new Scanner(System.in);
        System.out.print("Escribe de nombre: ");
        nombre = name.nextLine();
    }

    public MultiCast(int tipoHilo) {
        this.tipoHilo = tipoHilo;
    }

    public static void main(String[] args) {
        try {
            pedirNombre();
            socketMulticast = new MulticastSocket(puerto);
            InetAddress dir = InetAddress.getByName(direccionIPGrupo);
            grupo = new InetSocketAddress(dir, puerto);
            netIf = NetworkInterface.getByInetAddress(dir);
            socketMulticast.joinGroup(grupo, netIf);
            MultiCast enviarC = new MultiCast(enviar);
            MultiCast recibirC = new MultiCast(recibir);
            enviarC.start();
            recibirC.start();
            try {
                enviarC.join();
                recibirC.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            socketMulticast.leaveGroup(grupo, netIf);
            socketMulticast.close();
            System.out.println("Socket cerrado");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

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

    private void runEnviar() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String mensaje;
        while (ejecucion) {
            try {
                System.out.println("Mensaje a enviar: ");
                mensaje = bufferedReader.readLine();
                mensaje = nombre + ":" + mensaje;
                DatagramPacket paquete = new DatagramPacket(mensaje.getBytes(), mensaje.length(), grupo.getAddress(), puerto);
                socketMulticast.send(paquete);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
        }
    }
}



