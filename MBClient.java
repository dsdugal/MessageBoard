/* *************************************************************
 * 
 * A message board client with a GUI to support chat
 * functionality for one connected client.
 * 
 * Title		MBClient.java
 * Author		Dustin Dugal
 * Email		dsdugal@gmail.com
 * Updated		2019-06-06
 * Version		1.0 (alpha)
 * 
 ************************************************************* */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MBClient {
	
	/* *************************************************************
	 * 
	 * Static constants.
	 * 
	 ************************************************************* */
	
	public static final int CMD_NULL = 0;
	public static final int CMD_CONNECT = 1;
	public static final int CMD_DISCONNECT = 2;
	public static final int CMD_CLEAR = 3;
	public static final int CMD_POST = 4;
	public static final int CMD_UPDATE = 5;
	public static final int IP_OCTET_MAX = 255;
	public static final int IP_OCTET_MIN = 0;
	public static final int PORT_MAX = 65535;
	public static final int PORT_MIN = 0;
	public static final int VIEW_MAIN_HEIGHT = 400;
	public static final int VIEW_MAIN_WIDTH = 510;
	public static final Font FONT_BOARD = Font.font( "Agency FB", 12 );
	public static final Font FONT_CONTROL = Font.font( "Agency FB", 14);
	public static final Font FONT_LABEL = Font.font( "Verdana", 12 );
	public static final String ERROR_NO_ADDRESS_PORT = "ERROR: NO ADDRESS AND/OR PORT SPECIFIED.";
	public static final String ERROR_NO_SERVER = "ERROR: COULD NOT ESTABLISH CONNECTION WITH SERVER.";
	public static final String TEXT_CONTROL_ABOUT = "About";
	public static final String TEXT_CONTROL_CLEAR = "CLEAR";
	public static final String TEXT_CONTROL_CONNECT = "CONNECT";
	public static final String TEXT_CONTROL_DISCONNECT = "DISCONNECT";
	public static final String TEXT_CONTROL_POST = "POST";
	public static final String TEXT_LABEL_CONNECT = "CONNECTION";
	public static final String TEXT_LABEL_IP = "IP Address:";
	public static final String TEXT_LABEL_MESSAGE = "MESSAGE BOARD";
	public static final String TEXT_LABEL_PORT = "Port:";
	public static final String USERNAME_DEFAULT = "MBUser";
	
	/* *************************************************************
	 * 
	 * Interface to facilitate client-server interactions.
	 * 
	 ************************************************************* */
	
	public static class View extends Application implements EventHandler<ActionEvent> {
		
		/* *************************************************************
		 * 
		 * GUI elements.
		 * 
		 ************************************************************* */
		
		private Button btnClear;
		private Button btnConnect;
		private Button btnPost;
		private HBox hbxChat;
		private HBox hbxConnect;
		private HBox hbxMessage;
		private Label labIPaddress;
		private Label labIPab;
		private Label labIPbc;
		private Label labIPcd;
		private Label labPort;
		private Menu mnuAbout;
		private MenuBar mbrMain;
		private Separator sepChat;
		private Separator sepIP;
		private Separator sepLine;
		private Separator sepMessage;
		private Separator sepPort;
		private Separator sepPost;
		private TextArea txaChat;
		private TextArea txaClient;
		private TextArea txaMessage;
		private TextField txfIPa;
		private TextField txfIPb;
		private TextField txfIPc;
		private TextField txfIPd;
		private TextField txfPort;
		private TitledPane tpnChat;
		private TitledPane tpnConnect;
		private VBox vbxBackground;
		private VBox vbxChat;
		private VBox vbxInteract;
		
		/* *************************************************************
		 * 
		 * Client resources.
		 * 
		 ************************************************************* */
		
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
		private String username;
		
		/* *************************************************************
		 * 
		 * Constructor.
		 * 
		 * Use: View view = new View()
		 * 
		 ************************************************************* */
		
		public View() {
			this.in = null;
			this.out = null;
			this.socket = null;
			this.username = USERNAME_DEFAULT;
		}

		/* *************************************************************
		 * 
		 * Connects this client to the given server through a TCP
		 * socket.
		 * 
		 * Parameters:
		 * 		address (String)
		 * 			A string in the format a.b.c.d where a, b, c, and d
		 * 			are integers between 0 and 255.
		 *   
		 *   	port (int)
		 *   		An integer between 0 and 65535.
		 * 
		 * Use:
		 * 		connect( address, port );
		 * 
		 ************************************************************* */
		
		private void connect( String address, int port ) {
			try {
				socket = new Socket( address, port );
				in = new BufferedReader( new InputStreamReader( socket.getInputStream() ));
				out = new PrintWriter( socket.getOutputStream(), true );
				btnClear.setDisable( false );
				btnConnect.setText( TEXT_CONTROL_DISCONNECT );
				btnPost.setDisable( false );
				txaChat.setDisable( false );
				txaClient.setDisable( false );
				txaMessage.setDisable( false );
				txfIPa.setDisable( true );
				txfIPb.setDisable( true );
				txfIPc.setDisable( true );
				txfIPd.setDisable( true );
				txfPort.setDisable( true );
			} catch ( UnknownHostException e ) {
				e.printStackTrace();
			} catch ( ConnectException e ) {
				System.err.println( ERROR_NO_SERVER );
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		
		/* *************************************************************
		 * 
		 * Disconnects this client from a server connected through its
		 * private TCP socket.
		 * 
		 * Use:
		 * 		disconnect();
		 * 
		 ************************************************************* */
		
		private void disconnect() {
			try {
				socket.close();
				in = null;
				out = null;
				btnClear.setDisable( true );
				btnConnect.setText( TEXT_CONTROL_CONNECT );
				btnPost.setDisable( true );
				txaChat.clear();
				txaChat.setDisable( true );
				txaClient.clear();
				txaClient.setDisable( true );
				txaMessage.clear();
				txaMessage.setDisable( true );
				txfIPa.clear();
				txfIPa.setDisable( false );
				txfIPb.clear();
				txfIPb.setDisable( false );
				txfIPc.clear();
				txfIPc.setDisable( false );
				txfIPd.clear();
				txfIPd.setDisable( false );
				txfPort.clear();
				txfPort.setDisable( false );
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		
		/* *************************************************************
		 * 
		 * Constructs and returns a valid IP address from the
		 * information input by a user into the GUI elements.
		 * 
		 * Use:
		 * 		String address = getAddress();
		 * 
		 ************************************************************* */

		private String getAddress() {
			boolean valid = true;
			int values[] = new int[4];
			try {
				values[0] = Integer.parseInt( txfIPa.getText() );
				values[1] = Integer.parseInt( txfIPb.getText() );
				values[2] = Integer.parseInt( txfIPc.getText() );
				values[3] = Integer.parseInt( txfIPd.getText() );
				for ( int i = 0; i < 4; i++ ) {
					if ( values[i] >= IP_OCTET_MIN && values[i] <= IP_OCTET_MAX ) {
						valid = valid & true;
					}
				}
				if ( valid ) {
					return txfIPa.getText() + "." + txfIPb.getText() + "." + txfIPc.getText() + "." + txfIPd.getText();
				} else {
					return "";
				}
			} catch ( NumberFormatException e ) {
				return "";
			}
		}
		
		/* *************************************************************
		 * 
		 * Constructs and returns a valid network port number from the
		 * information input by a user into the GUI elements.
		 * 
		 * Use:
		 * 		String port = getPort();
		 * 
		 ************************************************************* */
		
		private int getPort() {
			try {
				int port = Integer.parseInt( txfPort.getText() );
				if ( port >= PORT_MIN && port <= PORT_MAX ) {
					return port;
				} else {
					return -1;
				}
			} catch ( NumberFormatException e ) {
				return -1;
			}
		}
		
		/* *************************************************************
		 * 
		 * Constructs and determines the properties of the GUI elements.
		 * 
		 * Use:
		 * 		initializeView();
		 * 
		 ************************************************************* */
		
		private void initializeView() {
			btnClear = new Button( TEXT_CONTROL_CLEAR );
			btnClear.setDisable( true );
			btnClear.setFont( FONT_CONTROL );
			btnClear.setOnAction( this );
			btnClear.setPrefSize( 80, 25 );
			btnConnect = new Button( TEXT_CONTROL_CONNECT );
			btnConnect.setFont( FONT_CONTROL );
			btnConnect.setOnAction( this );
			btnConnect.setPrefSize( 80, 25 );
			btnPost = new Button( TEXT_CONTROL_POST );
			btnPost.setDisable( true );
			btnPost.setFont( FONT_CONTROL );
			btnPost.setOnAction( this );
			btnPost.setPrefSize( 80, 25 );
			hbxChat = new HBox();
			hbxConnect = new HBox();
			hbxMessage = new HBox();
			labIPaddress = new Label( TEXT_LABEL_IP );
			labIPaddress.setAlignment( Pos.BASELINE_LEFT );
			labIPaddress.setFont( FONT_LABEL );
			labIPaddress.setPrefSize( 75, 25 );
			labIPab = new Label( "." );
			labIPab.setAlignment( Pos.BASELINE_CENTER );
			labIPab.setFont( FONT_LABEL );
			labIPab.setPrefSize( 10, 25 );
			labIPbc = new Label( "." );
			labIPbc.setAlignment( Pos.BASELINE_CENTER );
			labIPbc.setFont( FONT_LABEL );
			labIPbc.setPrefSize( 10, 25 );
			labIPcd = new Label( "." );
			labIPcd.setAlignment( Pos.BASELINE_CENTER );
			labIPcd.setFont( FONT_LABEL );
			labIPcd.setPrefSize( 10, 25 );
			labPort = new Label( TEXT_LABEL_PORT );
			labPort.setAlignment( Pos.BASELINE_LEFT );
			labPort.setFont( FONT_LABEL );
			labPort.setPrefSize( 35, 25 );
			mnuAbout = new Menu( TEXT_CONTROL_ABOUT );
			mbrMain = new MenuBar();
			mbrMain.setPrefSize( VIEW_MAIN_WIDTH, 25 );
			mbrMain.setNodeOrientation( NodeOrientation.RIGHT_TO_LEFT );
			sepChat = new Separator();
			sepChat.setPrefSize( 10,  25 );
			sepChat.setVisible( false );
			sepIP = new Separator();
			sepIP.setPrefSize( 30, 25 );
			sepIP.setVisible( false );
			sepLine = new Separator();
			sepLine.setPrefSize( 10,  25 );
			sepLine.setVisible( true );
			sepMessage = new Separator();
			sepMessage.setPrefSize( 10,  25 );
			sepMessage.setVisible( false );
			sepPort = new Separator();
			sepPort.setPrefSize( 10,  25 );
			sepPort.setVisible( false );
			sepPost = new Separator();
			sepPost.setPrefSize( 80,  5 );
			sepPost.setVisible( false );
			txaChat = new TextArea();
			txaChat.setDisable( true );
			txaChat.setEditable( false );
			txaChat.setFont( FONT_BOARD );
			txaChat.setPrefSize( 405, 150 );
			txaClient = new TextArea();
			txaClient.setDisable( true );
			txaClient.setEditable( false );
			txaClient.setFont( FONT_BOARD );
			txaClient.setPrefSize( 80, 150 );
			txaMessage = new TextArea();
			txaMessage.setDisable( true );
			txaMessage.setFont( FONT_BOARD );
			txaMessage.setPrefSize( 405, 50 );
			txfIPa = new TextField();
			txfIPa.setAlignment( Pos.CENTER );
			txfIPa.setFont( FONT_CONTROL );
			txfIPa.setPrefSize( 45, 25 );
			txfIPb = new TextField();
			txfIPb.setAlignment( Pos.CENTER );
			txfIPb.setFont( FONT_CONTROL );
			txfIPb.setPrefSize( 45, 25 );
			txfIPc = new TextField();
			txfIPc.setAlignment( Pos.CENTER );
			txfIPc.setFont( FONT_CONTROL );
			txfIPc.setPrefSize( 45, 25 );
			txfIPd = new TextField();
			txfIPd.setAlignment( Pos.CENTER );
			txfIPd.setFont( FONT_CONTROL );
			txfIPd.setPrefSize( 45, 25 );
			txfPort = new TextField();
			txfPort.setAlignment( Pos.CENTER );
			txfPort.setFont( FONT_CONTROL );
			txfPort.setPrefSize( 55, 25 );
			tpnChat = new TitledPane();
			tpnChat.setCollapsible( false );
			tpnChat.setFont( FONT_CONTROL );
			tpnChat.setPadding( new Insets( 5, 5, 5, 5 ));
			tpnChat.setText( TEXT_LABEL_MESSAGE );
			tpnConnect = new TitledPane();
			tpnConnect.setCollapsible( false );
			tpnConnect.setFont( FONT_CONTROL );
			tpnConnect.setPadding( new Insets( 10, 5, 5, 5 ));
			tpnConnect.setText( TEXT_LABEL_CONNECT );
			vbxBackground = new VBox();
			vbxChat = new VBox();
			vbxInteract = new VBox();
		}
		
		/* *************************************************************
		 * 
		 * Determines the structure and position of the GUI elements.
		 * 
		 * Use:
		 * 		layoutView();
		 * 
		 ************************************************************* */
		
		private void layoutView() {
			hbxConnect.getChildren().add( labIPaddress );
			hbxConnect.getChildren().add( txfIPa );
			hbxConnect.getChildren().add( labIPab );
			hbxConnect.getChildren().add( txfIPb );
			hbxConnect.getChildren().add( labIPbc );
			hbxConnect.getChildren().add( txfIPc );
			hbxConnect.getChildren().add( labIPcd );
			hbxConnect.getChildren().add( txfIPd );
			hbxConnect.getChildren().add( sepIP );
			hbxConnect.getChildren().add( labPort );
			hbxConnect.getChildren().add( txfPort );
			hbxConnect.getChildren().add( sepPort );
			hbxConnect.getChildren().add( btnConnect );
			tpnConnect.setContent( hbxConnect );
			hbxChat.getChildren().add( txaChat );
			hbxChat.getChildren().add( sepChat );
			hbxChat.getChildren().add( txaClient );
			mbrMain.getMenus().add( mnuAbout );
			vbxInteract.getChildren().add( btnPost );
			vbxInteract.getChildren().add( sepPost );
			vbxInteract.getChildren().add( btnClear );
			hbxMessage.getChildren().add( txaMessage );
			hbxMessage.getChildren().add( sepMessage );
			hbxMessage.getChildren().add( vbxInteract );
			vbxChat.getChildren().add( hbxChat );
			vbxChat.getChildren().add( sepLine );
			vbxChat.getChildren().add( hbxMessage );
			tpnChat.setContent( vbxChat );
			vbxBackground.getChildren().add( mbrMain );
			vbxBackground.getChildren().add( tpnConnect );
			vbxBackground.getChildren().add( tpnChat );
		}
		
		/* *************************************************************
		 * 
		 * Sends a request from this client to the server connected to
		 * it through its TCP connection.
		 * 
		 * Parameters:
		 *		command (int)
		 *			A single-digit integer between 0 and 5, where each
		 *			number corresponds to a specific command. 
		 *
		 * Use:
		 * 		request( command );
		 * 
		 ************************************************************* */
		
		private void request( int command ) {
			if ( out != null && in != null ) {
				String response = "";
				if ( command == CMD_NULL ) {
					out.println( command );
					/*
					try {
						response = in.readLine();
						Scanner input = new Scanner( response );
						if ( input.hasNext() && input.nextInt() == CMD_UPDATE ) {
							updateClients( input.nextLine() );
						}
						input.close();
					} catch ( IOException e ) {
						e.printStackTrace();
					}
					*/
				}
				else if ( command == CMD_CONNECT ) {
					out.println( command + " " + username );
					try {
						response = in.readLine() + "\n";
						txaClient.appendText( response );
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				} else if ( command == CMD_DISCONNECT ) {
					out.println( command + " " + username );
				} else if ( command == CMD_CLEAR ) {
					out.println( command + " " + username );
					txaChat.clear();
				} else if ( command == CMD_POST ) {
					out.println( command + " " + username + " " + txaMessage.getText() );
					try {
						response = in.readLine() + "\n";
						txaChat.appendText( response );
						txaMessage.clear();
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				}
			}
		}
		
		/* *************************************************************
		 * 
		 * Updates this client's list of server-connected users to
		 * display on the GUI.
		 * 
		 * Parameters:
		 * 		clients (String)
		 * 			A space-delimited list of clients connected to the
		 * 			server.
		 * 
		 * Use:
		 * 		updateClients( clients );
		 * 
		 ************************************************************* */

		private void updateClients( String clients ) {
			if ( clients != "" ) {
				Scanner input = new Scanner( clients );
				String client;
				txaClient.clear();
				while ( input.hasNext() ) {
					client = input.next();
					txaClient.appendText( client + "\n" );
					
				}
				input.close();
			}
		}
		
		/* *************************************************************
		 * 
		 * Controls the client's responses to events related to the GUI.
		 * 
		 * Required for JavaFX.
		 * 
		 ************************************************************* */
		
		@Override
		public void handle( ActionEvent event ) {
			if ( event.getSource() == btnConnect ) {
				if ( socket == null || socket.isClosed() ) {
					String address = getAddress();
					int port = getPort();
					if ( address != "" && port != -1 ) {
						connect( getAddress(), getPort() );
						request( CMD_CONNECT );
					} else {
						System.err.println( ERROR_NO_ADDRESS_PORT );
					}
				} else {
					request( CMD_DISCONNECT );
					disconnect();
				}
			} else if ( event.getSource() == btnPost ) {
				request( CMD_POST );
			} else if ( event.getSource() == btnClear ) {
				request( CMD_CLEAR );
			}
			// request( CMD_NULL );
		}
		
		/* *************************************************************
		 * 
		 * Starts the client's GUI.
		 * 
		 * Required for JavaFX.
		 * 
		 ************************************************************* */
		
		@Override
		public void start( Stage main ) {
			initializeView();
			layoutView();
			main.setResizable( false );
			main.setScene( new Scene( vbxBackground, VIEW_MAIN_WIDTH, VIEW_MAIN_HEIGHT ));
			main.setTitle( "Message Board" );
			main.show();
		}
		
	}

	/* *************************************************************
	 * 
	 * Launches the client.
	 * 
	 * Parameters:
	 * 		args (String array)
	 * 			Unused.
	 * 
	 ************************************************************* */
	
	public static void main( String[] args ) {
		Application.launch( View.class, args );
	}

}
