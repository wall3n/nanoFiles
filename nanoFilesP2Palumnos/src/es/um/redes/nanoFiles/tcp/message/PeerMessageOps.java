package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

	public static final byte OPCODE_INVALID_CODE = 0;

	/*
	 * DONE: (Boletín MensajesBinarios) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con un par
	 * servidor de ficheros (valores posibles del campo "operation").
	 */
	public static final byte OPCODE_FILE_NOT_FOUND = 0x01;
	public static final byte OPCODE_DOWNLOAD_FILE = 0x02;
	public static final byte OPCODE_GET_CHUNK = 0x03;
	public static final byte OPCODE_SEND_CHUNK = 0x04;
	public static final byte OPCODE_TRANSFER_END = 0x05;
	public static final byte OPCODE_FILE_FOUNDED = 0x06;
	public static final byte OPCODE_CHUNK_NOT_FOUND = 0x07;
	public static final byte OPCODE_FILE_AMBIGUOUS = 0x08;
	public static final byte OPCODE_GET_FILE_SIZE = 0x09;
	public static final byte OPCODE_FILE_SIZE = 0x10;


	/*
	 * DONE: (Boletín MensajesBinarios) Definir constantes con nuevos opcodes de
	 * mensajes definidos anteriormente, añadirlos al array "valid_opcodes" y añadir
	 * su representación textual a "valid_operations_str" EN EL MISMO ORDEN.
	 */
	private static final Byte[] _valid_opcodes = {
			OPCODE_INVALID_CODE,
			OPCODE_FILE_NOT_FOUND,
			OPCODE_DOWNLOAD_FILE,
			OPCODE_GET_CHUNK,
			OPCODE_SEND_CHUNK,
			OPCODE_TRANSFER_END,
			OPCODE_FILE_FOUNDED,
			OPCODE_CHUNK_NOT_FOUND,
			OPCODE_FILE_AMBIGUOUS,
			OPCODE_GET_FILE_SIZE,
			OPCODE_FILE_SIZE,
	};
	private static final String[] _valid_operations_str = { 
			"INVALID_OPCODE",
			"FILE_NOT_FOUND",
			"DOWNLOAD_FILE",
			"GET_CHUNK",
			"SEND_CHUNK",
			"TRANSFER_END",
			"FILE_FOUNDED",
			"CHUNK_NOT_FOUND",
			"FILE_AMBIGUOUS",
			"GET_FILE_SIZE",
			"FILE_SIZE",
	};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}

	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OPCODE_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
