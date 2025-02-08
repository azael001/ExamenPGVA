package SocketCualquier;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class UserWrite extends Thread {
    private static final int PUERTO = 6668;
    private static UserWrite[] clientes = new UserWrite[10]; // Array para almacenar clientes
    private static int numClientes = 0; // Contador de clientes conectados
    private Socket socket;
    private PrintWriter writer;
    private String nombreCliente;

    public UserWrite(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            // Hilo para escuchar entrada desde la consola del servidor
            Thread consoleInputThread = new Thread(UserWrite::leerConsola);
            consoleInputThread.start();

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Nueva conexión aceptada");
                // Crea un nuevo hilo para manejar el cliente
                new UserWrite(clienteSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Solicitar el nombre del cliente
            writer.println("Introduce tu nombre:");
            nombreCliente = reader.readLine().trim(); // Eliminamos espacios extra

            // Añadir cliente al array
            synchronized (clientes) {
                if (numClientes < clientes.length) {
                    clientes[numClientes] = this;
                    numClientes++;
                }
            }
            System.out.println(nombreCliente + " se ha conectado.");

            String mensaje;
            while ((mensaje = reader.readLine()) != null) {
                if (mensaje.equalsIgnoreCase("exit")) {
                    break;
                } else if (mensaje.startsWith("@")) {
                    enviarMensajePrivado(mensaje);
                } else {
                    enviarMensajePublico(nombreCliente + ": " + mensaje);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cerrarConexion();
        }
    }

    private void enviarMensajePublico(String mensaje) {
        synchronized (clientes) {
            for (int i = 0; i < numClientes; i++) {
                clientes[i].writer.println(mensaje);
            }
        }
    }

    private void enviarMensajePrivado(String mensaje) {
        int espacio = mensaje.indexOf(" ");
        if (espacio != -1) {
            String destinatario = mensaje.substring(1, espacio).trim(); // Aseguramos que no tenga espacios
            String mensajePrivado = "Mensaje del servidor a " + nombreCliente + " (privado): " + mensaje.substring(espacio + 1);

            boolean encontrado = false;
            synchronized (clientes) {
                for (int i = 0; i < numClientes; i++) {
                    if (clientes[i] != null && clientes[i].nombreCliente.equals(destinatario)) { // Comparación sin espacios
                        clientes[i].writer.println(mensajePrivado);
                        this.writer.println("Mensaje enviado a " + destinatario + ": " + mensajePrivado); // Confirmación al servidor
                        encontrado = true;
                        break;
                    }
                }
            }

            if (!encontrado) {
                this.writer.println("Usuario " + destinatario + " no encontrado.");
            }
        } else {
            this.writer.println("Formato incorrecto. Usa @nombre mensaje");
        }
    }

    private void cerrarConexion() {
        try {
            if (nombreCliente != null) {
                synchronized (clientes) {
                    for (int i = 0; i < numClientes; i++) {
                        if (clientes[i] != null && clientes[i].nombreCliente.equals(nombreCliente)) {
                            clientes[i] = null;
                            break;
                        }
                    }
                }
                System.out.println(nombreCliente + " se ha desconectado.");
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para leer la entrada desde la consola del servidor
    private static void leerConsola() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String comando = scanner.nextLine();
            if (comando.startsWith("@")) {
                // Enviar un mensaje privado a un cliente
                String destinatario = comando.substring(1, comando.indexOf(" "));
                String mensaje = comando.substring(comando.indexOf(" ") + 1);
                enviarMensajePrivadoConsola(destinatario, mensaje);
            } else if (comando.equalsIgnoreCase("exit")) {
                // Terminar el servidor
                System.out.println("Servidor cerrado.");
                break;
            }
        }
        scanner.close();
    }

    private static void enviarMensajePrivadoConsola(String destinatario, String mensaje) {
        synchronized (clientes) {
            for (int i = 0; i < numClientes; i++) {
                if (clientes[i] != null && clientes[i].nombreCliente.equals(destinatario)) {
                    clientes[i].writer.println("Mensaje del servidor: " + mensaje);
                    System.out.println("Mensaje enviado al cliente " + destinatario);
                    return;
                }
            }
            System.out.println("Usuario " + destinatario + " no encontrado.");
        }
    }
}
