package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;
	/**
	 * Nombre/IP del host donde se ejecuta el directorio
	 */
	private String directoryHostname;





	public DirectoryConnector(String hostname) throws IOException {
		// Guardamos el string con el nombre/IP del host
		directoryHostname = hostname;
		/*
		 * (Boletín SocketsUDP) Convertir el string 'hostname' a InetAddress y
		 * guardar la dirección de socket (address:DIRECTORY_PORT) del directorio en el
		 * atributo directoryAddress, para poder enviar datagramas a dicho destino.
		 */
		InetAddress serverIp = InetAddress.getByName(directoryHostname);
		directoryAddress = new InetSocketAddress(serverIp, DirectoryConnector.DIRECTORY_PORT);
		
		/*
		 * (Boletín SocketsUDP) Crea el socket UDP en cualquier puerto para enviar
		 * datagramas al directorio
		 */
		socket = new DatagramSocket();


	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 * 
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/*
		 * TODO: (Boletín SocketsUDP) Enviar datos en un datagrama al directorio y
		 * recibir una respuesta. El array devuelto debe contener únicamente los datos
		 * recibidos, *NO* el búfer de recepción al completo.
		 */
		/*
		 * TODO: (Boletín SocketsUDP) Una vez el envío y recepción asumiendo un canal
		 * confiable (sin pérdidas) esté terminado y probado, debe implementarse un
		 * mecanismo de retransmisión usando temporizador, en caso de que no se reciba
		 * respuesta en el plazo de TIMEOUT. En caso de salte el timeout, se debe volver
		 * a enviar el datagrama y tratar de recibir respuestas, reintentando como
		 * máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones.
		 */
		/*
		 * TODO: (Boletín SocketsUDP) Las excepciones que puedan lanzarse al
		 * leer/escribir en el socket deben ser capturadas y tratadas en este método. Si
		 * se produce una excepción de entrada/salida (error del que no es posible
		 * recuperarse), se debe informar y terminar el programa.
		 */
		/*
		 * NOTA: Las excepciones deben tratarse de la más concreta a la más genérica.
		 * SocketTimeoutException es más concreta que IOException.
		 */
		
		int attempts = 0;
		boolean success = false;
		while(attempts < MAX_NUMBER_OF_ATTEMPTS && !success) {
			try {
				// Envio de datos al directorio
				DatagramPacket packetToDir = new DatagramPacket(requestData, requestData.length, directoryAddress);
				socket.send(packetToDir);
				
				// Recibir respuesta
				DatagramPacket packetFromDir = new DatagramPacket(responseData, responseData.length);
				socket.setSoTimeout(DirectoryConnector.TIMEOUT);
				socket.receive(packetFromDir);
				
				response = Arrays.copyOfRange(packetFromDir.getData(), 0, packetFromDir.getLength());
				
				if (response != null && response.length == responseData.length) {
					System.err.println("Your response is as large as the datagram reception buffer!!\n"
							+ "You must extract from the buffer only the bytes that belong to the datagram!");
				} else {
					success = true;
				}
				
			} catch (SocketTimeoutException e) {
				attempts++;
				System.err.println("Tiemout. Retrying... Attempt: " + attempts);
			} catch (IOException e) {
				System.err.println("I/O exception. Terminating program.");
				System.exit(-1);
			}
		}

		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 */
	public boolean testSendAndReceive() {
		/*
		 * TODO: (Boletín SocketsUDP) Probar el correcto funcionamiento de
		 * sendAndReceiveDatagrams. Se debe enviar un datagrama con la cadena "ping" y
		 * comprobar que la respuesta recibida empieza por "pingok". En tal caso,
		 * devuelve verdadero, falso si la respuesta no contiene los datos esperados.
		 */
		boolean success = false;
		
		byte[] msg = "ping".getBytes();
		
		byte[] res = sendAndReceiveDatagrams(msg);
		
		String sRes = new String(res, 0, res.length);
		
		if(sRes.startsWith("pingok")) {
			success = true;
		}

		return success;
	}

	public String getDirectoryHostname() {
		return directoryHostname;
	}

	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que
	 * usa un protocolo compatible. Este método no usa mensajes bien formados.
	 * 
	 * @return Verdadero si
	 */
	public boolean pingDirectoryRaw() {
		boolean success = false;
		/*
		 * TODO: (Boletín EstructuraNanoFiles) Basándose en el código de
		 * "testSendAndReceive", contactar con el directorio, enviándole nuestro
		 * PROTOCOL_ID (ver clase NanoFiles). Se deben usar mensajes "en crudo" (sin un
		 * formato bien definido) para la comunicación.
		 * 
		 * PASOS: 1.Crear el mensaje a enviar (String "ping&protocolId"). 2.Crear un
		 * datagrama con los bytes en que se codifica la cadena : 4.Enviar datagrama y
		 * recibir una respuesta (sendAndReceiveDatagrams). : 5. Comprobar si la cadena
		 * recibida en el datagrama de respuesta es "welcome", imprimir si éxito o
		 * fracaso. 6.Devolver éxito/fracaso de la operación.
		 */
		
		byte[] messageToSend = ("ping&" + NanoFiles.PROTOCOL_ID).getBytes();
		
		byte[] response = this.sendAndReceiveDatagrams(messageToSend);
		
		String sRes = new String(response , 0, response.length);
		
		if(sRes.equals("welcome")) {
			success = true;
		}
		
		System.out.println("The result of the pingDirectoryRaw test is:" + success);

		return success;
	}

	/**
	 * Método para "hacer ping" al directorio, comprobar que está operativo y que es
	 * compatible.
	 * 
	 * @return Verdadero si el directorio está operativo y es compatible
	 */
	public boolean pingDirectory() {
		boolean success = false;
		/*
		 * TODO: (Boletín MensajesASCII) Hacer ping al directorio 1.Crear el mensaje a
		 * enviar (objeto DirMessage) con atributos adecuados (operation, etc.) NOTA:
		 * Usar como operaciones las constantes definidas en la clase DirMessageOps :
		 * 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		 * 3.Crear un datagrama con los bytes en que se codifica la cadena : 4.Enviar
		 * datagrama y recibir una respuesta (sendAndReceiveDatagrams). : 5.Convertir
		 * respuesta recibida en un objeto DirMessage (método DirMessage.fromString)
		 * 6.Extraer datos del objeto DirMessage y procesarlos 7.Devolver éxito/fracaso
		 * de la operación
		 */
		DirMessage messageToDir = new DirMessage(DirMessageOps.OPERATION_PING);
		messageToDir.setProtocolID(NanoFiles.PROTOCOL_ID);
		byte[] byteMessageToDir = messageToDir.toString().getBytes();
		
		byte[] response = this.sendAndReceiveDatagrams(byteMessageToDir);
		
		String sRes = new String(response, 0, response.length);
		
		DirMessage messageFromDir = DirMessage.fromString(sRes);
		
		if(messageFromDir.getOperation().equals(DirMessageOps.OPERATION_PING_OK)) {
			success = true;
		} else {
			System.err.println("Ping failed");
		}
		
		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado y
	 * publicar los ficheros que este peer servidor está sirviendo.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @param files      La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean registerFileServer(int serverPort, FileInfo[] files) {
		boolean success = false;

		// TODO: Ver TODOs en pingDirectory y seguir esquema similar
		/*
		 * TODO: (Boletín MensajesASCII) Hacer ping al directorio 1.Crear el mensaje a
		 * enviar (objeto DirMessage) con atributos adecuados (operation, etc.) NOTA:
		 * Usar como operaciones las constantes definidas en la clase DirMessageOps :
		 * 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		 * 3.Crear un datagrama con los bytes en que se codifica la cadena : 4.Enviar
		 * datagrama y recibir una respuesta (sendAndReceiveDatagrams). : 5.Convertir
		 * respuesta recibida en un objeto DirMessage (método DirMessage.fromString)
		 * 6.Extraer datos del objeto DirMessage y procesarlos 7.Devolver éxito/fracaso
		 * de la operación
		 */
		
		DirMessage messageToDir = new DirMessage(DirMessageOps.OPERATION_SERVE);
		messageToDir.setProtocolID(NanoFiles.PROTOCOL_ID);
		messageToDir.setPort(Integer.toString(serverPort));
		
		String[] filenames = new String[files.length];
		String[] sizes = new String[files.length];
		String[] hashes = new String[files.length];
		
		int i = 0;
		for(FileInfo file : files) {
			filenames[i] = file.fileName;
			sizes[i] = Long.toString(file.fileSize);
			hashes[i++] = file.fileHash;
		}
		
		messageToDir.setFilename(String.join(",", filenames));
		messageToDir.setSize(String.join(",", sizes));
		messageToDir.setHash(String.join(",", hashes));
		
		byte[] byteMessageToDir = messageToDir.toString().getBytes();
		
		byte[] response = this.sendAndReceiveDatagrams(byteMessageToDir);
		
		String sRes = new String(response, 0, response.length);
		
		DirMessage messageFromDir = DirMessage.fromString(sRes);
		
		if(messageFromDir.getOperation().equals(DirMessageOps.OPERATION_SERVE_OK)) {
			success = true;
			System.out.println("Directory registered!");
		} else {
			System.err.println("Serve failed");
		}
		
		return success;
		
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public FileInfo[] getFileList() {
		FileInfo[] filelist = new FileInfo[0];
		// TODO: Ver TODOs en pingDirectory y seguir esquema similar



		return filelist;
	}

	/**
	 * Método para obtener la lista de servidores que tienen un fichero cuyo nombre
	 * contenga la subcadena dada.
	 * 
	 * @filenameSubstring Subcadena del nombre del fichero a buscar
	 * 
	 * @return La lista de direcciones de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public InetSocketAddress[] getServersSharingThisFile(String filenameSubstring) {
		// TODO: Ver TODOs en pingDirectory y seguir esquema similar
		InetSocketAddress[] serversList = new InetSocketAddress[0];
		return serversList;
	}

	/**
	 * Método para darse de baja como servidor de ficheros.
	 * 
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y ha dado de baja sus ficheros.
	 */
	public boolean unregisterFileServer() {
		boolean success = false;




		return success;
	}




}
