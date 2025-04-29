package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {




	private byte opcode;

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */
	
	// Atributos para crear los mensajes
	private byte[] fileHash; 
	private long fileOffset;
	private int chunkSize;
	private byte[] data;


	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}
	
	// Constructores para crear los mensajes
	
	// DownloadFile
	public PeerMessage(byte op, byte[] fileHash) {
		opcode = op;
		this.fileHash = fileHash;
	}
	// GetChunck
	public PeerMessage(byte op, long fileOffset, int chunkSize) {
		opcode = op;
		this.fileOffset = fileOffset;
		this.chunkSize = chunkSize;
	}
	
	// SendChunck
	public PeerMessage(byte op, byte[] data) {
		opcode = op;
		this.data = data;
	}

	/*
	 * DONE: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public byte getOpcode() {
		return opcode;
	}
	
	// Métdos getter para la obtención de los valores de los atributos de un mensaje
	public byte[] getFileHash() {
		if (opcode != PeerMessageOps.OPCODE_DOWNLOAD_FILE) throw new IllegalStateException("Invalid access to file Hash");
		return fileHash;
	}
	public long getFileOffset() {
		if (opcode != PeerMessageOps.OPCODE_GET_CHUNK) throw new IllegalStateException("Invalid access to file Offset");
		return fileOffset;
	}
	public int getChunckSize() {
		if (opcode != PeerMessageOps.OPCODE_GET_CHUNK) throw new IllegalStateException("Invalid access to chunk Size");
		return chunkSize;
	}
	public byte[] getData() {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) throw new IllegalStateException("Invalid access to data");
		return data;
	};
	

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		switch (opcode) {
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			case PeerMessageOps.OPCODE_TRANSFER_END:
				break;
				
			case PeerMessageOps.OPCODE_DOWNLOAD_FILE:
				long lengthHash = dis.readLong();
				if (lengthHash > Integer.MAX_VALUE) throw new IOException("Chunk too large");
				byte[] fileHash = new byte[(int) lengthHash];
				dis.readFully(fileHash);
				message.fileHash = fileHash;
				break;
				
			case PeerMessageOps.OPCODE_GET_CHUNK:
				message.fileOffset = dis.readLong();
				message.chunkSize = dis.readInt();
				break;
				
			case PeerMessageOps.OPCODE_SEND_CHUNK:
				long length = dis.readLong();
				if (length > Integer.MAX_VALUE) throw new IOException("Chunk too large");
				byte[] data = new byte[(int) length];
				dis.readFully(data);
				message.data = data;
				break;
				
				
		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * DONE (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			case PeerMessageOps.OPCODE_TRANSFER_END:
				break;
			
			case PeerMessageOps.OPCODE_DOWNLOAD_FILE:
				dos.writeLong(fileHash.length);
				dos.write(fileHash);
				break;
				
			case PeerMessageOps.OPCODE_GET_CHUNK:
				dos.writeLong(fileOffset);
				dos.writeInt(chunkSize);
				break;
				
			case PeerMessageOps.OPCODE_SEND_CHUNK:
				dos.writeLong(data.length);
				dos.write(data);
				break;

				
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}




}
