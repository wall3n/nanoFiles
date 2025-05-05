package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLSyntaxErrorException;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;




public class NFServer implements Runnable {

	public static final int PORT = 10000;
	private ServerSocket serverSocket = null;

	public NFServer() throws IOException {
		/*
		 * DONE: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		InetSocketAddress serverSocketAddr = new InetSocketAddress(PORT);
		
		/*
		 * DONE: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
		serverSocket = new ServerSocket();
		serverSocket.bind(serverSocketAddr);
		
	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out
					.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}
		try {
			while (true) {
				/*
				 * DONE: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
				 * otros peers que soliciten descargar ficheros.
				 */

				Socket clientSocket = serverSocket.accept();
				/*
				 * DONE: (Boletín SocketsTCP) Tras aceptar la conexión con un peer cliente, la
				 * comunicación con dicho cliente para servir los ficheros solicitados se debe
				 * implementar en el método serveFilesToClient, al cual hay que pasarle el
				 * socket devuelto por accept.
				 */
				serveFilesToClient(clientSocket);
				
				
				
			}
		} catch (IOException ex) {
			System.out.println("Error test(): " + ex.getMessage());
			ex.printStackTrace();
		}

	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * DONE: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */
		/*
		 * DONE: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		try {
			Socket clientSocket = serverSocket.accept();
			serveFilesToClient(clientSocket);
			
		} catch (IOException ex) {
			System.out.println("Server exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		
		/*
		 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
		 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
		 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
		 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
		 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
		 * más de un cliente conectado a este servidor.
		 */




	}
	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */




	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	public static void serveFilesToClient(Socket socket) {
		/*
		 * DONE: (Boletín SocketsTCP) Crear dis/dos a partir del socket
		 */
		System.out.println("ServeFilesToClient Executed");
		
		try {
			
			while(socket.isConnected()) {
				try {
					DataInputStream dis = new DataInputStream(socket.getInputStream());
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					
					PeerMessage msgRecibe = PeerMessage.readMessageFromInputStream(dis);
					
					FileInfo[] myFiles = NanoFiles.db.getFiles();
					FileInfo file = null;
					boolean fileValidated = false;
					
					switch(msgRecibe.getOpcode()) {
						case PeerMessageOps.OPCODE_DOWNLOAD_FILE: {
							System.out.println("File requested for download: " + new String(msgRecibe.getFileName()));
							FileInfo[] match = FileInfo.lookupFilenameSubstring(myFiles, new String(msgRecibe.getFileName()));
							if(match.length == 0) {
								// file_not_found
								System.err.println("File not found");
								PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
								msg.writeMessageToOutputStream(dos);
							}else if (match.length > 1){
								// file_ambiguous
								System.err.println("File ambiguous");
								PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_FILE_AMBIGUOUS);
								msg.writeMessageToOutputStream(dos);
							} else {
								System.out.println("File founded");
								file = match[0];
								fileValidated = true;
								PeerMessage msg = new PeerMessage(PeerMessageOps.OPCODE_FILE_FOUNDED, (long) file.fileHash.length(), file.fileHash.getBytes());
								msg.writeMessageToOutputStream(dos);
							}
							break;
						}
						
						case PeerMessageOps.OPCODE_GET_FILE_SIZE: {
							System.out.println("Requested file size");
							PeerMessage msgToClient = new PeerMessage();
							if(!fileValidated) {
								System.err.println("First you have to select a file");
								msgToClient.setOpcode(PeerMessageOps.OPCODE_INVALID_CODE);
								msgToClient.writeMessageToOutputStream(dos);
								break;
							}
							msgToClient.setOpcode(PeerMessageOps.OPCODE_FILE_SIZE);
							msgToClient.setFileSize(file.fileSize);
							msgToClient.writeMessageToOutputStream(dos);
							break;
						}
						
						case PeerMessageOps.OPCODE_GET_CHUNK: {
							System.out.println("Chunk requested");
							PeerMessage msgToClient = new PeerMessage();
							if(!fileValidated) {
								System.err.println("First you have to select a file");
								msgToClient.setOpcode(PeerMessageOps.OPCODE_INVALID_CODE);
								msgToClient.writeMessageToOutputStream(dos);
								break;
							}
							RandomAccessFile raf = new RandomAccessFile(file.filePath, "r");
							raf.seek(msgRecibe.getFileOffset());
							byte[] data = new byte[msgRecibe.getChunckSize()];
							raf.readFully(data);
							
							msgToClient.setOpcode(PeerMessageOps.OPCODE_SEND_CHUNK);
							msgToClient.setLength(msgRecibe.getChunckSize());
							msgToClient.setData(data);
							msgToClient.writeMessageToOutputStream(dos);
							break;
						}
						
						case PeerMessageOps.OPCODE_TRANSFER_END: {
							// TODO: Cerrar la conexion
							System.out.println("Client disconnected");
							break;
						}
					}
				} catch (EOFException eof) {
					System.out.println("Cliente se ha desconectado.");
					break;
				}
			}
			
		
		/*
		 * TODO: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
		 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
		 * tipo de mensaje recibido, enviando los correspondientes mensajes de
		 * respuesta.
		 */
			
		} catch (IOException ex) {
			System.out.println("Exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		/*
		 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
		 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
		 * compartidos. Los ficheros compartidos se pueden obtener con
		 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
		 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
		 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
		 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
		 * de su hash completo.
		 */

	}




}