package es.um.redes.nanoFiles.logic;

import java.net.InetSocketAddress;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.application.NanoFiles;



import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	/*
	 * DONE: Se necesita un atributo NFServer que actuará como servidor de ficheros
	 * de este peer
	 */
	private NFServer fileServer = null;


	protected NFControllerLogicP2P() {
	}

	/**
	 * Método para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y está a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer() {
		boolean serverRunning = false;
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		/*
		 * DONE: (Boletín Servidor TCP concurrente) Arrancar servidor en segundo plano
		 * creando un nuevo hilo, comprobar que el servidor está escuchando en un puerto
		 * válido (>0), imprimir mensaje informando sobre el puerto de escucha, y
		 * devolver verdadero. Las excepciones que puedan lanzarse deben ser capturadas
		 * y tratadas en este método. Si se produce una excepción de entrada/salida
		 * (error del que no es posible recuperarse), se debe informar sin abortar el
		 * programa
		 * 
		 */
		if (fileServer != null) {
			System.err.println("File server is already running");
			return false;
		} else {
			try {
				fileServer = new NFServer();
				Thread thread = new Thread(fileServer);
				thread.setDaemon(true);
				thread.start();
				
				int port = fileServer.getPort();
				if(port <= 0) throw new IOException("Invalid port: " + port);
				System.out.println("File server listening on: " + port);
				serverRunning = true;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return serverRunning;

	}

	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya está en marcha.
		 */
		assert (fileServer == null);
		try {

			fileServer = new NFServer();
			/*
			 * (Boletín SocketsTCP) Inicialmente, se creará un NFServer y se ejecutará su
			 * método "test" (servidor minimalista en primer plano, que sólo puede atender a
			 * un cliente conectado). Posteriormente, se desactivará "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
			// Este código es inalcanzable: el método 'test' nunca retorna...
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Cannot start the file server");
			fileServer = null;
		}
	}

	public void testTCPClient() {

		assert (NanoFiles.testModeTCP);
		/*
		 * (Boletín SocketsTCP) Inicialmente, se creará un NFConnector (cliente TCP)
		 * para conectarse a un servidor que esté escuchando en la misma máquina y un
		 * puerto fijo. Después, se ejecutará el método "test" para comprobar la
		 * comunicación mediante el socket TCP. Posteriormente, se desactivará
		 * "testModeTCP" para implementar la descarga de un fichero desde múltiples
		 * servidores.
		 */

		try {
			NFConnector nfConnector = new NFConnector(new InetSocketAddress(NFServer.PORT));
			nfConnector.test();
		} catch (IOException e) {
			// DONE Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Método para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList       La lista de direcciones de los servidores a
	 *                                los que se conectará
	 * @param targetFileNameSubstring Subcadena del nombre del fichero a descargar
	 * @param localFileName           Nombre con el que se guardará el fichero
	 *                                descargado
	 */
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, String targetFileNameSubstring,
			String localFileName) {
		
		boolean downloaded = false;

		if (serverAddressList.length == 0) {
			System.err.println("* Cannot start download - No list of server addresses provided");
			return false;
		}
		
		/*
		 * DONE: Crear un objeto NFConnector distinto para establecer una conexión TCP
		 * con cada servidor de ficheros proporcionado, y usar dicho objeto para
		 * descargar trozos (chunks) del fichero. 
		 * 
		 * Se debe comprobar previamente si ya existe un fichero con el mismo nombre 
		 * (localFileName) en esta máquina, en cuyo caso se informa y no se realiza la descarga. 
		 * 
		 * Se debe asegurar que el fichero cuyos datos se solicitan es el mismo para todos los servidores
		 * involucrados (el fichero está identificado por su hash). 
		 * 
		 * Una vez descargado,
		 * se debe comprobar la integridad del mismo calculando el hash mediante
		 * FileDigest.computeFileChecksumString. Si todo va bien, imprimir resumen de la
		 * descarga informando de los trozos obtenidos de cada servidor involucrado. Las
		 * excepciones que puedan lanzarse deben ser capturadas y tratadas en este
		 * método. Si se produce una excepción de entrada/salida (error del que no es
		 * posible recuperarse), se debe informar sin abortar el programa
		 */
		
		// Comprobamos que no exista un archivo con el mismo nombre
		if( FileInfo.lookupFilenameSubstring(NanoFiles.db.getFiles(), localFileName).length != 0) {
			System.err.println("No se puede realizar la descarga porque ya existe un fichero con el nombre: " + localFileName );
			return false;
		}
		
		 
		File file = new File("nf-shared", localFileName);
		
		RandomAccessFile rafFile;
		try {
			rafFile = new RandomAccessFile(file, "rw");
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		// Obtenemos el tamaño del archivo para calcular el tamaño del chunk
		String fileHash = null;
		int chunk_size = 0;
		try {
			NFConnector conectServer = new NFConnector(serverAddressList[0]);
			// Pedir a cada servidor el chunk especifico
			fileHash = conectServer.getFileHash(targetFileNameSubstring);
			chunk_size = (int) (conectServer.getFileSize() / serverAddressList.length);
			conectServer.transferEnd();
					
		} catch (IOException ex) {
			System.out.println("Error al establecer conexión TCP: " + ex.getMessage());
		}
		
		if(fileHash == null) {
			System.err.println("Error looking for the file");
			try {
				rafFile.close();
			} catch (IOException ignore) {
				
			}
			file.delete();
			return false;
		}
		
		if(chunk_size <= 0) {
			System.err.println("Error reading the filesize");
			try {
				rafFile.close();
			} catch (IOException ignore) {
				
			}
			file.delete();
			return false;
		} 
	
		// Iteramos todos los servidores para descargar un chunk de cada servidor
		long offset = 0;
		for (InetSocketAddress fserverAddr : serverAddressList) {
			try {
				NFConnector conectServer = new NFConnector(fserverAddr);
				String serverHash = conectServer.getFileHash(targetFileNameSubstring);
				if(serverHash == null) {
					System.err.println("getFileHash failed");
					try {
						rafFile.close();
					} catch (IOException ignore) {
						
					}
					file.delete();
					return false;
				}
				
				if(!fileHash.equals(serverHash)) {
					System.err.println("Hashes are different");
					try {
						rafFile.close();
					} catch (IOException ignore) {
						
					}
					return false;
				}
				
				byte[] chunk = conectServer.getChunk(offset, chunk_size);
				
				rafFile.seek(offset);
				rafFile.write(chunk);
				
				offset += chunk_size;
				
				conectServer.transferEnd();
						
			} catch (IOException ex) {
				System.out.println("Error al establecer conexión TCP: " + ex.getMessage());
			}
			
		}
		
		try {
			rafFile.close();
		} catch (IOException ignore) {
			
		}
		
		String actualFileHash;
		actualFileHash = FileDigest.computeFileChecksumString(file.getPath());
		
		// Comprobamos la integridad del archivo
		if(!fileHash.equals(actualFileHash)) {
			System.err.println("Hashes doesnt match");
			file.delete();
			return false;
		}
			
		System.out.println("Descarga completada");
		return true;
				
	}

	/**
	 * Método para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	protected int getServerPort() {
		/*
		 * DONE: Devolver el puerto de escucha de nuestro servidor de ficheros
		 */
		return fileServer != null ? fileServer.getPort() : 0;
		
	}

	/**
	 * Método para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	protected void stopFileServer() {
		/*
		 * DONE: Enviar señal para detener nuestro servidor de ficheros en segundo plano
		 */
		if(fileServer != null) {
			fileServer.stopServer();
			fileServer = null;
			System.out.println("File server stopped");
		}
		


	}

	protected boolean serving() {

		return (fileServer != null && fileServer.isRunning());

	}

	protected boolean uploadFileToServer(FileInfo matchingFile, String uploadToServer) {
		boolean result = false;



		return result;
	}

}
