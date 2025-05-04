package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * DONE: (Boletín MensajesASCII) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con el
	 * directorio (valores posibles del campo "operation").
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_PING = "ping";
	public static final String OPERATION_PING_OK = "ping_ok";
	public static final String OPERATION_BAD_PROTOCOL = "bad_protocol";
	public static final String OPERATION_FILELIST_REQUEST = "filelist_request";
	public static final String OPERATION_FILELIST_OK = "filelist_ok";
	public static final String OPERATION_SERVE = "serve";
	public static final String OPERATION_SERVE_OK = "serve_ok";
	public static final String OPERATION_DOWNLOAD_REQUEST = "download_request";
	public static final String OPERATION_DOWNLOAD_OK = "download_ok";
	public static final String OPERATION_FILE_NOT_FOUND = "file_not_found";
	public static final String OPERATION_FILE_AMBIGUOUS = "file_ambiguous";



}
