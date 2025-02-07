package Defi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Scanner;
public class NormalUser extends Thread {
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



    public NormalUser(int tipoHilo) {
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
            pedirNombre();

            socketMulticast = new MulticastSocket(puerto);
            InetAddress dir = InetAddress.getByName(direccionIPGrupo);
            grupo = new InetSocketAddress(dir, puerto);
            netIf = NetworkInterface.getByInetAddress(dir);
            socketMulticast.joinGroup(grupo, netIf);

            NormalUser enviarC = new NormalUser(enviar);
            NormalUser recibirC = new NormalUser(recibir);
            NormalUser privadoC = new NormalUser(privado);

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
                mensajePrivado();
                break;

            case recibir:
                runRecibir();
                break;

            case privado:
                mensajePrivado();
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
        String mensaje;
            try {
                mensaje = "Se ha conectado " + nombre;
                DatagramPacket paquete = new DatagramPacket(mensaje.getBytes(), mensaje.length(), grupo.getAddress(), puerto);
                socketMulticast.send(paquete);
        } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
    private void mensajePrivado(){
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            InetAddress dirservidor = InetAddress.getByName("192.168.56.1");

            String mensaje = new String("Nombre:"+nombre);
            DatagramPacket datagrama1 = new DatagramPacket(mensaje.getBytes(),
                    mensaje.getBytes().length, dirservidor, 7668);
            datagramSocket.send(datagrama1);

            while(true){
                byte[] respuesta = new byte[100];
                DatagramPacket datagrama2 = new DatagramPacket(respuesta,
                        respuesta.length);
                datagramSocket.receive(datagrama2);
                System.out.println("Mensaje privado: " + new String(respuesta,0,datagrama2.getLength()));

                String mensaje2= "Ok recibido mensaje privado";
                DatagramPacket datagrama3 = new DatagramPacket(mensaje2.getBytes(),
                        mensaje2.getBytes().length, dirservidor, 7668);
                datagramSocket.send(datagrama3);
                if(respuesta.equals("end")){
                    break;
                }
            }
            datagramSocket.close();
            System.out.println("el cliente termino");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

