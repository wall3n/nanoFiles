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
	 * DONE: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */
	
	// Atributos para crear los mensajes
	private short lengthName;
	private byte[] fileName;
	
	private long lengthHash;
	private byte[] fileHash;
	
	private long fileOffset;
	private int chunkSize;
	
	private long length;
	private byte[] data;

	private long fileSize;

	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}
	
	// Constructores para crear los mensajes
	
	public PeerMessage(byte op, long campo1) {
		opcode = op;
		fileSize = campo1;
	}
	
	public PeerMessage(byte op, short campo1, byte[] campo2) {
		opcode = op;
		lengthName = campo1;
		fileName = campo2;
	}
	
	public PeerMessage(byte op, long campo1, byte[] campo2) {
		opcode = op;
		if(op == PeerMessageOps.OPCODE_FILE_FOUNDED) {
			lengthHash = campo1;
			fileHash = campo2;
		} else if(op == PeerMessageOps.OPCODE_SEND_CHUNK) {
			length = campo1;
			data = campo2;
		}
	}
	// GetChunck
	public PeerMessage(byte op, long fileOffset, int chunkSize) {
		opcode = op;
		this.fileOffset = fileOffset;
		this.chunkSize = chunkSize;
	}
	

	/*
	 * DONE: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	
	public void setOpcode(byte op) {
		opcode = op;
	}
	
	public byte getOpcode() {
		return opcode;
	}
	
	// Métdos getter para la obtención de los valores de los atributos de un mensaje
	public short getLengthName() {
		if (opcode != PeerMessageOps.OPCODE_DOWNLOAD_FILE) throw new IllegalStateException("Invalid access to file Hash");
		return (short) fileName.length;
	}
	
	public byte[] getFileName() {
		if (opcode != PeerMessageOps.OPCODE_DOWNLOAD_FILE) throw new IllegalStateException("Invalid access to file Hash");
		return fileName;
	}
	
	public void setFileName(byte[] data) {
		if (opcode != PeerMessageOps.OPCODE_DOWNLOAD_FILE) throw new IllegalStateException("Invalid access to file Hash");
		fileName = data;
	}
	
	public long getLengthHash() {
		if (opcode != PeerMessageOps.OPCODE_FILE_FOUNDED) throw new IllegalStateException("Invalid access to file Hash");
		return fileHash.length;
	}
	
	public byte[] getFileHash() {
		if (opcode != PeerMessageOps.OPCODE_FILE_FOUNDED) throw new IllegalStateException("Invalid access to file Hash");
		return fileHash;
	}
	
	public void setFileHash(byte[] data) {
		if (opcode != PeerMessageOps.OPCODE_FILE_FOUNDED) throw new IllegalStateException("Invalid access to file Hash");
		fileHash = data;
	}
	
	public long getFileOffset() {
		if (opcode != PeerMessageOps.OPCODE_GET_CHUNK) throw new IllegalStateException("Invalid access to file Hash");
		return fileOffset;
	}
	
	public void setFileOffset(long data) {
		if (opcode != PeerMessageOps.OPCODE_GET_CHUNK) throw new IllegalStateException("Invalid access to file Hash");
		fileOffset = data;
	}
	
	public int getChunckSize() {
		if (opcode != PeerMessageOps.OPCODE_GET_CHUNK) throw new IllegalStateException("Invalid access to chunk Size");
		return chunkSize;
	}
	
	public void setChunckSize(int data) {
		if (opcode != PeerMessageOps.OPCODE_GET_CHUNK) throw new IllegalStateException("Invalid access to chunk Size");
		chunkSize = data;
	}
	
	public long getLength() {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) throw new IllegalStateException("Invalid access to file Offset");
		return length;
	}
	
	public void setLength(long data) {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) throw new IllegalStateException("Invalid access to file Offset");
		length = data;
	}
	
	public byte[] getData() {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) throw new IllegalStateException("Invalid access to data");
		return data;
	};
	
	public void setData(byte[] data) {
		if (opcode != PeerMessageOps.OPCODE_SEND_CHUNK) throw new IllegalStateException("Invalid access to data");
		this.data = data;
	};
	
	public long getFileSize() {
		if (opcode != PeerMessageOps.OPCODE_FILE_SIZE) throw new IllegalStateException("Invalid access to data");
		return fileSize;
	};
	
	public void setFileSize(long data) {
		if (opcode != PeerMessageOps.OPCODE_GET_FILE_SIZE) throw new IllegalStateException("Invalid access to data");
		this.fileSize = data;
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
		 * DONE: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		byte opcode = dis.readByte();
		PeerMessage message = new PeerMessage(opcode);
		
		switch (opcode) {
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			case PeerMessageOps.OPCODE_TRANSFER_END:
			case PeerMessageOps.OPCODE_GET_FILE_SIZE:
			case PeerMessageOps.OPCODE_CHUNK_NOT_FOUND:
			case PeerMessageOps.OPCODE_FILE_AMBIGUOUS: {
				break;
			}
			
			case PeerMessageOps.OPCODE_FILE_SIZE: {
				long rFileSize = dis.readLong();
				if (rFileSize > Long.MAX_VALUE) throw new IOException("Size too large");
				message.setFileSize(rFileSize);
				break;
			}
				
			case PeerMessageOps.OPCODE_FILE_FOUNDED: {
				long rLengthHash = dis.readLong();
				if (rLengthHash > Long.MAX_VALUE) throw new IOException("Hash too large");
				byte[] rFileHash = new byte[(int) rLengthHash];
				dis.readFully(rFileHash);
				message.setFileHash(rFileHash);
				break;
			}
				
			case PeerMessageOps.OPCODE_DOWNLOAD_FILE: {
				long rLengthName = dis.readShort();
				if (rLengthName > Short.MAX_VALUE) throw new IOException("FileName too large");
				byte[] fileName = new byte[(int) rLengthName];
				dis.readFully(fileName);
				message.setFileName(fileName);
				break;
			}
				
			case PeerMessageOps.OPCODE_GET_CHUNK: {
				long rFileOffset = dis.readLong();
				if (rFileOffset > Long.MAX_VALUE) throw new IOException("FileOffset too large");
				message.setFileOffset(rFileOffset);
				
				int rChunkSize = dis.readInt();
				if (rFileOffset > Integer.MAX_VALUE) throw new IOException("ChunkSize too large");
				message.setChunckSize(rChunkSize);
				break;
			}
				
			case PeerMessageOps.OPCODE_SEND_CHUNK: {
				long length = dis.readLong();
				if (length > Long.MAX_VALUE) throw new IOException("Chunk too large");
				byte[] data = new byte[(int) length];
				dis.readFully(data);
				message.setData(data);
				break;
			}
				
				
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
			case PeerMessageOps.OPCODE_GET_FILE_SIZE:
			case PeerMessageOps.OPCODE_FILE_AMBIGUOUS:
			case PeerMessageOps.OPCODE_CHUNK_NOT_FOUND:
				break;
			
			case PeerMessageOps.OPCODE_FILE_FOUNDED: { 
				dos.writeLong(lengthHash);
				dos.write(fileHash);
				break;
			}
				
			case PeerMessageOps.OPCODE_DOWNLOAD_FILE:
				dos.writeShort(fileName.length);
				dos.write(fileName);
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
