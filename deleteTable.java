import java.sql.*;

public class deleteTable {
  public static void main(String []args) {
    lookTable();
//    System.out.println(select("nice", "description"));
  }
  public static void killTable() {
    
    Connection c = null;
    Statement stmt = null;
    
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
      System.out.println("Opened database successfully");
      
      stmt = c.createStatement();
      String sql = "DROP TABLE CUSTOM";
      stmt.executeUpdate(sql);
      stmt.close();
      c.close();
      System.out.println("deleted");
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    
  }
  public static void lookTable () {

   Connection c = null;
   Statement stmt = null;
   try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
      c.setAutoCommit(false);
      System.out.println("Opened database successfully");

      stmt = c.createStatement();
      ResultSet rs = stmt.executeQuery( "SELECT * FROM CUSTOM;" );
      
      while ( rs.next() ) {
        String  username = rs.getString("username");
         String  name = rs.getString("name");
         String  backgroundcolor = rs.getString("backgroundcolor");
         String  description = rs.getString("description");
         
         System.out.println( "USERNAME = " + username );
         System.out.println( "NAME = " + name );
         System.out.println( "AGE = " + backgroundcolor );
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
}
  
