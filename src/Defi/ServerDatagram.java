package Defi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class ServerDatagram extends Thread {
    private static DatagramSocket datagramSocket = null;
    private static ArrayList<InetAddress> arrayDir = new ArrayList<>();
    private static ArrayList<Integer> arrayPuertos = new ArrayList<>();
    private static ArrayList<String> arrayNombres = new ArrayList<>();
    public static final int privado = 3;
    public static final int enviar = 2;
    public int tipoHilo = -1;

    private static boolean ejecucion = true;
    public ServerDatagram(int tipoHilo) {
        this.tipoHilo = tipoHilo;
    }
    public static void main(String[] args) {
        try {
            ejecucion = true;
            ServerDatagram privadoC = new ServerDatagram(privado);
            ServerDatagram enviarC = new ServerDatagram(enviar);
            enviarC.start();
            privadoC.start();
            enviarC.join();
            privadoC.join();
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

        System.out.println("Servidor UDP en escucha...");
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
