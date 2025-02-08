package Socket;

import java.io.*;
import java.net.*;
import java.util.Random;

public class UserWrite extends Thread {
    private static boolean ejecucion = true;
    private static final int PUERTO = 6668;
    private Socket socket;

    public UserWrite(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            while (ejecucion) {
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
        try (InputStream is = socket.getInputStream();
             OutputStream os = socket.getOutputStream()) {

            byte[] mensaje = new byte[1024];
            while (true) {
                int bytesRead = is.read(mensaje);
                String smens = new String(mensaje, 0, bytesRead).trim();
                   if(smens.equals("exit")){
                       break;
                   }
                   else if(smens.equals("write")){
                       os.write("lala".getBytes());
                   }
                   else {
                       System.out.println(smens);
                   }

            }
            socket.close();
            System.out.println("Cliente desconectado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
