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
		socket = new Socket();
		socket.connect(fserverAddr);
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
		/*
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
		*/
	}
	
	public String getFileHash(String fileName) throws IOException {
		PeerMessage msgToPeer = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FILE, (short) fileName.length(), fileName.getBytes());
		msgToPeer.writeMessageToOutputStream(dos);
		
		PeerMessage msgFromPeer = PeerMessage.readMessageFromInputStream(dis);
		if(msgFromPeer.getOpcode() == PeerMessageOps.OPCODE_FILE_NOT_FOUND){
			System.err.println("File not found");
			return null;
		} else if(msgFromPeer.getOpcode() == PeerMessageOps.OPCODE_FILE_AMBIGUOUS){
			System.err.println("File ambiguous");
			return null;
		} else if(msgFromPeer.getOpcode() == PeerMessageOps.OPCODE_FILE_FOUNDED) {
			return new String(msgFromPeer.getFileName());
		} else {
			System.err.println("Unexpected error");
			return null;
		}
		
	}
	
	public long getFileSize() throws IOException {
		PeerMessage msgToPeer = new PeerMessage(PeerMessageOps.OPCODE_GET_FILE_SIZE);
		msgToPeer.writeMessageToOutputStream(dos);
		
		PeerMessage msgFromPeer = PeerMessage.readMessageFromInputStream(dis);
		if(msgFromPeer.getOpcode() == PeerMessageOps.OPCODE_FILE_SIZE) {
			return msgFromPeer.getFileSize();
		} else {
			System.err.println("getFileSize() dindt worked correctly");
			return 0;
		}
	}
	
	public byte[] getChunk(long offset, int chunkSize) throws IOException {
		PeerMessage msgToPeer = new PeerMessage(PeerMessageOps.OPCODE_GET_CHUNK, offset, chunkSize);
		msgToPeer.writeMessageToOutputStream(dos);
		
		PeerMessage msgFromPeer = PeerMessage.readMessageFromInputStream(dis);
		if(msgFromPeer.getOpcode() == PeerMessageOps.OPCODE_CHUNK_NOT_FOUND) {
			System.err.println("Requested chunk: not found");
			return null;
		}
		
		return msgFromPeer.getData();
	}
	
	public void transferEnd() throws IOException {
		PeerMessage msgToPeer = new PeerMessage(PeerMessageOps.OPCODE_TRANSFER_END);
		msgToPeer.writeMessageToOutputStream(dos);
		dis.close();
		dos.close();
		socket.close();
	}
	


	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
