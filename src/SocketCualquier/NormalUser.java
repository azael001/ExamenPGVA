package SocketCualquier;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class NormalUser extends Thread {
    private static final int PUERTO = 6668;
    private static Socket clientSocket;
    private static PrintWriter writer;
    private static BufferedReader reader;
    private static String nombreCliente;

    public NormalUser() {
    }

    public static void main(String[] args) {
        try {
            pedirNombre();
            clientSocket = new Socket("localhost", PUERTO);
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Enviar el nombre al servidor
            writer.println(nombreCliente);

            // Crear un hilo para recibir mensajes
            new NormalUser().start();

            // Crear un hilo para enviar mensajes
            Scanner scanner = new Scanner(System.in);
            String mensaje;
            while (true) {
                mensaje = scanner.nextLine();
                if (mensaje.equals("exit")) {
                    writer.println("exit");
                    break;
                } else {
                    writer.println(mensaje);
                }
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void pedirNombre() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Escribe tu nombre: ");
        nombreCliente = scanner.nextLine();
    }

    @Override
    public void run() {
        try {
            String mensaje;
            while ((mensaje = reader.readLine()) != null) {
                System.out.println(mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
