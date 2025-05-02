package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.util.FileDigest;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	
	private DataInputStream dis;
	private DataOutputStream dos;
	
	
	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		/*
		 * DONE: (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */
		socket = new Socket(serverAddr.getHostName(), serverAddr.getPort());
		/*
		 * DONE: (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
	
	}

	public void test() {
		/*
		 * DONE: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 */
		try {
			// Enviar
			PeerMessage msgEnvia = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FILE, "gsjsks".getBytes());
			System.out.println("El mensaje generado tiene el codigo de operacion: " + msgEnvia.getOpcode());
			msgEnvia.writeMessageToOutputStream(dos);
			
			
			// Recibir
			PeerMessage msgRecibe = PeerMessage.readMessageFromInputStream(dis);
			if(msgRecibe.getOpcode() == PeerMessageOps.OPCODE_FILE_FOUNDED) {
				System.out.println("Comunicacion existosa");
			} else {
				System.err.println("Comunicacion fallida");
			}
			
		} catch (IOException ex) {
			System.out.println("Error en test(): " + ex.getMessage());
		}
	}
	
	


	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
