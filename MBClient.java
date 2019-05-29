import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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

//				REQUEST				RESPONSE					LOG							MESSAGE (time, user, text)
// CONNECT:		<1> <user>			=> <user>					=>	<00:00:00> <1> <user>	=>
// DISCONNECT:	<2> <user>			=>							=>	<00:00:00> <2> <user>	=>
// CLEAR:		<3> <user>			=> 							=>	<00:00:00> <3> <user>	=>
// POST:		<4> <user> <text>	=> <time> <user> <text>		=>	<00:00:00> <4> <user>	=>	<00:00:00> <user> <text>

// list of clients ???
// displayChat(); ???
// displayClients(); ???
// add status codes?
// pin and unpin?

public class MBClient {
	
	public static final Font FONT_BOARD = Font.font( "Agency FB", 12 );
	public static final Font FONT_CONTROL = Font.font( "Agency FB", 14);
	public static final Font FONT_LABEL = Font.font( "Verdana", 12 );
	public static final String TEXT_CONTROL_ABOUT = "About";
	public static final String TEXT_CONTROL_CLEAR = "CLEAR";
	public static final String TEXT_CONTROL_CONNECT = "CONNECT";
	public static final String TEXT_CONTROL_DISCONNECT = "DISCONNECT";
	public static final String TEXT_CONTROL_POST = "POST";
	public static final String TEXT_LABEL_CONNECT = "CONNECTION";
	public static final String TEXT_LABEL_IP = "IP Address:";
	public static final String TEXT_LABEL_MESSAGE = "MESSAGE BOARD";
	public static final String TEXT_LABEL_PORT = "Port:";
	public static final int CMD_NULL = 0;
	public static final int CMD_CONNECT = 1;
	public static final int CMD_DISCONNECT = 2;
	public static final int CMD_CLEAR = 3;
	public static final int CMD_POST = 4;
	public static final int IP_OCTET_MAX = 255;
	public static final int IP_OCTET_MIN = 0;
	public static final int PORT_MAX = 65535;
	public static final int PORT_MIN = 0;
	public static final int VIEW_MAIN_HEIGHT = 410;
	public static final int VIEW_MAIN_WIDTH = 510;
	
	public static class View extends Application implements EventHandler<ActionEvent> {
		
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
		
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
		private String username;
		
		public View() {
			this.in = null;
			this.out = null;
			this.socket = null;
			this.username = "user1"; // ???
		}

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
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		
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
						// ERROR
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
		}
		
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
		
		private void request( int command ) {
			if ( out != null && in != null ) {
				if ( command == CMD_CONNECT ) {
					out.println( command + " " + username );
					try {
						String response = in.readLine() + "\n";
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
						String response = in.readLine() + "\n";
						txaChat.appendText( response );
						txaMessage.clear();
					} catch ( IOException e ) {
						e.printStackTrace();
					}
				}
			}
		}

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

	public static void main( String[] args ) {
		Application.launch( View.class, args );
	}

}
