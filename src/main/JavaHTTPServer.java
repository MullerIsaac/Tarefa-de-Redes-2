package main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class JavaHTTPServer implements Runnable{
	static final File WEB_ROOT = new File(".");
	
	static final int PORT = 5555;
	
	static final boolean verbose = true;
	
	private Socket connect;
	
	public JavaHTTPServer(Socket c) {
		connect = c;
	}
	
	public static void main(String[] args) {
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Servidor Iniciado...");
			
			while(true) {
				JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());
				
				if(verbose) {
					System.out.println("Conexão aberta");
				}
				
				Thread thread = new Thread(myServer);
				thread.start();
			}
		} catch (IOException ioe) {
			System.err.println("Erro de Conexao com servidor: "+ ioe.getMessage());
		}
	}

	@Override
	public void run() {
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		String fileRequested = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
			String input = in.readLine();
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			fileRequested = parse.nextToken().toLowerCase();
			
			if(!method.equals("GET") && !method.equals("HEAD")) {
				if(verbose) {
					System.out.println("metodo não implementado");
				}
				File file = new File(WEB_ROOT, "nsuportado.txt");
				int fileLenght = (int)file.length();
				String contentType = "text/plain";
				byte[] fileData = readFileData(file, fileLenght);
				
				out.println("HTTP/1.1 NÃO IMPLEMENTADO");
				out.println("Server: Atividade de Redes");
				out.println("Data: "+new Date());
				out.println("Content-type: "+contentType);
				out.println();
				out.flush();
				dataOut.write(fileData, 0, fileLenght);
				dataOut.flush();
				
			}else {
				File file = new File(WEB_ROOT, fileRequested);
				int fileLenght = (int)file.length();
				String contentType = "text/plain";
				byte[] fileData = readFileData(file, fileLenght);
				
				out.println("HTTP 200 OK");
				out.println("Server: Atividade de Redes");
				out.println("Data: "+new Date());
				out.println("Content-type: "+contentType);
				out.println();
				out.flush();
				dataOut.write(fileData, 0, fileLenght);
				dataOut.flush();
				
			}
		}catch (FileNotFoundException fnfe) {
			try {
				fileNotFound(out, dataOut, fileRequested);
			} catch (IOException ioe) {
				System.err.println("Arquivo não encontrado : " + ioe.getMessage());
			}
			
		}
		catch(IOException ioe) {
			System.err.println("Erro no servidor: "+ioe);
		}finally {
			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); 
			} catch (Exception e) {
				System.err.println("Erro ao fechar a conexão : " + e.getMessage());
			} 
			
			if (verbose) {
				System.out.println("Conexão Fechada.\n");
			}
		}
		
	}

	private byte[] readFileData(File file, int fileLenght) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLenght];
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		}finally{
			if(fileIn != null)
				fileIn.close();
		}
		
		return fileData;
		
	}
	
	private void fileNotFound(PrintWriter out, BufferedOutputStream dataOut, String fileRequested) throws IOException {
		File file = new File(WEB_ROOT, "filenotfound.txt");
		int fileLength = (int) file.length();
		String content = "text/plain";
		byte[] fileData = readFileData(file, fileLength);
		
		out.println("HTTP 404 NOT FOUND");
		out.println("Server: Atividade de Redes");
		out.println("Data: " + new Date());
		out.println("Content-type: " + content);
		out.println("Content-length: " + fileLength);
		out.println();
		out.flush(); 
		
		dataOut.write(fileData, 0, fileLength);
		dataOut.flush();
		
	}
}
