package es.um.redes.nanoFiles.udp.message;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea
	private static final String ITEM_SEPARATOR = ","; //Definimos el separador para listas

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	/*
	 * DONE: (Boletín MensajesASCII) Definir de manera simbólica los nombres de
	 * todos los campos que pueden aparecer en los mensajes de este protocolo
	 * (formato campo:valor)
	 */
	private static final String FIELDNAME_PROTOCOLID = "protocolid";
	private static final String FIELDNAME_FILENAME = "filename";
	private static final String FIELDNAME_SIZE = "size";
	private static final String FIELDNAME_HASH = "hash";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_PEER = "peer";


	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	
	/**
	 * Identificador de protocolo usado, para comprobar compatibilidad del directorio.
	 */
	private String protocolId;
	/*
	 * DONE: (Boletín MensajesASCII) Crear un atributo correspondiente a cada uno de
	 * los campos de los diferentes mensajes de este protocolo.
	 */
	private String filename;
	private String size;
	private String hash;
	private String port;
	private String peer;




	public DirMessage(String op) {
		operation = op;
	}

	/*
	 * DONE: (Boletín MensajesASCII) Crear diferentes constructores adecuados para
	 * construir mensajes de diferentes tipos con sus correspondientes argumentos
	 * (campos del mensaje)
	 */
	
	// Constructor filelist_ok
	public DirMessage(String op, String[] files, String[] sizes, String[] hashes) {
		operation = op;
		filename = String.join(DirMessage.ITEM_SEPARATOR, files);
		size = String.join(DirMessage.ITEM_SEPARATOR, sizes);
		hash = String.join(DirMessage.ITEM_SEPARATOR, hashes);
	}
	
	// Constructor serve
	public DirMessage(String op, String protocolId, String port, String[] filenames, String[] sizes, String[] hashes) {
		operation = op;
		this.protocolId = protocolId;
		this.port = port;
		filename = String.join(DirMessage.ITEM_SEPARATOR, filenames);
		size = String.join(DirMessage.ITEM_SEPARATOR, sizes);
		hash = String.join(DirMessage.ITEM_SEPARATOR, hashes);
	}
	
	// Constructor download_request
	public DirMessage(String op, String protocolId, String filename) {
		operation = op;
		this.protocolId = protocolId;
		this.filename = filename;
	}
	
	// Constructor download_ok
	public DirMessage(String op, String filename, String hash, String[] peers, String[] ports) {
		operation = op;
		this.filename = filename;
		this.hash = hash;
		peer = String.join(DirMessage.ITEM_SEPARATOR, peers);
		port = String.join(ITEM_SEPARATOR, ports);
	}
	

	public String getOperation() {
		return operation;
	}

	/*
	 * DONE: (Boletín MensajesASCII) Crear métodos getter y setter para obtener los
	 * valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	
	// Creamos estructuras para que ver que conjunto de operaciones pueden acceder a los métodos de cada atributo
	private static final Set<String> protocolIdAllowedOp = new HashSet<String>(Arrays.asList(DirMessageOps.OPERATION_PING, DirMessageOps.OPERATION_FILELIST_REQUEST, DirMessageOps.OPERATION_SERVE, DirMessageOps.OPERATION_DOWNLOAD_REQUEST));
	private static final Set<String> filenameAllowedOp = new HashSet<String>(Arrays.asList(DirMessageOps.OPERATION_FILELIST_OK, DirMessageOps.OPERATION_SERVE, DirMessageOps.OPERATION_DOWNLOAD_REQUEST, DirMessageOps.OPERATION_DOWNLOAD_OK));
	private static final Set<String> sizeAllowedOp = new HashSet<String>(Arrays.asList(DirMessageOps.OPERATION_FILELIST_OK, DirMessageOps.OPERATION_SERVE));
	private static final Set<String> hashAllowedOp = new HashSet<String>(Arrays.asList(DirMessageOps.OPERATION_FILELIST_OK, DirMessageOps.OPERATION_SERVE, DirMessageOps.OPERATION_DOWNLOAD_OK));
	private static final Set<String> portAllowedOp = new HashSet<String>(Arrays.asList(DirMessageOps.OPERATION_SERVE, DirMessageOps.OPERATION_DOWNLOAD_OK));
	
	public void setProtocolID(String protocolIdent) {
		if (!DirMessage.protocolIdAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		protocolId = protocolIdent;
	}

	public String getProtocolId() {
		if (!DirMessage.protocolIdAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: getProtocolId called for message of unexpected type (" + operation + ")");
		}

		return protocolId;
	}
	
	public void setFilename(String filename) {
		if(!DirMessage.filenameAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: getFilename called for message of unexpected type (" + operation + ")"
			);
		}
		
		this.filename = filename;
	}
	
	public String getFilename() {
		if(!DirMessage.filenameAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: getFilename called for message of unexpected type (" + operation + ")"
			);
		}
		
		return filename;
	}
	
	public void setSize(String size) {
		if(!DirMessage.sizeAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: getSize called for message of unexpected type (" + operation + ")"
			);
		}
		
		this.size = size;
	}
	
	public String getSize() {
		if(!DirMessage.sizeAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: getSize called for message of unexpected type (" + operation + ")"
			);
		}
		
		return size;
	}
	
	public void setHash(String hash) {
		if(!DirMessage.hashAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: getHash called for message of unexpected type (" + operation + ")"
			);
		}
		
		this.hash = hash;
	}
	
	public String getHash() {
		if(!DirMessage.hashAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: getHash called for message of unexpected type (" + operation + ")"
			);
		}
		
		return hash;
	}
	
	public void setPort(String port) {
		if(!DirMessage.portAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: getPort called for message of unexpected type (" + operation + ")"
			);
		}
		
		this.port = port;
	}
	
	public String getPort() {
		if(!DirMessage.portAllowedOp.contains(operation)) {
			throw new RuntimeException(
					"DirMessage: getPort called for message of unexpected type (" + operation + ")"
			);
		}
		
		return port;
	}
	
	public void setPeer(String peer) {
		if(!operation.equals(DirMessageOps.OPERATION_DOWNLOAD_OK)) {
			throw new RuntimeException(
					"DirMessage: getPeer called for message of unexpected type (" + operation + ")"
			);
		}
		
		this.peer = peer;
	}
	
	public String getPeer() {
		if(!operation.equals(DirMessageOps.OPERATION_DOWNLOAD_OK)) {
			throw new RuntimeException(
					"DirMessage: getPeer called for message of unexpected type (" + operation + ")"
			);
		}
		
		return peer;
	}

	
	
	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * DONE: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */

		// Código de depuración
		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		
		
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;



		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			
			case FIELDNAME_PROTOCOLID: {
				if(m == null) {
					System.err.println("WRONG FORMAT: Operation field must exist always before any atribute");
					System.exit(-1);
				}
				m.setProtocolID(value);
				break;
			}
			
			case FIELDNAME_FILENAME: {
				if(m == null) {
					System.err.println("WRONG FORMAT: Operation field must exist always before any atribute");
					System.exit(-1);
				}
				m.setFilename(value);
				break;
			}
			
			case FIELDNAME_SIZE: {
				if(m == null) {
					System.err.println("WRONG FORMAT: Operation field must exist always before any atribute");
					System.exit(-1);
				}
				m.setSize(value);
				break;
			}
			case FIELDNAME_HASH: {
				if(m == null) {
					System.err.println("WRONG FORMAT: Operation field must exist always before any atribute");
					System.exit(-1);
				}
				m.setHash(value);
				break;
			}
			case FIELDNAME_PORT: {
				if(m == null) {
					System.err.println("WRONG FORMAT: Operation field must exist always before any atribute");
					System.exit(-1);
				}
				m.setPort(value);
				break;
			}
			case FIELDNAME_PEER: {
				if(m == null) {
					System.err.println("WRONG FORMAT: Operation field must exist always before any atribute");
					System.exit(-1);
				}
				m.setPeer(value);
				break;
			}

			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}




		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: (Boletín MensajesASCII) En función de la operación del mensaje, crear
		 * una cadena la operación y concatenar el resto de campos necesarios usando los
		 * valores de los atributos del objeto.
		 */
		switch(operation) {
		case DirMessageOps.OPERATION_PING: {
			
			sb.append(FIELDNAME_PROTOCOLID + DELIMITER + protocolId + END_LINE);
			break;
		}
		
		}


		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}

}
