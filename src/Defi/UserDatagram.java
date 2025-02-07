package Defi;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UserDatagram extends Thread {
    public static String nombre = "";
    private static boolean ejecucion = true;
    public int tipoHilo = -1;
    public static final int ENVIAR = 1;
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
            UserDatagram enviarC = new UserDatagram(ENVIAR);
            UserDatagram privadoC = new UserDatagram(PRIVADO);

            enviarC.start();
            privadoC.start();

            enviarC.join();
            privadoC.join();
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
            Scanner scanner = new Scanner(System.in);

            while (ejecucion) {
                String mensaje = scanner.nextLine();
                DatagramPacket datagrama4 = new DatagramPacket(mensaje.getBytes(),
                        mensaje.getBytes().length, dirservidor, 7668);
                datagramSocket.send(datagrama4);
            }
            datagramSocket.close();
            System.out.println("Finalizando envío de mensajes.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
