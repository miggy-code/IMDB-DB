import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.*;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class imdb_gui extends JFrame {
  /**
	 * 
	 */
	private static final long serialVersionUID = -4471296639912931336L;

private ActionHandler actionHandler = new ActionHandler();
  
  JLabel usernameLabel = new JLabel("Username");
  JTextField usernameField = new JTextField(5);
  JLabel passwordLabel = new JLabel("Password");
  JTextField passwordField = new JTextField(5);
  JButton loginButton = new JButton("Login");
  JRadioButton filmButton = new JRadioButton("Film");
  JRadioButton seriesButton = new JRadioButton("Series");
  JRadioButton personButton = new JRadioButton("Person");
  ButtonGroup mainButtonGroup = new ButtonGroup();
  JCheckBox titleCheckBox = new JCheckBox("Title");
  JTextField titleField = new JTextField(5);
  JLabel nameLabel = new JLabel("Name");
  JTextField nameField = new JTextField(5);
  JCheckBox seasonCheckBox = new JCheckBox("Season #");
  JTextField seasonField = new JTextField(5);
  JCheckBox episodeCheckBox = new JCheckBox("Episode #");
  JTextField episodeField = new JTextField(5);
  JCheckBox yearCheckBox = new JCheckBox("Year");
  JTextField yearField = new JTextField(5);
  JCheckBox ratingCheckBox = new JCheckBox("Rating");
  JTextField ratingField = new JTextField(5);
  JButton searchButton = new JButton("Search");
  JPanel searchPanel = new JPanel();
  JPanel resultPanel = new JPanel();
  
  Connection conn;
  MysqlDataSource dataSource;
  PreparedStatement pstmt;
  Statement stmt;
  ResultSet rs; 

  public imdb_gui() {
    setTitle("IMDB APP");
    setSize(960, 540);
    setLayout(new BorderLayout());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    loginButton.addActionListener(actionHandler);

    // production button
    filmButton.addActionListener(actionHandler);

    // Series button
    seriesButton.addActionListener(actionHandler);

    // Person button
    personButton.addActionListener(actionHandler);

    // Group previous buttons
    mainButtonGroup.add(filmButton);
    mainButtonGroup.add(seriesButton);
    mainButtonGroup.add(personButton);

    titleCheckBox.setVisible(false);
    titleCheckBox.addActionListener(actionHandler);

    titleField.setVisible(false);

    nameLabel.setVisible(false);

    nameField.setVisible(false);

    // Season Checkbox
    seasonCheckBox.setVisible(false);
    seasonCheckBox.addActionListener(actionHandler);

    // Season Field
    seasonField.setVisible(false);

    // Episode Checkbox
    episodeCheckBox.setVisible(false);
    episodeCheckBox.addActionListener(actionHandler);

    // Episode field
    episodeField.setVisible(false);

    // Year Checkbox
    yearCheckBox.setVisible(false);
    yearCheckBox.addActionListener(actionHandler);

    // Year field
    yearField.setVisible(false);

    // Rating CheckBox
    ratingCheckBox.setVisible(false);
    ratingCheckBox.addActionListener(actionHandler);

    // Rating field
    ratingField.setVisible(false);

    // Search Button
    searchButton.addActionListener(actionHandler);

    // search panel
    searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
    searchPanel.add(usernameLabel);
    searchPanel.add(usernameField);
    searchPanel.add(passwordLabel);
    searchPanel.add(passwordField);
    searchPanel.add(loginButton);

    // Instantiate the result panel
    resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
    
    dataSource = new MysqlDataSource();
    dataSource.setPort( 3306 ); // could also be 3306
    dataSource.setUseSSL( false );
    dataSource.setDatabaseName( "IMDB" );
    dataSource.setServerName( "mysql.cs.jmu.edu" );

    add(searchPanel, BorderLayout.NORTH);
    add(resultPanel, BorderLayout.CENTER);
    setVisible(true);
  }

  private class ActionHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      if (e.getSource().equals(loginButton)) {
		  dataSource.setUser(usernameField.getText());
		  dataSource.setPassword(passwordField.getText());
		    try {
		        conn = (Connection) dataSource.getConnection();
		        stmt = (Statement) conn.createStatement();
		        
		        searchPanel.removeAll();
		        searchPanel.add(filmButton);
		        searchPanel.add(seriesButton);
		        searchPanel.add(personButton);
		        searchPanel.add(titleCheckBox);
		        searchPanel.add(titleField);
		        searchPanel.add(nameLabel);
		        searchPanel.add(nameField);
		        searchPanel.add(seasonCheckBox);
		        searchPanel.add(seasonField);
		        searchPanel.add(episodeCheckBox);
		        searchPanel.add(episodeField);
		        searchPanel.add(yearCheckBox);
		        searchPanel.add(yearField);
		        searchPanel.add(ratingCheckBox);
		        searchPanel.add(ratingField);
		        searchPanel.add(searchButton);
		        searchPanel.revalidate();
		        searchPanel.repaint();
		        
		        resultPanel.removeAll();
		        resultPanel.revalidate();
		        resultPanel.repaint();
		      } catch (SQLException sqle) {
		        resultPanel.removeAll();  
	            resultPanel.add(new JLabel("Invalid username and/or password"));
	            resultPanel.revalidate();
	            resultPanel.repaint();
		      }
	  
      } else if (e.getSource().equals(searchButton)) {
        resultPanel.removeAll();
        String query;
        
        if (filmButton.isSelected()) {
          if (!titleCheckBox.isSelected() && !yearCheckBox.isSelected()
              && !ratingCheckBox.isSelected())
            resultPanel.add(new JLabel("Error: No search fields enabled"));

          else {

            query = "SELECT Production.prodID, primaryTitle, startYear, runTime, averageRating " +
                    "FROM Production ";
            
            query += "LEFT OUTER JOIN Ratings ON Production.prodID = Ratings.prodID ";
            query += " WHERE ";
            query += "typeID = 'movie' AND ";
            if (ratingCheckBox.isSelected())
              query += "averageRating = '" + ratingField.getText() + "' AND ";
            if (titleCheckBox.isSelected())
              query += "primaryTitle LIKE '%" + titleField.getText() + "' AND ";
            if (yearCheckBox.isSelected())
              query += "startYear = '" + yearField.getText() + "' AND ";
            
            try {
              rs = stmt.executeQuery(query.substring(0, query.length() - 4) + ";");
              
              ArrayList<String> prodIDs = new ArrayList<>();
              ArrayList<String> primaryTitles = new ArrayList<>();
              ArrayList<String> startYears = new ArrayList<>();
              ArrayList<String> runTimes = new ArrayList<>();
              ArrayList<String> averageRatings = new ArrayList<>();
              ArrayList<String> genres = new ArrayList<>();
              
              while (rs.next()) {
                prodIDs.add(rs.getString("prodID"));
                primaryTitles.add(rs.getString("primaryTitle"));
                startYears.add(rs.getString("startYear"));
                runTimes.add(rs.getString("runTime"));
                averageRatings.add(rs.getString("averageRating"));
              }
              
              for (String prodID : prodIDs) {
                String genresString = "";
                rs = stmt.executeQuery("SELECT genreID FROM ProductionGenre " +
                    "WHERE prodID = '" + prodID + "';");
                while (rs.next())
                  genresString += rs.getString("genreID") + ", ";
                genres.add(genresString);
              }
              
              int numRows = prodIDs.size();
              JTable newTable = new JTable(numRows + 1, 5);
              newTable.setValueAt("Title", 0, 0);
              newTable.setValueAt("Year", 0, 1);
              newTable.setValueAt("Running Time", 0, 2);
              newTable.setValueAt("Average Rating", 0, 3);
              newTable.setValueAt("Genres", 0, 4);
              for (int i = 0; i < numRows; i++) {
                newTable.setValueAt(primaryTitles.get(i), i + 1, 0);
                newTable.setValueAt(startYears.get(i), i + 1, 1);
                newTable.setValueAt(runTimes.get(i), i + 1, 2);
                newTable.setValueAt(averageRatings.get(i), i + 1, 3);
                newTable.setValueAt(genres.get(i), i + 1, 4);
              }
              newTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
              newTable.setEnabled(false);
              resultPanel.add(new JScrollPane(newTable));

            } catch (SQLException sqle) {
              resultPanel.add(new JLabel("SQL Error"));
            }
          }
          
        } else if (seriesButton.isSelected()) {
          if (!titleCheckBox.isSelected() && !seasonCheckBox.isSelected()
              && !yearCheckBox.isSelected() && !ratingCheckBox.isSelected())
            resultPanel.add(new JLabel("Error: No search fields enabled"));
          
          else {
            query = "SELECT primaryTitle,startYear,seasonNumber, episodeNumber, runTime, Production.prodID, averageRating FROM Production ";
            query += "NATURAL JOIN Episode ";
            query += "NATURAL JOIN ProductionType ";
            query += "LEFT OUTER JOIN Ratings ON Production.prodID = Ratings.prodID ";
            query += " WHERE ";
            query += "typeID LIKE 'tv%' AND ";
            if (seasonCheckBox.isSelected())
                query += "seasonNumber = '" + seasonField.getText() + "' AND ";
            if (episodeCheckBox.isSelected())
                query += "episodeNumber = '" + episodeField.getText() + "' AND ";
            if (ratingCheckBox.isSelected())
              query += "averageRating = '" + ratingField.getText() + "' AND ";
            if (titleCheckBox.isSelected())
              query += "primaryTitle LIKE '%" + titleField.getText() + "%' AND ";
            if (yearCheckBox.isSelected())
              query += "startYear = '" + yearField.getText() + "' AND ";
            
            try {
              System.out.println(query);
              rs = stmt.executeQuery(query.substring(0, query.length() - 4) + ";");
              
              ArrayList<String> prodIDs = new ArrayList<>();
              ArrayList<String> primaryTitles = new ArrayList<>();
              ArrayList<String> startYears = new ArrayList<>();
              ArrayList<String> seasonNums = new ArrayList<>();
              ArrayList<String> episodeNums = new ArrayList<>();
              ArrayList<String> runTimes = new ArrayList<>();
              ArrayList<String> averageRatings = new ArrayList<>();
              ArrayList<String> genres = new ArrayList<>();
              
              while (rs.next()) {
                prodIDs.add(rs.getString("prodID"));
                primaryTitles.add(rs.getString("primaryTitle"));
                episodeNums.add(rs.getString("episodeNumber"));
                seasonNums.add(rs.getString("seasonNumber"));
                startYears.add(rs.getString("startYear"));
                runTimes.add(rs.getString("runTime"));
                averageRatings.add(rs.getString("averageRating"));
              }
              
              for (String prodID : prodIDs) {
                String genresString = "";
                rs = stmt.executeQuery("SELECT genreID FROM ProductionGenre " +
                    "WHERE prodID = '" + prodID + "';");
                while (rs.next())
                  genresString += rs.getString("genreID") + ", ";
                genres.add(genresString);
              }
              
              int numRows = prodIDs.size();
              JTable newTable = new JTable(numRows + 1, 7);
              newTable.setValueAt("Title", 0, 0);
              newTable.setValueAt("Year", 0, 1);
              newTable.setValueAt("Season", 0, 2);
              newTable.setValueAt("Episode", 0, 3);
              newTable.setValueAt("Running Time", 0, 4);
              newTable.setValueAt("Average Rating", 0, 5);
              newTable.setValueAt("Genres", 0, 6);
              for (int i = 0; i < numRows; i++) {
                newTable.setValueAt(primaryTitles.get(i), i + 1, 0);
                newTable.setValueAt(startYears.get(i), i + 1, 1);
                newTable.setValueAt(seasonNums.get(i), i + 1, 2);
                newTable.setValueAt(episodeNums.get(i), i + 1, 3);
                newTable.setValueAt(runTimes.get(i), i + 1, 4);
                newTable.setValueAt(averageRatings.get(i), i + 1, 5);
                newTable.setValueAt(genres.get(i), i + 1, 6);
              }
              newTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
              newTable.setEnabled(false);
              resultPanel.add(new JScrollPane(newTable));

            } catch (SQLException sqle) {
              resultPanel.add(new JLabel("SQL Error"));
            }
          }
          
        } else if (personButton.isSelected()) {
          String name = nameField.getText();
          if (name.equals("")) {
            resultPanel.add(new JLabel("Error: No name entered"));
          } else if (name.contains(";")) {
            resultPanel.add(new JLabel("Error: No semicolons allowed in search terms"));
          } else {
            query =
                "SELECT primaryName, birthYear, deathYear FROM Person WHERE primaryName LIKE '%";
                query += nameField.getText() + "%';";
                try {
                  rs = stmt.executeQuery(query);

                  ArrayList<String> primaryNames = new ArrayList<>();
                  ArrayList<String> birthYears = new ArrayList<>();
                  ArrayList<String> deathYears = new ArrayList<>();

                  while (rs.next()) {

                    primaryNames.add(rs.getString("primaryName"));
                    birthYears.add(rs.getString("birthYear"));
                    deathYears.add(rs.getString("deathYear"));
                  }
                  
                  int numRows = primaryNames.size();
                  JTable newTable = new JTable(numRows + 1, 3);
                  newTable.setValueAt("Name", 0, 0);
                  newTable.setValueAt("Birth Year", 0, 1);
                  newTable.setValueAt("Death Year", 0, 2);
                  for (int i = 0; i < numRows; i++) {
                    newTable.setValueAt(primaryNames.get(i), i + 1, 0);
                    newTable.setValueAt(birthYears.get(i), i + 1, 1);
                    newTable.setValueAt(deathYears.get(i), i + 1, 2);
                  }
                  newTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                  newTable.setEnabled(false);
                  resultPanel.add(new JScrollPane(newTable));

                } catch (SQLException sqle) {
                  resultPanel.add(new JLabel("SQL Error"));
                }
              
          }
         
        } else
          resultPanel.add(new JLabel("Error: No search option selected"));
        
        resultPanel.revalidate();
        resultPanel.repaint();
      } else {
        boolean seriesButtonSelected = seriesButton.isSelected();
        boolean titleButtonSelected = filmButton.isSelected() || seriesButtonSelected;
        boolean seasonCheckBoxSelected = seasonCheckBox.isSelected();
        boolean personButtonSelected = personButton.isSelected();

        titleCheckBox.setVisible(titleButtonSelected);
        titleField.setVisible(titleButtonSelected);
        titleField.setEnabled(titleCheckBox.isSelected());
        nameLabel.setVisible(personButtonSelected);
        nameField.setVisible(personButtonSelected);
        seasonCheckBox.setVisible(seriesButtonSelected);
        seasonField.setVisible(seriesButtonSelected);
        seasonField.setEnabled(seasonCheckBoxSelected);
        episodeCheckBox.setVisible(seriesButtonSelected);
        episodeCheckBox.setEnabled(seasonCheckBoxSelected);
        episodeField.setVisible(seriesButtonSelected);
        episodeField.setEnabled(seasonCheckBoxSelected && episodeCheckBox.isSelected());

        yearCheckBox.setVisible(titleButtonSelected);
        yearField.setVisible(titleButtonSelected);
        yearField.setEnabled(yearCheckBox.isSelected());
        ratingCheckBox.setVisible(titleButtonSelected);
        ratingField.setVisible(titleButtonSelected);
        ratingField.setEnabled(ratingCheckBox.isSelected());
        searchPanel.revalidate();
        searchPanel.repaint();
      }
    }
  }

  public static void main(String[] args) {
    new imdb_gui();
  }
}