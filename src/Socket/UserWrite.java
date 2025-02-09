package Socket;
import Multicast.MultiCast;

import java.io.*;
import java.net.*;
import java.util.Random;

public class UserWrite extends Thread {
    public static String direccionIPGrupo = "225.10.10.10";
    public static int puerto = 6998;
    public static MulticastSocket socketMulticast = null;
    public static InetSocketAddress grupo = null;
    public static NetworkInterface netIf = null;
    public static String nombre = "multi";
    private static boolean ejecucion = true;
    public int tipoHilo = -1;
    public static final int enviar = 1;
    public static final int recibirPrivado = 2;
    public static final int recibir = 3;
    public UserWrite(int tipoHilo) {
        this.tipoHilo = tipoHilo;
    }


    public static void main(String[] args) {
        try {
            ejecucion = true;
            //Entramos al grupo multicast
            socketMulticast = new MulticastSocket(puerto);
            InetAddress dir = InetAddress.getByName(direccionIPGrupo);
            grupo = new InetSocketAddress(dir, puerto);
            netIf = NetworkInterface.getByInetAddress(dir);
            socketMulticast.joinGroup(grupo, netIf);
            //Ejecutamos para recibir, enviar y enviar mensajes privados
            UserWrite enviarC = new UserWrite(enviar);
            UserWrite recibirC = new UserWrite(recibirPrivado);
            UserWrite recibirM = new UserWrite(recibir);

            enviarC.start();
            recibirC.start();
            recibirM.start();

            enviarC.join();
            recibirC.join();
            recibirM.join();
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
            case recibirPrivado:
                recibirPrivado();
                break;

        }
    }
    private static void runEnviar(Socket newSocket) {
        try {
            OutputStream os = newSocket.getOutputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String mensaje;
        while (ejecucion) {
            try {
                mensaje = bufferedReader.readLine();
                String mensajeFinal[]= mensaje.split(":");
                if(mensaje.startsWith("Privado")){
                    os.write((mensajeFinal[1]).getBytes());
                }
                else {
                    mensaje = nombre + ": " + mensaje;
                    DatagramPacket paquete = new DatagramPacket(mensaje.getBytes(), mensaje.length(), grupo.getAddress(), puerto);
                    socketMulticast.send(paquete);
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void recibirPrivado(){
        try {
            System.out.println("Creando socket del servidor");
            // Crea un socket del servidor (ServerSocket) que escucha conexiones en el puerto 6668.
            ServerSocket serverSocket = new ServerSocket(6668);
            System.out.println("Acepta conexiones");
            // Bucle para aceptar múltiples conexiones
            while (true) {
                // El servidor se bloquea aquí hasta que un cliente se conecta.
                Socket newSocket = serverSocket.accept();
                System.out.println("Conexion recibida");
                // Crear un hilo para manejar cada cliente conectado
                new Thread(() -> handleClient(newSocket)).start();
                new Thread(() -> runEnviar(newSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
    private static void handleClient(Socket newSocket) {
        try {
            InputStream is = newSocket.getInputStream();
            OutputStream os = newSocket.getOutputStream();
            while (true) {
                byte[] mensaje = new byte[1024];
                is.read(mensaje);
                String smens = (new String(mensaje)).trim();
                String[] mensajeFinal = smens.split(" ");
                String numeroAleat;
                if (!smens.isEmpty()) {
                    System.out.println(smens);
                    if (mensajeFinal[0].equalsIgnoreCase("Conectar")) {
                        os.write(("Bienvenido " + mensajeFinal[1]).getBytes());
                    } else if (mensajeFinal[0].equalsIgnoreCase("leerftp")) {
                        Random r = new Random();
                        int x = r.nextInt(100);
                        numeroAleat = Integer.toString(x);
                        os.write(("Hay " + numeroAleat + " de archivos").getBytes());
                    } else if (smens.equalsIgnoreCase("fin")) {
                        os.write("Ha finalizado la conexion".getBytes());
                        break;
                    } else {
                        os.write("No conocemos ese mensaje".getBytes());
                    }
                }
            }
            newSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}



