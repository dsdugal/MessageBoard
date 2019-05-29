import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

public class MBServer {
	
	public static final int CMD_NULL = 0;
	public static final int CMD_CONNECT = 1;
	public static final int CMD_DISCONNECT = 2;
	public static final int CMD_CLEAR = 3;
	public static final int CMD_POST = 4;
	public static final int PORT_MAX = 65535;
	public static final int PORT_MIN = 0;
	
	public static class Board {
		
		private Vector<Message> messages;
		private Vector<User> users;
		
		public Board() {
			this.messages = new Vector<Message>();
			this.users = new Vector<User>();
		}
		
		public void addMsg( Message msg ) {
			messages.add( msg );
		}
		
		public void removeMsg( Message msg ) {
			messages.remove( msg );
		}
		
		public void addUser( User user ) {
			users.add( user );
		}
		
		public User getUser( String username ) {
			for ( User user : users ) {
				if ( user.getName() == username ) {
					return user;
				}
			}
			return null;
		}
		
		public void removeUser( String username ) {
			User user = getUser( username );
			if ( user != null ) {
				users.remove( user );
			}
		}
		
		public void clear() {
			messages.clear();
		}
		
	}
	
	public static class User {
		
		private int id;
		private String username;
		
		public User( int id, String username ) {
			this.id = id;
			this.username = username;
		}
		
		public int getID() {
			return this.id;
		}
		
		public String getName() {
			return this.username;
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
			return ( this.time + this.username + this.text );
		}
	}
	
	private static class MBThread extends Thread {
		
		private Board board;
		private int client;
		private Socket socket;
		
		// private int clear() {}
		
		// private int post() {}
		
		public MBThread( Board board, int client, Socket socket ) {
			this.board = board;
			this.client = client;
			this.socket = socket;
		}
		
		private void service( String request, PrintWriter out ) {
			if ( request != "" && out != null ) {
				Scanner input = new Scanner( request );
				int command = input.nextInt();
				String username = input.next();
				if ( command == CMD_CONNECT ) {
					board.addUser( new User( 0, username ));
					out.println( username );
				} else if ( command == CMD_DISCONNECT ) {
					board.removeUser( username );
					// display update ???
				} else if ( command == CMD_CLEAR ) {
					board.clear();
				} else if ( command == CMD_POST ) {
					String text = input.nextLine();
					Message msg = new Message( username, text );
					board.addMsg( msg );
					out.println( msg );
					// display update ???
				}
				// log activity
				
				
				
				
				
				
				
				
				//
				if ( command == CMD_POST ) {
					
				}
				input.close();
			}
		}

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader( socket.getInputStream() ));
				PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
				String request;
				System.out.println( "DEBUG - CONNECTION OPENED WITH CLIENT " + client );
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
					e.printStackTrace();
				}
				System.out.println( "DEBUG - CONNECTION CLOSED WITH CLIENT " + client );
			}
		}
		
	}
	
	public static void main( String[] args ) {
		if ( args.length >= 1 ) {
			System.out.println( "DEBUG - THE MESSAGE BOARD IS RUNNING." ); //
			int clients = 0;
			try {
				int port = Integer.parseInt( args[0] );
				if ( port >= PORT_MIN && port <= PORT_MAX ) {
					Board board = new Board();
					ServerSocket socket;
					try {
						socket = new ServerSocket( port );
						try {
							while ( true ) {
								new MBThread( board, clients++, socket.accept() ).start();
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
					// ERROR
				}
			} catch ( NumberFormatException e ) {
				e.printStackTrace();
			}
		}
	}
}
