//Server fot tic-tac-toe game
import java.awt.BorderLayout;
import java.awt.Color;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.io.IOException;
 import java.util.Formatter;
 import java.util.Scanner;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.Condition;
 import javax.swing.JFrame;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;


 public class Server extends JFrame
 {

   private  int x,y,z;
   private int k;
   //board for tic-tac-toe game
   private String[] board = new String[ 9 ]; 
   //to build output area
   private JTextArea output; 
   //intialize player
   private Player[] p;
   //socket server
   private ServerSocket server;
   private int currentPlayer; 
   //player x = 0, player y = 1
   private final static int PLAYERX = 0; 
   private final static int PLAYERO = 1;
   //mark the player x , o 
   private final static String[] MARKS = { "X", "O" };
   //run tic-tac-toe game
   private ExecutorService runGame;
   //lock tic-tac-toe game
   private Lock gameLocking;
   //waiting for the other player
   private Condition otherPlayerConnected; 
   //waiting for player turn
   private Condition otherPlayerTurn; 
          public Server()
     {
      super( "Tic-Tac-Toe Server" );        
       runGame = Executors.newFixedThreadPool( 2 );
       gameLocking = new ReentrantLock(); 
             otherPlayerConnected = gameLocking.newCondition();
             otherPlayerTurn = gameLocking.newCondition();

       for ( int i = 0; i < 9; i++ )
          board[ i ] = new String( "" ); 
    p = new Player[ 2 ]; 
       currentPlayer = PLAYERX;
       try
       {
          server = new ServerSocket( 12345, 2 );
       } 
       catch ( IOException ioException )
       {
          ioException.printStackTrace();
          System.exit( 1 );
       } 

//intialize board locations
       board[0]="a";
       board[1]="b";
       board[2]="c";
       board[3]="d";
       board[4]="e";
       board[5]="f";
       board[6]="g";
       board[7]="h";
       board[8]="i";    
       output = new JTextArea();
       add( output, BorderLayout.CENTER );
       output.setText( "Hello, The Server is waiting for clients connections\n" );
       //size of the tic-tac-toe server board
       setSize( 400, 400 ); 
       setVisible( true ); 
   }  
//how the player execute from the game
    public void execute(){
        for ( int i = 0; i < p.length; i++ ){
          try {
             p[ i ] = new Player( server.accept(), i );           
            runGame.execute( p[ i ] );
          } 
          catch ( IOException ioException ){
             ioException.printStackTrace();
             System.exit( 1 );
          } 
       } 
       gameLocking.lock();
       try{
          p[ PLAYERX ].setSuspended( false );       
          otherPlayerConnected.signal(); 
        } 
       //unlock the game after playerx moving
       finally{
          gameLocking.unlock();
        } 
    } 
//to display the message to the players with RMI
        private void showMessage( final String message ){
            SwingUtilities.invokeLater(
               new Runnable(){
                   public void run(){
                       output.append( message ); 
             } 
          } 
       ); 
    } 

    // to know if the player move is true
    public boolean validateAndMove( int loc, int player ){
        //to wait for the other player to turn
       while ( player != currentPlayer ){
          gameLocking.lock(); 
         //waiting for the player's turn
          try{
            otherPlayerTurn.await(); 
          } 
         catch ( InterruptedException exception ){
             exception.printStackTrace();
         } 
          //to unlock tic-tac-toe game after waiting
         finally{
             gameLocking.unlock(); 
         } }  
       // if all locations not occupied
      if ( !isOccupied( loc ) ){
           //if game is over return false
        if(gameOver()){
          return false;
        } 
        //mark the current player on the board
        board[ loc ] = MARKS[ currentPlayer ]; 
        p[currentPlayer].playermovement(loc);
        // to change from the current player to the other player      
        currentPlayer = ( currentPlayer + 1 ) % 2; 
        //new current player know that move occurred
         p[ currentPlayer ].otherPlayerMoved( loc );
         gameLocking.lock();
         //notifiy the other player to play
         try {
            otherPlayerTurn.signal();
          }
         //unlock the game after notifying
          finally {
           if(!gameOver())
              gameLocking.unlock(); 
           else{ 
                 p[currentPlayer].output.format("The Game is over, Player %s won the game\n",board[x]);
                 p[currentPlayer].output.flush();
                gameLocking.lock();
           }          
          } 
           return true; 
      } else
        return false;
    }
    //function to know if the location is occupied by any player
    public boolean isOccupied( int loc ){
        //if location us occupied by any player x or player o-----> return true
        if ( board[ loc ].equals( MARKS[ PLAYERX ] ) ||
          board [ loc ].equals( MARKS[ PLAYERO ] ) )
          return true; 
        else
            return false;
    }
    //function to know if the game is over or not
    public boolean gameOver(){
        //intialize the won conditions
    x=0;y=1;z=2;
    if(board[x].equals(board[y])&&board[x].equals(board[z]))
       return true;
    x=0;y=3;z=6;
    if(board[x].equals(board[y])&&board[x].equals(board[z]))
       return true;
    x=0;y=4;z=8;
    if(board[x].equals(board[y])&&board[x].equals(board[z]))
       return true;
    x=1;y=4;z=7;
    if(board[x].equals(board[y])&&board[x].equals(board[z]))
       return true;
    x=2;y=5;z=8;
    if(board[x].equals(board[y])&&board[x].equals(board[z]))
       return true;
    x=3;y=4;z=5;
    if(board[x].equals(board[y])&&board[x].equals(board[z]))
       return true;
    x=6;y=7;z=8;
    if(board[x].equals(board[y])&&board[x].equals(board[z]))
       return true;
    x=2;y=4;z=6;
    if(board[x].equals(board[y])&&board[x].equals(board[z]))
       return true;
return false;
} 

    // to manages each Player as a runnable
    private class Player implements Runnable{
       private Socket connection; 
       private Scanner input; 
       private Formatter output;
       private int pNum; 
       private String mark; 
       private boolean suspended = true;
        public Player( Socket socket, int number ){
       pNum = number;
       mark = MARKS[ pNum ]; 
      connection = socket; 
       try {
           input = new Scanner( connection.getInputStream() );    
           output = new Formatter( connection.getOutputStream() );
       } 
       catch ( IOException ioException ){ 
          ioException.printStackTrace();
          System.exit( 1 );
       } 
    }
        
    public void playermovement(int loc){
       output.format("Your move is done"+"\n");
       output.format("%d\n",loc);
       output.flush();
}
      // send message that other player moved
      public void otherPlayerMoved( int loc ){
        output.format( "Opponent moved\n" );                       
        output.format( "%d\n", loc );
        output.flush();                            
      } 

      public void run()
      {
        // send client its mark (X or O), process messages from client
       try
       {
           showMessage( "Player " + mark + " connected\n" );
           output.format( "%s\n", mark ); // send player's mark
           output.flush(); // flush output                     

         if ( pNum == PLAYERX ){
            output.format( "%s\n%s", "Player X is connected",
            "Waiting for the other player to connect\n" );             
            output.flush();              
           gameLocking.lock(); 
            try{
                //wait for the player o
             while( suspended ){
                otherPlayerConnected.await(); 
              }
           } 
            catch ( InterruptedException exception ){
             exception.printStackTrace();
            }
            //unlock tic-tac-toe game after the player o connected
            finally{
              gameLocking.unlock(); 
            }
              output.format( "The Other player connected. Your move is.\n" );
              output.flush();                        
            } 
            else{
               output.format( "Player O connected, please wait\n" );
               output.flush();                       
            } 

                // if tic-tac-toe game is not finished
                while ( !gameOver () ){
                  int loc = 0;
                  //return the movement location
                   if ( input.hasNext() )
                      loc = input.nextInt(); 
                  if ( validateAndMove( loc, pNum ) )
                  {
                    showMessage( "\nlocation: " +loc );
                    output.format( "Validate movement is %d\n",loc);
                    output.flush();                     
                  }  
                  else {
                    output.format( "Invalid move,please try again in another location\n" );
                    output.flush ();            
                  }  
                       if(gameOver()){ 
                 output.format("The Game over  %s is won\n",board[x]);
                 output.flush();
                     } 
            }
}             finally{
           //close the client connection and logout
                try{
                  connection.close(); 
               } 
                catch ( IOException ioException ){
                  ioException.printStackTrace();
                  System.exit( 1 );
                }             }
          }
          
          public void setSuspended( boolean status ){
            suspended = status; 
          } 
    } 
}