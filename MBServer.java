import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

public class MBServer {
	
	public static final int CMD_NULL = 0;
	public static final int CMD_CONNECT = 1;
	public static final int CMD_DISCONNECT = 2;
	public static final int CMD_CLEAR = 3;
	public static final int CMD_POST = 4;
	public static final int CMD_UPDATE = 5;
	public static final int PORT_ARG = 1;
	public static final int PORT_MAX = 65535;
	public static final int PORT_MIN = 0;
	public static final int STATUS_NO_UPDATE = 0;
	public static final int STATUS_UPDATE = 1;
	public static final String ERROR_NO_PORT = "ERROR: NO PORT SPECIFIED.";
	public static final String LOG_FILENAME = "logfile.txt";
	public static final String LOG_SERVER_START = "MBSERVER STARTED";
	public static final String LOG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	public static class Board {
		
		private int status;
		private Vector<Client> clients;
		private Vector<Message> messages;
		
		public Board() {
			this.clients = new Vector<Client>();
			this.messages = new Vector<Message>();
			this.status = STATUS_NO_UPDATE;
		}
		
		public void addMsg( Message msg ) {
			messages.add( msg );
		}
		
		public void removeMsg( Message msg ) {
			messages.remove( msg );
		}
		
		public void addClient( Client client ) {
			clients.add( client );
		}
		
		public Client getClient( String username ) {
			for ( Client client : clients ) {
				if ( client.getName() == username ) {
					return client;
				}
			}
			return null;
		}
		
		public int getStatus() {
			return this.status;
		}
		
		public void removeClient( String username ) {
			Client client = getClient( username );
			if ( client != null ) {
				clients.remove( client );
			}
		}
		
		public void setStatus( int status ) {
			this.status = status;
		}
		
		public void clear() {
			messages.clear();
		}
		
	}
	
	public static class Client {
		
		private int id;
		private String username;
		
		public Client( int id, String username ) {
			this.id = id;
			this.username = username;
		}
		
		public int getID() {
			return this.id;
		}
		
		public String getName() {
			return this.username;
		}
		
		public String toString() {
			return ( this.id + " " + this.username );
		}
		
	}
	
	public static class Message {
		
		private Date time;
		private String username;
		private String text;
		
		public Message( String username, String text ) {
			this.time = new Date();
			this.username = username;
			this.text = text;
		}
		
		public Date getTime() {
			return this.time;
		}
		
		public String getName() {
			return this.username;
		}
		
		public String toString() {
			String timestamp = new SimpleDateFormat( "hh:mm" ).format( this.time );
			return ( timestamp + " " + this.username + " " + this.text );
		}
		
	}
	
	private static class MBThread extends Thread {
		
		private int client;
		private Board board;
		private Socket socket;
		
		public MBThread( Board board, int client, Socket socket ) {
			this.client = client;
			this.board = board;
			this.socket = socket;
			log( LOG_SERVER_START );
		}
		
		private void log( String message ) {
			try {
				BufferedWriter buffer = new BufferedWriter( new FileWriter( LOG_FILENAME, true ) );
				PrintWriter writer = new PrintWriter( buffer );
				String time = new SimpleDateFormat( LOG_TIME_FORMAT ).format( new Date() );
				writer.println( time + " " + message );
				writer.close();
			} catch ( IOException e ) {
				e.printStackTrace(); // ???
			}
		}
		
		private void service( String request, PrintWriter out ) {
			if ( request != "" && out != null ) {
				Scanner input = new Scanner( request );
				int command = input.nextInt();
				String username = input.next();
				if ( command == CMD_NULL ) {
					if ( board.getStatus() == STATUS_UPDATE ) {
						String clientList = "";
						for ( Client client : board.clients ) {
							clientList += client.getName() + "";
						}
						out.println( CMD_UPDATE + " " + clientList );
						board.setStatus( STATUS_NO_UPDATE );
					} else {
						out.println( CMD_NULL );
					}
				} else if ( command == CMD_CONNECT ) {
					board.addClient( new Client( 0, username ));
					out.println( username );
				} else if ( command == CMD_DISCONNECT ) {
					board.removeClient( username );
					board.setStatus( STATUS_UPDATE );
				} else if ( command == CMD_CLEAR ) {
					board.clear();
				} else if ( command == CMD_POST ) {
					String text = input.nextLine();
					Message msg = new Message( username, text );
					board.addMsg( msg );
					out.println( msg );
				}
				log( request );
				input.close();
			}
		}

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader( socket.getInputStream() ));
				PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
				String request;
				while ( true ) {
					while ((request = in.readLine()) != null ) {
						service( request, out );
					}
				}
			} catch ( IOException e ) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch ( IOException e ) {
					// e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main( String[] args ) {
		if ( args.length >= PORT_ARG ) {
			int client = 0;
			try {
				int port = Integer.parseInt( args[0] );
				if ( port >= PORT_MIN && port <= PORT_MAX ) {
					Board board = new Board();
					ServerSocket socket;
					try {
						socket = new ServerSocket( port );
						try {
							while ( true ) {
								new MBThread( board, client++, socket.accept() ).start();
							}
						} catch ( IOException e ) {
							e.printStackTrace();
						} finally {
							socket.close();
						}
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				} else {
					System.err.println( ERROR_NO_PORT );
				}
			} catch ( NumberFormatException e ) {
				e.printStackTrace();
			}
		}
	}
	
}
