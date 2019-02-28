/* [SimpleWebServerDemo.java]
 * Description: This is an example of a web server.
 * The program  waits for a client and accepts a message. 
 * It then responds to the message and quits.
 * This server demonstrates how to employ multithreading to accepts multiple clients
 * @author Chris
 * @version 1.0a
 */

//imports for network communication
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Scanner;
import java.sql.*;


class SimpleWebServerDemo {
  
  ServerSocket serverSock;// server socket for connection
//  static int page = -1; //wait this is supposed to work for more than one, so i need to not do this
  static Boolean running = true;  // controls if the server is accepting clients
  static Boolean accepting = true;
  /** Main
    * @param args parameters from command line
    */
  public static void main(String[] args) {
    createDatabase(); //for SQL
    new SimpleWebServerDemo().go(); //start the server
  }
  
  /** Go
    * Starts the server
    */
  public void go() {
    System.out.println("Waiting for a client connection..");
    
    Socket client = null;//hold the client connection
    
    try {
      serverSock = new ServerSocket(80);  //assigns an port to the server
      
      // serverSock.setSoTimeout(15000);  //5 second timeout
      
      while(accepting) {  //this loops to accept multiple clients
        client = serverSock.accept();  //wait for connection
        System.out.println("Client connected");
        //Note: you might want to keep references to all clients if you plan to broadcast messages
        //Also: Queues are good tools to buffer incoming/outgoing messages
        Thread t = new Thread(new ConnectionHandler(client)); //create a thread for the new client and pass in the socket
        t.start(); //start the new thread
      }
    }catch(Exception e) {
      // System.out.println("Error accepting connection");
      //close all and quit
      
       try {
       client.close();
       }catch (Exception e1) { 
       System.out.println("Failed to close socket");
       }
       System.exit(-1);
       
    }
  }
  
  //***** Inner class - thread for client connection
  class ConnectionHandler implements Runnable {
    private DataOutputStream output; //assign printwriter to network stream
    private BufferedReader input; //Stream for network input
    private Socket client;  //keeps track of the client socket
    private boolean running;
    
    //GUI Stuff
    private JButton sendButton, htmlButton;
    private JTextField typeField;
    private JTextArea msgArea; 
    private JPanel southPanel;
    
    
    /*How the pages thing works
     * -1 = nothing, no such page associated
     * 0 = homepage, with the log-in/register buttons
     * 1 = createProfile page
     * 2 = profile page
     * 3 = profileEdit page
     **/
    
    /* ConnectionHandler
     * Constructor
     * @param the socket belonging to this client connection
     */    
    ConnectionHandler(Socket s) { 
      this.client = s;  //constructor assigns client to this    
      try {  //assign all connections to client
        this.output =  new DataOutputStream(client.getOutputStream());
        InputStreamReader stream = new InputStreamReader(client.getInputStream());
        this.input = new BufferedReader(stream);
      }catch(IOException e) {
        e.printStackTrace();        
      }            
      running=true;
//      initGUI(); //start the GUI
//      sendHTML("homepage.html");
    } //end of constructor
    
    
    public void initGUI() {
      JFrame window = new JFrame("Web Server");
      southPanel = new JPanel();
      southPanel.setLayout(new GridLayout(2,0));
      
      sendButton = new JButton("SEND HTML W/IMAGE");
      htmlButton = new JButton("Homepage");
      
      sendButton.addActionListener(new SendButtonListener());
      htmlButton.addActionListener(new htmlButtonListener());
      
      JLabel errorLabel = new JLabel("");
      
      typeField = new JTextField(10);
      
      msgArea = new JTextArea();
      
      southPanel.add(typeField);
      southPanel.add(sendButton);
      southPanel.add(errorLabel);
      southPanel.add(htmlButton);
      
      window.add(BorderLayout.CENTER,msgArea);
      window.add(BorderLayout.SOUTH,southPanel);
      
      window.setSize(400,400);
      window.setVisible(true);
      
      // call a method that connects to the server 
      // after connecting loop and keep appending[.append()] to the JTextArea
    }
    
    
    /* run
     * executed on start of thread
     */
    public void run() {
      int page = -1;
      //Get a message from the client
      String msg="";
      String username = "";
      boolean ready = false;
      String postMessage = "";
      //Get a message from the client
      while(running) {  // loop until a message is received  
        
        try {
          if (input.ready()) { //check for an incoming messge
            System.out.println("Break 1");
            String URL = "";
            int gotURL = 0;
            int contentLength = 0;
            while (input.ready()) {
              msg = input.readLine();  //get a message from the client
              System.out.println(msg);
              if (gotURL < 1) {
                URL = msg;
                gotURL++;
              }
              if (URL.substring(0,4).equals("POST")) {
                if (msg.indexOf("Content-Length") != -1) {
                  contentLength = Integer.parseInt(msg.substring(msg.indexOf("Content-Length") + 16));
                }
              }
              
              if (msg.equals("")) {
                if (URL.length() > 3) {
                  if (URL.substring(0,4).equals("POST")) {
                    System.out.println("reached**************************************");
                    //the buffer thing goes here
                    while ((msg = "" + (char)input.read()) != null) {
                      postMessage = postMessage + msg;
                      System.out.println(input.read());
                    }
                    System.out.println(postMessage);
//              System.out.println("reached");
                    
                  }
                }
              }
//              msgArea.append(msg + "\n");
            }
            
            /*weird bulls here */
            System.out.println("Break 2");
            System.out.println("current message: " + URL);
            if (msg.length() > 3) {
              if (msg.substring(0,4).equals("POST")) {
                System.out.println("reached**************************************");
                msg= input.readLine();
                System.out.println(msg);
//              System.out.println("reached");
                
              }
            }
            
            //end of weird bullshit
            System.out.println("URL: " + URL);
            URL = URL.substring(URL.indexOf("/") + 1, URL.indexOf(" HTTP"));
            System.out.println(URL);
           
            System.out.println("Break 1");
            
            if (URL.equals("")) {
              sendHTML("homepage.html");
            }
            if (URL.equals("favicon.ico")) {
              sendHTML(URL);
              ready = false;
            }
            if (URL.equals("meme.jpg")) {
              sendHTML(URL);
              ready = false;
            }
            System.out.println("URL: " + URL);
            System.out.println("READY: " + ready);
            if (ready == true) { //dont go through the first time otherwise it tries to run this on the homepage with nothing in the url
              if (URL.substring(0,8).equals("sign-in?")) {
                page = 0;
                username = URL.substring(URL.indexOf("username=") + 9, URL.indexOf("&pass"));
              } else if(URL.substring(0,8).equals("profile?")) {
                page = 2;
              } else if(URL.substring(0,8).equals("log-out?")) {
                sendHTML("homepage.html");
                page = -1;
              } else if (URL.substring(0,9).equals("register?")) {
                page = 1;
              } else if (URL.substring(0,12).equals("profileEdit?")) {
                page = 3;
              }
            }
            System.out.println("PAGE: "+ page);
            ready = true; // the second time around, we can do this thing
            
            if (page == 0) { //if at the homepage, so therefore the log-in/register button is being pressed
                String password = URL.substring(URL.indexOf("password=") + 9, URL.indexOf("&opti"));
                int option = Integer.parseInt(URL.substring(URL.indexOf("option=") + 7));
                
                System.out.println(username+ "\n" +password + "\n" + option);
                
                if (option == 0) { //attempt log in and display profile (what if wrong user/pass)
                  Boolean authenticated = authenticate(username, password);
                  if (authenticated == true) {
                    sendProfile("profile.html", username); 
                  } else if (authenticated == false) {
                    sendHTML("nouserpass.html");
                  }
                } else if (option == 1) { //register the person
                  Boolean alreadyReg = checkUsername(username);
                  if (alreadyReg == false) {
                    sendHTML("alreadyReg.html");
                  } else if (alreadyReg == true) {
                    register(username, password);
                    sendHTML("createProfile.html");
                  }
                }
              
            }
            if (page == 1) { //if at the create profile page
              String name = URL.substring(URL.indexOf("name=") + 5, URL.indexOf("&color"));
              String backgroundColor = URL.substring(URL.indexOf("color=") + 6, URL.indexOf("&description"));
              String description = URL.substring(URL.indexOf("description=") + 12);
              insert(username, name, backgroundColor, description);
              sendProfile("profile.html", username);
            }
            if (page == 2) {
              sendProfile("profileEdit.html", username);
            }
            
            if (page == 3) {
              if (URL.indexOf("&color") != -1) {
                String name = URL.substring(URL.indexOf("name=") + 5, URL.indexOf("&color"));
                String backgroundColor = URL.substring(URL.indexOf("color=") + 6, URL.indexOf("&description"));
                String description = URL.substring(URL.indexOf("description=") + 12);
                System.out.println(name);
                System.out.println(backgroundColor);
                System.out.println(description);
                update(username, name, backgroundColor, description);
                sendProfile("profile.html", username);
              } else {
                sendProfile("profileEdit.html", username);
              }
            }
            System.out.println("Break 4");
          }
          
          
        }catch (IOException e) {
          System.out.println("Failed to receive msg from the client");
          e.printStackTrace();
        }
      }
      
      //close the socket
      /*
       try {
       input.close();
       output.close();
       client.close();
       }catch (Exception e) {
       System.out.println("Failed to close socket");
       }
       */
    } // end of run()
    
    
    //****** Inner Classes for Action Listeners ****
    
    //To complete this you will need to add action listeners to both buttons
    // clear - clears the textfield
    // send - send msg to server (also flush), then clear the JTextField
    class SendButtonListener implements ActionListener { 
      public void actionPerformed(ActionEvent event)  {
        //smth here idk
      }
    }
    
    class htmlButtonListener implements ActionListener {
      public void actionPerformed(ActionEvent event)  {
        
        sendHTML("homepage.html");
//        page = 0;
        
        //do not close the server or whatever
        /**
         //       running=false; //end the server
         try {
         //        input.close();
         //        output.close();
         //        client.close();
         }catch (Exception e) { 
         System.out.println("Failed to close socket");
         }
         */
        
      }
      
    }
    public boolean authenticate (String username, String password) {
      File userpass = new File("userpass.txt");
      boolean weIn = false;
      try {
        
        Scanner sc = new Scanner(userpass);
        while (sc.hasNextLine()) {
          String user = sc.nextLine();
          if (user.equals(username)) {
            String pass = sc.nextLine();
            if (pass.equals(password)) {
              return true;
            }
          }
        }
        sc.close();
      }
      catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      
      return false;
      
    }
    
    public boolean checkUsername (String username) {
      username = username.toLowerCase();
      File userpass = new File("userpass.txt");
      try {
        
        Scanner sc = new Scanner(userpass);
        while (sc.hasNextLine()) {
          String user = sc.nextLine().toLowerCase();
          if (user.equals(username)) {
            return false;
          }
        }
        sc.close();
      }
      catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      
      return true;
      
    }
    
    
    /*method for registering new people to the server or whatever */
    public void register (String user, String pass) {
      File userpass = new File("userpass.txt");
      
      try {
        
        PrintWriter print = new PrintWriter(new FileOutputStream(userpass,true));
        print.println(user);
        print.println(pass);
        print.println("");
        print.close();
        System.out.println("successful registration");
      }
      
      catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
    //kinda useless method, but whatever
    public String removeSpaces(String input) {
      while (input.indexOf("+") != -1) {
        input = input.substring(0, input.indexOf("+")) + " " + input.substring(input.indexOf("+") +1);
      }
      return input;
    }
    
    
    public void sendProfile (String HTML, String username) {
      File original = new File(HTML);
      File webFile = new File("temp.html");
      
      //copy the original file to temp.html, but replace anything between % signs
      try {
        Scanner sc = new Scanner(original);
        PrintWriter print = new PrintWriter(new FileOutputStream(webFile));
        while (sc.hasNextLine()) {
          //check if % signs in a line and replace text
          String line = sc.nextLine();
          String editedLine = line;
          if (line.indexOf('%') != -1) { //if the %character exists within a line
            int first = line.indexOf('%');
            int second = line.substring(line.indexOf('%') + 1).indexOf('%') + first +1;
            String toReplace = line.substring(first + 1, second);
            if (toReplace.equals("NAME")) {
              //get name from SQL
              editedLine = line.substring(0, first) + select(username, "name") + line.substring(second + 1);
            } else if (toReplace.equals("COLOR")) {
              editedLine = line.substring(0, first) + select(username, "backgroundcolor") + line.substring(second + 1);
            } else if (toReplace.equals("DESCRIPTION")) {
              editedLine = line.substring(0, first) + select(username, "description") + line.substring(second + 1);
            }
//            editedLine = line.substring(0, first) + "NAME" + line.substring(second + 1);
          }
          //print edited line to the temp html file
//          System.out.println(editedLine);
          print.println(removeSpaces(editedLine));
        }
        print.close();
        sendHTML("temp.html");
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
    
    /*method for sending an HTML file (mostly for convenience) */
    public void sendHTML (String HTML) {
      
      //honestly, its probably easier to take the original file, write it to another file, and then change the %COLOR% crap there
      File webFile = new File(HTML);
      BufferedInputStream in = null;
      
      try {
        in = new BufferedInputStream(new FileInputStream(webFile));
        int data;
        
        System.out.println("File Size: " +webFile.length());
        byte[] d = new byte[(int)webFile.length()];
        
        output.writeBytes("HTTP/1.1 200 OK" + "\n");
        output.flush();
        
        output.writeBytes("Content-Type: text/html"+"\n");
        output.flush();
        output.writeBytes("Content-Length: " + webFile.length() + "\n\n");
        output.flush();
        
        String t="";
        
        
        in.read(d,0,(int)webFile.length());
        
        
        System.out.print(d[0]);
//        for (int i = 0; i < webFile.length(); i++) {
//          System.out.print(d[i]);
//        }
        System.out.println();
        System.out.println("read: " +webFile.length()+" bytes");
        output.write(d,0,d.length);
        System.out.println("sent: " +webFile.length()+" bytes");
        
        
        output.flush();
        
      } catch (IOException e) { 
        e.printStackTrace();}               
      
      
//      msgArea.append("SENT HTML REPONSE!"); 
      return;
    }
    
    
    
    
    
  } //end of inner class   
  
  public static void update(String username, String name, String backgroundcolor, String description) {
       Connection c = null;
   Statement stmt = null;
   
   try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
      c.setAutoCommit(false);
      System.out.println("Opened database successfully");

      stmt = c.createStatement();
      String sql1 = "UPDATE CUSTOM set NAME = '"+name+"' where USERNAME= '"+username+"';";
      String sql2= "UPDATE CUSTOM set BACKGROUNDCOLOR = '"+backgroundcolor+"' where USERNAME= '"+username+"';";
      String sql3 = "UPDATE CUSTOM set DESCRIPTION = '"+description+"' where USERNAME= '"+username+"';";
      stmt.executeUpdate(sql1);
      stmt.executeUpdate(sql2);
      stmt.executeUpdate(sql3);
      c.commit();
      
      stmt.close();
      c.close();
   } catch ( Exception e ) {
     System.err.println( e.getClass().getName() + ": " + e.getMessage() );
     System.exit(0);
   }
  }
  
  public static void createDatabase() {
    Connection c = null;
    Statement stmt = null;
    
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
      System.out.println("Opened database successfully");
      
      stmt = c.createStatement();
      String sql = "CREATE TABLE IF NOT EXISTS CUSTOM" +
        "(USERNAME TEXT PRIMARY KEY NOT NULL, " +
        " NAME TEXT NOT NULL, " + 
        " BACKGROUNDCOLOR TEXT NOT NULL, " + 
        " DESCRIPTION TEXT)";
      stmt.executeUpdate(sql);
      stmt.close();
      c.close();
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    System.out.println("Table created successfully");
  }
  public static void insert(String username, String name, String backgroundColor, String description ) {
    Connection c = null;
      Statement stmt = null;
      
      try {
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:test.db");
         c.setAutoCommit(false);
         System.out.println("Opened database successfully");

         stmt = c.createStatement();
         String sql = "INSERT INTO CUSTOM (USERNAME,NAME,BACKGROUNDCOLOR,DESCRIPTION) " +
                        "VALUES ('"+username+"', '"+name+"', '"+backgroundColor+"', '"+description+"');"; //does this work?************************************************
         stmt.executeUpdate(sql);

         stmt.close();
         c.commit();
         c.close();
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
//        System.exit(0);
      }
      System.out.println("Records created successfully");
  }
  public static String select(String username, String need) {
    String query = "";
    Connection c = null;
    Statement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
      c.setAutoCommit(false);
      System.out.println("Opened database successfully");
      
      stmt = c.createStatement();
//      ResultSet rs = stmt.executeQuery( "SELECT * FROM CUSTOM;" ); //change the star to smth im not sure yet
      ResultSet rs = stmt.executeQuery( "SELECT * FROM CUSTOM WHERE USERNAME = '"+username+"';"); 
      
      while ( rs.next() ) {
        String  name = rs.getString("name");
        String  backgroundcolor = rs.getString("backgroundcolor");
        String description = rs.getString("description");
        
        if (need.equals("name" )) {
          query = name;
        } else if (need.equals("backgroundcolor")) {
          query = backgroundcolor;
        } else if (need.equals("description")) {
          query = description;
        }
                   
        System.out.println( "USERNAME = " + username );
        System.out.println( "NAME = " + name );
        System.out.println( "BACKGROUNDCOLOR = " + backgroundcolor );
        System.out.println( "DESCRIPTION = " + description );
        System.out.println();
      }
      rs.close();
      stmt.close();
      c.close();
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    System.out.println("Operation done successfully");
    return query;
  }
  
} //end of SillyServer class