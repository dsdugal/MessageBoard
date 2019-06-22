/* *************************************************************
 * 
 * A multi-threaded message board server to support chat
 * functionality for multiple connected clients.
 * 
 * Title	    MBServer.java
 * Author	    Dustin Dugal
 * Email	    dsdugal@gmail.com
 * Updated		2019-06-06
 * Version		1.0 (alpha)
 * 
 ************************************************************* */

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
	
	/* *************************************************************
	 * 
	 * Static constants.
	 * 
	 ************************************************************* */
	
	public static final int BROADCAST_INTERVAL = 15000;
	public static final int CMD_NULL = 0;
	public static final int CMD_CONNECT = 1;
	public static final int CMD_DISCONNECT = 2;
	public static final int CMD_CLEAR = 3;
	public static final int CMD_POST = 4;
	public static final int CMD_UPDATE = 5;
	public static final int PORT_ARG = 1;
	public static final int PORT_MAX = 65535;
	public static final int PORT_MIN = 0;
	public static final String ERROR_INVALID_PORT = "ERROR: INVALID PORT SPECIFIED.";
	public static final String ERROR_NO_PORT = "ERROR: NO PORT SPECIFIED.";
	public static final String LOG_FILENAME = "logfile.txt";
	public static final String LOG_SERVER_START = "MBSERVER STARTED";
	public static final String LOG_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/* *************************************************************
	 * 
	 * Class for holding synchronized server information.
	 * 
	 ************************************************************* */
	
	private static class Board {
		
		private Vector<Client> clients;
		private Vector<Message> messages;
		
		/* *************************************************************
		 * 
		 * Constructor.
		 * 
		 * Use:
		 * 		Board board = new Board();
		 * 
		 ************************************************************* */
		
		public Board() {
			this.clients = new Vector<Client>();
			this.messages = new Vector<Message>();
		}
		
		/* *************************************************************
		 * 
		 * Adds a message to this server's message board.
		 * 
		 * Parameters:
		 * 		msg (Message)
		 * 			A message object to be added to the server's internal
		 * 			message list.
		 * 
		 * Use:
		 * 		board.addMsg( msg );
		 * 
		 ************************************************************* */
		
		public void addMsg( Message msg ) {
			messages.add( msg );
		}
		
		/* *************************************************************
		 * 
		 * Removes a message from this server's message board.
		 * 
		 * Parameters:
		 * 		msg (Message)
		 * 			The message object to be removed from the server's
		 * 			internal message list.
		 * 
		 * Use:
		 * 		board.removeMsg( msg );
		 * 
		 ************************************************************* */
		
		public void removeMsg( Message msg ) {
			messages.remove( msg );
		}
		
		/* *************************************************************
		 * 
		 * Adds a client to this server's list of connected clients.
		 * 
		 * Parameters:
		 * 		client (Client)
		 * 			The client object to be added to this server's
		 * 			internal client list.
		 * 
		 * Use:
		 * 		board.addClient( client );
		 * 
		 ************************************************************* */
		
		public void addClient( Client client ) {
			clients.add( client );
		}
		
		/* *************************************************************
		 * 
		 * Returns a client from this server's list of connected
		 * clients that matches a given username.
		 * 
		 * Parameters:
		 * 		client (Client)
		 * 			The client object to be removed from this server's
		 * 			internal client list.
		 * 
		 * Returns:
		 * 		A client object if it exists in the client list,
		 * 		otherwise null.
		 * 
		 * Use:
		 * 		Client client = board.getClient( client );
		 * 
		 ************************************************************* */
		
		public Client getClient( String username ) {
			for ( Client client : clients ) {
				if ( client.getName() == username ) {
					return client;
				}
			}
			return null;
		}
		
		/* *************************************************************
		 * 
		 * Removes and returns a client from this server's list of
		 * connected clients if an associated username exists, otherwise
		 * returns null.
		 * 
		 * Parameters:
		 * 		username (String)
		 * 			The username associated with the client object to be
		 * 			removed from this server's internal client list.
		 * 
		 * Use:
		 * 		Client client = board.removeClient( username );
		 * 
		 ************************************************************* */
		
		public void removeClient( String username ) {
			Client client = getClient( username );
			if ( client != null ) {
				clients.remove( client );
			}
		}
		
		/* *************************************************************
		 * 
		 * Removes all messages from this server's message board.
		 * 
		 * Use:
		 * 		board.clear();
		 * 
		 ************************************************************* */
		
		public void clear() {
			messages.clear();
		}
		
	}
	
	/* *************************************************************
	 * 
	 * Class for holding connected client information.
	 * 
	 ************************************************************* */
	
	public static class Client {
		
		private int id;
		private String username;
		
		/* *************************************************************
		 * 
		 * Constructs and returns a client object.
		 * 
		 * Parameters:
		 * 		id (int)
		 * 			An integer between 0 and ??? that represents a
		 * 			unique ID assigned by the server. ****
		 * 		username (String)
		 * 			The username associated with this client.
		 * 
		 ************************************************************* */
		
		public Client( int id, String username ) {
			this.id = id;
			this.username = username;
		}
		
		/* *************************************************************
		 * 
		 * Returns the unique ID of this client.
		 * 
		 * Use:
		 * 		int id = client.getID();
		 * 
		 ************************************************************* */
		
		public int getID() {
			return this.id;
		}
		
		/* *************************************************************
		 * 
		 * Returns the username associated with this client.
		 * 
		 * Use:
		 * 		String username = client.getName();
		 * 
		 ************************************************************* */
		
		public String getName() {
			return this.username;
		}
		
		/* *************************************************************
		 * 
		 * Returns a string object to represent the attributes of this
		 * class to enable easy printing and comparisons.
		 * 
		 * Use:
		 * 		System.out.println( this );
		 * 
		 ************************************************************* */
		
		@Override
		public String toString() {
			return ( this.id + " " + this.username );
		}
		
	}
	
	/* *************************************************************
	 * 
	 * Class for holding chat history/information.
	 * 
	 ************************************************************* */
	
	public static class Message {
		
		private Date time;
		private String username;
		private String text;
		
		/* *************************************************************
		 * 
		 * Constructs and returns a message object.
		 * 
		 * Parameters:
		 * 		username (String)
		 * 			The username of the client associatd with this
		 * 			message.
		 * 		text (String)
		 * 			The text content of this message.
		 * 
		 * Use:
		 * 		Message message = new Message( username, text );
		 * 
		 ************************************************************* */
		
		public Message( String username, String text ) {
			this.time = new Date();
			this.username = username;
			this.text = text;
		}
		
		/* *************************************************************
		 * 
		 * Returns the date and time associated with this message.
		 * 
		 * Use:
		 * 		Date date = message.getTime();
		 * 
		 ************************************************************* */
		
		public Date getTime() {
			return this.time;
		}
		
		/* *************************************************************
		 *
		 * Returns the username associated with this message.
		 * 
		 * Use:
		 * 		String username = message.getName();
		 * 
		 ************************************************************* */
		
		public String getName() {
			return this.username;
		}
		
		/* *************************************************************
		 * 
		 * Returns a string object to represent the attributes of this
		 * class to enable easy printing and comparisons.
		 * 
		 * Use:
		 * 		System.out.println( this );
		 * 
		 ************************************************************* */
		
		@Override
		public String toString() {
			String timestamp = new SimpleDateFormat( "hh:mm" ).format( this.time );
			return ( timestamp + " " + this.username + " " + this.text );
		}
		
	}
	
	/* *************************************************************
	 * 
	 * Class to enable multi-threading.
	 * 
	 ************************************************************* */
	
	private static class MBThread extends Thread {
		
		private int client;
		private Board board;
		private Socket socket;
		
		/* *************************************************************
		 * 
		 * Constructor.
		 * 
		 ************************************************************* */
		
		public MBThread( Board board, int client, Socket socket ) {
			this.client = client;
			this.board = board;
			this.socket = socket;
			log( LOG_SERVER_START );
		}
		
		/* *************************************************************
		 * 
		 * Prints service request information into the server's log.
		 * 
		 * Parameters:
		 * 		message (String)
		 * 			The information to be logged.
		 * 
		 * Use:
		 * 		log( message );
		 * 
		 ************************************************************* */
		
		private void log( String message ) {
			try {
				BufferedWriter buffer = new BufferedWriter( new FileWriter( LOG_FILENAME, true ) );
				PrintWriter writer = new PrintWriter( buffer );
				String time = new SimpleDateFormat( LOG_TIME_FORMAT ).format( new Date() );
				writer.println( time + " " + message );
				writer.close();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		
		/* *************************************************************
		 * 
		 * Services requests made by clients, and writes information
		 * required to fulfill those requests to the client's input
		 * stream using its TCP connection.
		 * 
		 * Parameters:
		 * 		request (String)
		 * 			A formatted string that contains the information
		 * 			necessary to service a valid request.
		 * 		out (PrintWriter)
		 * 			The PrintWriter associated with the requesting
		 * 			client's input stream.
		 * 
		 * Use:
		 * 		service( request, out );
		 * 
		 ************************************************************* */
		
		private void service( String request, PrintWriter out ) {
			if ( request != "" && out != null ) {
				Scanner input = new Scanner( request );
				if ( input.hasNext() ) {
					int command = input.nextInt();
					if ( input.hasNext() ) {
						String username = input.next(); // +++
						if ( command == CMD_NULL ) {
							/*
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
							*/
							out.println( CMD_NULL ); // ???
						} else if ( command == CMD_CONNECT ) {
							board.addClient( new Client( 0, username ));
							out.println( username );
						} else if ( command == CMD_DISCONNECT ) {
							board.removeClient( username );
						} else if ( command == CMD_CLEAR ) {
							board.clear();
						} else if ( command == CMD_POST ) {
							String text = input.nextLine();
							Message msg = new Message( username, text );
							board.addMsg( msg );
							out.println( msg );
						}
					}
					log( request );
				}
				input.close();
			}
		}

		/* *************************************************************
		 * 
		 * Runs a thread.
		 * 
		 ************************************************************* */
		
		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader( socket.getInputStream() ));
				PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
				String request;
				long previousTime = 0; //
				long currentTime = 0; //
				while ( true ) {
					if ((request = in.readLine()) != null ) { // while
						/*
						currentTime = System.currentTimeMillis();
						if (( currentTime - previousTime ) >= BROADCAST_INTERVAL ) {
							System.out.println( "broadcast time: " + currentTime );
							previousTime = currentTime;
						}
						*/

						service( request, out );
					}
					System.err.println( System.currentTimeMillis() ); // DEBUG

				}
			} catch ( IOException e ) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/* *************************************************************
	 * 
	 * Launches the server.
	 * 
	 * Parameters:
	 * 		args (String array)
	 * 			Must contain a valid port number to start the server.
	 * 
	 ************************************************************* */
	
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
					System.err.println( ERROR_INVALID_PORT );
				}
			} catch ( NumberFormatException e ) {
				e.printStackTrace();
			}
		} else {
			System.err.println( ERROR_NO_PORT );
		}
	}
	
}
