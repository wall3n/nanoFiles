package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;

	/*
	 * DONE: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	
	private Map<InetSocketAddress, List<FileInfo>> peerFiles;
	private Map<String, Set<InetSocketAddress>> fileOwners;



	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * DONE: (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local,
		 */
		socket = new DatagramSocket(NFDirectoryServer.DIRECTORY_PORT);
		/*
		 * DONE: (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: ficheros, etc.)
		 */
		peerFiles = new HashMap<>();
		fileOwners = new HashMap<>();

		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public DatagramPacket receiveDatagram() throws IOException {
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		while (!datagramReceived) {
			
			/*
			 * DONE: (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient)
			 */
			
			byte[] recvBuf = new byte[DirMessage.PACKET_MAX_SIZE];
			datagramReceivedFromClient = new DatagramPacket(recvBuf, recvBuf.length);
			
			/*
			 * DONE: (Boletín SocketsUDP) Recibimos a través del socket un datagrama
			 */
			
			socket.receive(datagramReceivedFromClient);

			if (datagramReceivedFromClient == null) {
				System.err.println("[testMode] NFDirectoryServer.receiveDatagram: code not yet fully functional.\n"
						+ "Check that all TODOs have been correctly addressed!");
				System.exit(-1);
			} else {
				// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println(
							"Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
				} else {
					datagramReceived = true;
					System.out
							.println("Directory received datagram from " + datagramReceivedFromClient.getSocketAddress()
									+ " of size " + datagramReceivedFromClient.getLength() + " bytes.");
				}
			}

		}

		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {

		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		DatagramPacket rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);

		System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
		rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * DONE: (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración.
		 */

		/*
		 * DONE: (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta.
		 */

		/*
		 * DONE: (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied").
		 */

		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);
		
		InetSocketAddress clientAddr = (InetSocketAddress) pkt.getSocketAddress();
		byte[] dataToClient = null;
		
		if(messageFromClient.startsWith("ping&")){
			String protocolId = messageFromClient.substring(5);
			dataToClient = protocolId.equals(NanoFiles.PROTOCOL_ID) ? "welcome".getBytes() : "denied".getBytes();
		} else {
			if(messageFromClient.equals("ping")) {
				dataToClient = "pingok".getBytes();
			} else {
				dataToClient = "invalid".getBytes();
			}
		}
		
		
		
		DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
		socket.send(packetToClient);


	}

	public void run() throws IOException {

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio
			DatagramPacket rcvDatagram = receiveDatagram();

			sendResponse(rcvDatagram);

		}
	}

	private void sendResponse(DatagramPacket pkt) throws IOException {
		/*
		 * DONE: (Boletín MensajesASCII) Construir String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración. Después, usar la cadena para construir un objeto
		 * DirMessage que contenga en sus atributos los valores del mensaje. A partir de
		 * este objeto, se podrá obtener los valores de los campos del mensaje mediante
		 * métodos "getter" para procesar el mensaje y consultar/modificar el estado del
		 * servidor.
		 */
		InetSocketAddress clientAddr = (InetSocketAddress) pkt.getSocketAddress();
		String messageFromClientStr = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println(messageFromClientStr);
		
		DirMessage messageFromClient = DirMessage.fromString(messageFromClientStr);


		/*
		 * DONE: Una vez construido un objeto DirMessage con el contenido del datagrama
		 * recibido, obtener el tipo de operación solicitada por el mensaje y actuar en
		 * consecuencia, enviando uno u otro tipo de mensaje en respuesta.
		 */
		String operation = messageFromClient.getOperation(); // DONE: Cambiar!

		/*
		 * DONE: (Boletín MensajesASCII) Construir un objeto DirMessage (msgToSend) con
		 * la respuesta a enviar al cliente, en función del tipo de mensaje recibido,
		 * leyendo/modificando según sea necesario el "estado" guardado en el servidor
		 * de directorio (atributos files, etc.). Los atributos del objeto DirMessage
		 * contendrán los valores adecuados para los diferentes campos del mensaje a
		 * enviar como respuesta (operation, etc.)
		 */
		DirMessage messageToClient = new DirMessage(DirMessageOps.OPERATION_INVALID);




		switch (operation) {
		case DirMessageOps.OPERATION_PING: {
			/*
			 * DONE: (Boletín MensajesASCII) Comprobamos si el protocolId del mensaje del
			 * cliente coincide con el nuestro.
			 */
			/*
			 * DONE: (Boletín MensajesASCII) Construimos un mensaje de respuesta que indique
			 * el éxito/fracaso del ping (compatible, incompatible), y lo devolvemos como
			 * resultado del método.
			 */
			/*
			 * DONE: (Boletín MensajesASCII) Imprimimos por pantalla el resultado de
			 * procesar la petición recibida (éxito o fracaso) con los datos relevantes, a
			 * modo de depuración en el servidor
			 */
			
			if(messageFromClient.getProtocolId().equals(NanoFiles.PROTOCOL_ID)) {
				messageToClient.setOperation(DirMessageOps.OPERATION_PING_OK);
				System.out.println("Login succed!!");
			} else {
				messageToClient.setOperation(DirMessageOps.OPERATION_BAD_PROTOCOL);
				System.err.println("Login denied - Bad protocol");
			}
			
			break;
		}
		
		case DirMessageOps.OPERATION_SERVE: {
			System.out.println(messageFromClientStr);
			
			if(!messageFromClient.getProtocolId().equals(NanoFiles.PROTOCOL_ID)) {
				messageToClient.setOperation(DirMessageOps.OPERATION_BAD_PROTOCOL);
				System.err.println("Incorrect protocol");
				break;
			}
			
			// DONE: Guardar los archivos en las estructuras de datos
			String[] filenames = messageFromClient.getFilename().split("[,\\n]");
			String[] sizes = messageFromClient.getSize().split("[,\\n]");
			String[] hashes = messageFromClient.getSize().split("[,\\n]");
			List<FileInfo> newFiles = new ArrayList<FileInfo>();
		
			for(int i = 0; i < filenames.length; i++) {
				FileInfo addedFile = new FileInfo(hashes[i], filenames[i], Long.parseLong(sizes[i]), "");
				newFiles.addLast(addedFile);
			}
			
			// Cada vez que se registra un peer se borra la información previa que pudiese existir
			List<FileInfo> oldFiles = peerFiles.get(clientAddr);
			if(oldFiles != null) {
				for(FileInfo file : oldFiles) {
					String fileName = file.fileName;
					Set<InetSocketAddress> owners = fileOwners.get(fileName);
					if(owners != null) {
						owners.remove(clientAddr);
						if(owners.isEmpty()) {
							fileOwners.remove(fileName);
						}
					}
				}
			}
			
			peerFiles.put(clientAddr, new ArrayList<>(newFiles));
			
			for(FileInfo file : newFiles) {
				String fileName = file.fileName;
				fileOwners
					.computeIfAbsent(fileName, k -> new HashSet<>())
					.add(clientAddr);
			}
			
			messageToClient.setOperation(DirMessageOps.OPERATION_SERVE_OK);
			
			break;
		}
		
		case DirMessageOps.OPERATION_FILELIST_REQUEST: {
			if(!messageFromClient.getProtocolId().equals(NanoFiles.PROTOCOL_ID)) {
				messageToClient.setOperation(DirMessageOps.OPERATION_BAD_PROTOCOL);
				System.err.println("Incorrect protocol");
				break;
			}
			
			messageToClient.setOperation(DirMessageOps.OPERATION_FILELIST_OK);
			
			// TODO: Reccorer las estructuras de datos para generar el mensaje de respuesta
			
			break;
		}
		
		case DirMessageOps.OPERATION_DOWNLOAD_REQUEST: {
			
			// TODO: Recorrer las estructuras de datos para devolver los peers que son poseedores del fichero
			
			break;
		}



		default:
			System.err.println("Unexpected message operation: \"" + operation + "\"");
			System.exit(-1);
		}

		/*
		 * DONE: (Boletín MensajesASCII) Convertir a String el objeto DirMessage
		 * (msgToSend) con el mensaje de respuesta a enviar, extraer los bytes en que se
		 * codifica el string y finalmente enviarlos en un datagrama
		 */
		byte[] dataToSend = messageToClient.toString().getBytes();
		
		DatagramPacket packetToClient = new DatagramPacket(dataToSend, dataToSend.length, clientAddr);
		socket.send(packetToClient);

	}
}
