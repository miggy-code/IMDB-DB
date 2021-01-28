import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DatabaseQueries {
  
  static Connection conn;
  static MysqlDataSource dataSource;
  PreparedStatement pstmt;
  static Statement stmt;
  static ResultSet rs;

 
  
  public static void setUp() {
    dataSource = new MysqlDataSource();
    dataSource.setPort( 3306 ); // could also be 3306
    dataSource.setUseSSL( false );
    dataSource.setDatabaseName( "IMDB_ORG" );
    dataSource.setUser( "blalocap" );
    dataSource.setPassword( "cs474" );
    dataSource.setServerName( "mysql.cs.jmu.edu" );
    try {
      conn = (Connection) dataSource.getConnection();
      stmt = (Statement) conn.createStatement();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  
  public static String[] queryDatabase(String query, String[] columnNames) throws SQLException {
    rs = stmt.executeQuery(query);
    ArrayList<String> outputList = new ArrayList<>();
    
    while(rs.next()) {
      String rowOutput = "";
      for(int i = 0; i < columnNames.length; i++) {
        rowOutput += rs.getString(columnNames[i]);
      }
      outputList.add(rowOutput);
    }
    return outputList.toArray(new String[0]);
  }

}
