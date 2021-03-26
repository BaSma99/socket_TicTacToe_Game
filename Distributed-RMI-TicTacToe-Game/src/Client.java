  import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.net.Socket;
 import java.net.InetAddress;
 import java.io.IOException;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import java.util.Formatter;
 import java.util.Scanner;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;

public class Client extends JFrame implements Runnable { 
   private JTextField field; 
   private JTextArea display;
    private JPanel Panel; 
    private JPanel panel2; 
    private Square board[][];
    private Square current;
    private Socket connection; 
    private Scanner input;
    private Formatter output;
    private String Host; 
    private String myMark; 
    private boolean myTurn;
    private final String XMARK = "X"; 
    private final String OMARK = "O"; 

    public Client( String host )
    {
       Host = host; 
       display = new JTextArea( 4, 30 );
       display.setEditable( false );
       add( new JScrollPane( display ), BorderLayout.SOUTH );
       Panel = new JPanel();
       //draw the tic-tac-toe game
       Panel.setLayout( new GridLayout( 3, 3, 0, 0 ) );
       board = new Square[ 3 ][ 3 ];      
       for ( int row = 0; row < board.length; row++ ){
                for ( int col = 0; col < board[ row ].length; col++ ){
             board[ row ][ col ] = new Square(  " ",row * 3 + col);
             Panel.add( board[ row ][ col ] ); 
           } 
       } 
       field = new JTextField();
       field.setEditable( false ); 
       add( field, BorderLayout.NORTH ); 

      panel2 = new JPanel();  
       panel2.add( Panel, BorderLayout.CENTER );
       add( panel2, BorderLayout.CENTER ); 

       setSize( 300, 225 ); 
       setVisible( true );  

       startClient(); 
    } 

   
    public void startClient() 
    { 
       try  
      { 
          // make connection to server 
          connection = new Socket(                           
             InetAddress.getLocalHost( ),12345);

          // get streams for input and output 
         input = new Scanner( connection.getInputStream() );    
          output = new Formatter( connection.getOutputStream() );
       }  
      catch ( IOException ioException ) 
      { 
          ioException.printStackTrace(); 
       }  

       
       ExecutorService worker = Executors.newFixedThreadPool( 1 ); 
       worker.execute( this ); 
    }  
    public void run() { 
       myMark = input.nextLine(); 
       SwingUtilities.invokeLater( 
          new Runnable() { 
            public void run() {
               field.setText( "You are the player \"" + myMark + "\"" ); 
            } 
         }  
       ); 
      myTurn = ( myMark.equals( XMARK ) ); 
        while ( true ) {
           if ( input.hasNextLine() )
               sendMsg( input.nextLine() );
            } 
    } 
     //function to manage sending messages between the two clients players
    private void sendMsg( String message ){
           if(message.equals("Game over %s won"))
             {
                showMsg(message+"\n" );
             }    
            else if ( message.equals( "Your move is done" ) )
            {
            showMsg( message+"\n" );
            int i=input.nextInt();
            input.nextLine();
            setMark( board[i/3][i%3], myMark ); 
       } 
          else if(message.equals("Valid move in "))
               showMsg(message);

         else if ( message.equals( "Invalid move, try again" ) )
         {
          showMsg( message + "\n" ); 
          myTurn = true; 
       } 
       else if ( message.equals( "Opponent moved" ) ){
          int location = input.nextInt(); 
          input.nextLine(); 
          int row = location / 3; 
          int col = location % 3; 
          setMark( board[ row ][ col ],
             ( myMark.equals( XMARK ) ? OMARK : XMARK ) ); 
          showMsg( "Opponent moved. Your turn.\n" );
          myTurn = true; 
       } 
     else
          showMsg( message + "\n" );
    } 
//function to show the messages of the two players
    private void showMsg( final String messageToDisplay ){
      SwingUtilities.invokeLater(
          new Runnable(){
            public void run(){
                display.append( messageToDisplay ); 
             }
          } 
        ); 
    } 
    //to set the player mark into the square of the game
    private void setMark( final Square sqMark, final String mark ){
       SwingUtilities.invokeLater(
          new Runnable(){
              public void run(){
                 sqMark.setMark( mark ); 
              } 
          } 
        );
   } 
//to click on the square of the game
    public void sendClickedSquare( int location ) { 
       if ( myTurn ) { 
          output.format( "%d\n", location );
          output.flush();                                              
          myTurn = false; 
       }  
    } 
    //to intialize the current square of playing
    public void setSquare( Square sq ) { 
       current = sq; 
    }  
    //square GUI
    private class Square extends JPanel { 
       private String mark; 
       private int loc; 
       public Square( String sqMark, int sqLoc ) { 
          mark = sqMark;  
          loc = sqLoc;  
          //to listen to the mouse event on the screen
          addMouseListener( 
             new MouseAdapter() { 
               public void mouseReleased( MouseEvent e ) {
                 current=Square.this; 
                sendClickedSquare( getSqLoc() ); 
               } 
             }  
           );  
       }      
    public Dimension getPreferredSize() { 
       return new Dimension( 35, 35 );  
    } 
    public Dimension getMinimumSize() { 
       return getPreferredSize(); 
    }  
    public void setMark( String newMark ) { 
       mark = newMark;
       repaint(); 
    }
    public int getSqLoc() { 
       return loc; 
    }

    public void paintComponent( Graphics g ) 
    { 
       super.paintComponent( g ); 

       g.drawRect( 0, 0, 29, 29 ); 
       g.drawString( mark, 11, 20 );  
    } 
   } 
} 
