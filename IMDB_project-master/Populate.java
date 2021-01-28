import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/**
 * Load Genre into IMDB_NEW from the genre_array attribute in 
 * IMDB_ORG.Title_Basics
 * 
 * @author Michael Norton
 * @version GP3 (6 March 2019)
 *
 */
public class Populate {

    /**
     * Main method.
     * 
     * @param args Command Line Arguments
     * @throws SQLException The SQL Exceptoin
     */
    public static void main( String[] args ) throws SQLException {

        Connection conn;
        MysqlDataSource dataSource;
        PreparedStatement pstmt;
        Statement stmt;
        ResultSet rs;
        String updateString;
        TempSet set;

        dataSource = new MysqlDataSource();
        dataSource.setPort( 3306 ); // could also be 3306
        dataSource.setUseSSL( false );
        dataSource.setDatabaseName( "IMDB_ORG" );
        dataSource.setUser( "blalocap" );
        dataSource.setPassword( "cs474" );
        dataSource.setServerName( "mysql.cs.jmu.edu" ); // "mysql.cs.jmu.edu" if  
                                                        // running from Eclipse

        conn = (Connection) dataSource.getConnection();
        stmt = (Statement) conn.createStatement();
        
        System.out.println("Starting...");
        
        // Recreate whole database
        System.out.println("Recreating database");
        stmt.executeUpdate("DROP DATABASE IF EXISTS IMDB_BlalockPadilla;");
        stmt.executeUpdate("CREATE DATABASE IMDB_BlalockPadilla " +
                           "CHARACTER SET UTF8 COLLATE utf8_general_ci;");
        
        // Connect to database
        stmt.executeUpdate("USE IMDB_BlalockPadilla;");
        
        // Create and populate Region table
        System.out.println("Creating region table");
        stmt.executeUpdate("CREATE TABLE Region (" +
                               "regionID VARCHAR(6)," +
                               "regionDescription VARCHAR(255)" +
                           ");");
        stmt.executeUpdate("INSERT INTO Region (regionID) " +
                           "SELECT region FROM IMDB_ORG.Title_AKAs " +
                           "WHERE region IS NOT NULL " +
                           "GROUP BY Region;");
        
        // Create and populate Language Table
        System.out.println("Creating Language table");
        stmt.executeUpdate("CREATE TABLE Language (" + 
                               "languageID VARCHAR(6)," + 
                               "languageName VARCHAR(255)" +
                           ");");
        stmt.executeUpdate("INSERT INTO Language (languageID) " +
                           "SELECT language FROM IMDB_ORG.Title_AKAs " +
                           "WHERE language IS NOT NULL " +
                           "GROUP BY language;");
        
        // Create and populate VersionType table
        System.out.println("Creating VersionType table");
        stmt.executeUpdate("CREATE TABLE VersionType (" +
                               "versionTypeID VARCHAR(16)," +
                               "versionTypeDescription VARCHAR(255)" +
                           ");");
        rs = stmt.executeQuery("SELECT types_array FROM IMDB_ORG.Title_AKAs " +
                                "WHERE types_array IS NOT NULL " +
                                "GROUP BY types_array;");
        set = new TempSet();
        while (rs.next()) {
            for (String item : rs.getString("types_array").split(",|\\n"))
                set.add(item);
        }
        pstmt = (PreparedStatement) conn.prepareStatement("INSERT INTO VersionType " +
                                                          "(versionTypeID) " +
                                                          "VALUES (?);");
        for (int i = 0; i < set.size(); i++) {
            pstmt.setString(1, set.get(i));
            pstmt.executeUpdate();
        }
        
        // Create and populate Genre table
        System.out.println("Creating genre table");
        stmt.executeUpdate("CREATE TABLE Genre (" +
                               "genreID VARCHAR(16)," +
                               "genreDescription VARCHAR(255)" +
                            ");");
        rs = stmt.executeQuery("SELECT genres_array FROM IMDB_ORG.Title_Basics " +
                               "WHERE genres_array IS NOT NULL " +
                               "GROUP BY genres_array;");
        set = new TempSet();
        while (rs.next()) {
            for (String item : rs.getString("genres_array").split(",|\\n"))
                set.add(item);
        }
        pstmt = (PreparedStatement) conn.prepareStatement("INSERT INTO Genre (genreID) " +
                                                          "VALUES (?);");
        for (int i = 0; i < set.size(); i++) {
            pstmt.setString(1, set.get(i));
            pstmt.executeUpdate();
        }
        
        // Create and populate ProductionType
        System.out.println("Creating ProductionType table");
        stmt.executeUpdate("CREATE TABLE ProductionType (" +
                           "typeID VARCHAR(16)," +
                           "typeDescription VARCHAR(255)" +
                           ");");
        stmt.executeUpdate("INSERT INTO ProductionType (typeID) " +
                           "SELECT titleType FROM IMDB_ORG.Title_Basics " +
                           "WHERE titleType IS NOT NULL " +
                           "GROUP BY titleType;");
        
        // Create Job
        System.out.println("Creating Job table");
        stmt.executeUpdate("CREATE TABLE Job (" +
                               "jobID VARCHAR(24)," +
                               "jobDescription VARCHAR(255)" +
                           ");");
        
        // Create and populate Production
        System.out.println("Creating Production table");
        stmt.executeUpdate("CREATE TABLE Production ( "+
                               "prodID VARCHAR(10)," +
                               "typeID VARCHAR(16)," +
                               "primaryTitle VARCHAR(512)," +
                               "originalTitle VARCHAR(512)," +
                               "isAdult INTEGER(1)," +
                               "startYear INTEGER(4)," +
                               "endYear INTEGER(4)," +
                               "runTime INTEGER(4)" +
                           ");");
        stmt.executeUpdate("INSERT INTO Production (prodID, typeID, primaryTitle, originalTitle, " +
                               "isAdult, startYear, endYear, runTime) " +
                           "SELECT tconst, titleType, primaryTitle, originalTitle, isAdult, startYear, " +
                               "endYear, runTimeMinutes " +
                           "FROM IMDB_ORG.Title_Basics;");
        
        // Create and populate Version
        System.out.println("Creating Version table");
        stmt.executeUpdate("CREATE TABLE Version (" +
                               "prodID VARCHAR(10)," +
                               "sequence SMALLINT(5)," +
                               "title VARCHAR(1024)," +
                               "regionID VARCHAR(6)," +
                               "languageID VARCHAR(6)," +
                               "isOriginal TINYINT(1)," +
                               "comments VARCHAR(2048)" +
                           ");");
        stmt.executeUpdate("INSERT INTO Version (prodID, sequence, regionID, languageID, " +
                               "comments, isOriginal) " +
                           "SELECT tconst, ordering, region, language, " +
                               "REPLACE(attributes_array, ',', '\n'), isOriginalTitle " +
                           "FROM IMDB_ORG.Title_AKAs GROUP BY tconst, ordering;");
        stmt.executeUpdate("INSERT INTO Production (prodID) " +
                           "SELECT prodID FROM Version " +
                           "WHERE prodID NOT IN (" +
                               "SELECT prodID FROM Production" +
                           ") GROUP BY prodID;"); 
        
        // Create and populate Episode
        System.out.println("Creating Episode table");
        stmt.executeUpdate("CREATE TABLE Episode (" +
                               "prodID VARCHAR(10)," +
                               "parentProdID_FK VARCHAR(255)," +
                               "seasonNumber INTEGER(10)," +
                               "episodeNumber INTEGER(10)" +
                           ");");
        stmt.executeUpdate("INSERT INTO Episode (prodID, parentProdID_FK, seasonNumber, " +
                               "episodeNumber) " +
                           "SELECT tconst, parentTconst_FK, seasonNumber, episodeNumber " +
                           "FROM IMDB_ORG.Title_Episode;");
        stmt.executeUpdate("INSERT INTO Production (prodID) " +
                           "SELECT parentProdID_FK " +
                           "FROM Episode " +
                           "AS prodID " +
                           "WHERE parentProdID_FK NOT IN (" +
                               "SELECT prodID FROM Production" +
                               "AS parentProdID_FK" +
                           ") GROUP BY parentProdID_FK;");
        
        // Create and populate Person
        System.out.println("Creating Person table");
        stmt.executeUpdate("CREATE TABLE Person (" +
                               "personID VARCHAR(10)," +
                               "primaryName VARCHAR(255)," +
                               "birthYear INTEGER(10)," +
                               "deathYear INTEGER(4)" +
                           ");");
        stmt.executeUpdate("INSERT INTO Person (personID, primaryname, birthYear, deathYear) " +
                           "SELECT nconst, primaryname, birthYear, deathYear " +
                           "FROM IMDB_ORG.Name_Basics;");
        
        // Create and populate Ratings
        System.out.println("Creating Ratings table");
        stmt.executeUpdate("CREATE TABLE Ratings (" +
                               "prodID VARCHAR(10)," +
                               "averageRating NUMERIC(9, 2)," +
                               "numVotes INTEGER(10)" +
                           ");");
        stmt.executeUpdate("INSERT INTO Ratings (prodID, averageRating, numVotes) " +
                           "SELECT tconst, averageRating, numVotes " +
                           "FROM IMDB_ORG.Title_Ratings;");
        stmt.executeUpdate("INSERT INTO Production (prodID) " +
                           "SELECT prodID FROM Ratings " +
                           "WHERE prodID NOT IN (" +
                               "SELECT prodID FROM Production" +
                           ");");
        
        // Create and populate CastAndCrew
        System.out.println("Creating CastAndCrew table");
        stmt.executeUpdate("CREATE TABLE CastAndCrew (" +
                               "prodID VARCHAR(10)," +
                               "personID VARCHAR(10)," +
                               "jobID VARCHAR(24)," +
                               "jobTitle VARCHAR(1024)," +
                               "characterPlayed VARCHAR(1024)" +
                           ");");
        stmt.executeUpdate("INSERT INTO CastAndCrew (prodID, personID, jobID, jobTitle, " +
                               "characterPlayed) " +
                           "SELECT tconst, nconst, category, job, characters " +
                           "FROM IMDB_ORG.Title_Principals;");
        /**********************************************************************
         * THIS NEXT PART TOOK LITERALLY MORE THAN 24 HOURS ARE YOU KIDDING ME?
         **********************************************************************/
        stmt.executeUpdate("CREATE TABLE CastAndCrewTemp (" +
                               "prodID VARCHAR(10)," +
                               "personID VARCHAR(10)," +
                               "jobID VARCHAR(24)" +
                           ");");
        rs = stmt.executeQuery("SELECT * FROM IMDB_ORG.Title_Crew;");
        while (rs.next()) {
            String prodID = rs.getString("tconst");
            String stringArray = rs.getString("directors_array");
            pstmt = (PreparedStatement) conn.prepareStatement("INSERT INTO CastAndCrewTemp " +
                                                                  "(prodID, personID, jobID) " +
                                                              "VALUES (?, ?, ?);");
            pstmt.setString(1, prodID);
            if (stringArray != null) {
                pstmt.setString(3, "director");
                for (String currDirector : stringArray.split(",|\\n")) {
                    pstmt.setString(2, currDirector);
                    pstmt.executeUpdate();
                }
            }
            stringArray = rs.getString("writers_array");
            if (stringArray != null) {
                pstmt.setString(3, "writer");
                for (String currWriter : stringArray.split(",|\\n")) {
                    pstmt.setString(2, currWriter);
                    pstmt.executeUpdate();
                }
            }
        }
        stmt.executeUpdate("INSERT INTO CastAndCrew (prodID, personID, jobID) " +
                           "SELECT prodID, personID, jobID " +
                           "FROM CastAndCrewTemp " +
                           "WHERE (prodID, personID, jobID) NOT IN (" +
                               "SELECT prodID, personID, jobID " +
                               "FROM CastAndCrew" +
                           ");");
        stmt.executeUpdate("INSERT INTO Production (prodID) " +
                           "SELECT prodID FROM CastAndCrew " +
                           "WHERE prodID NOT IN (" +
                               "SELECT prodID FROM Production" +
                           ");");
        stmt.executeUpdate("DROP TABLE CastAndCrewTemp;");
        
        // Create ProductionVersionType
        stmt.executeUpdate("CREATE TABLE ProductionVersionType (" +
                               "prodID VARCHAR(10), " +
                               "sequence SMALLINT(5), " +
                               "versiontypeID VARCHAR(6), " +
                               "versionTypeNotes VARCHAR(255)"+
                           ");");
        
        // Create ProductionGenre
        stmt.executeUpdate("CREATE TABLE ProductionGenre (" +
                               "prodID VARCHAR(10), " +
                               "genreID VARCHAR(16), " +
                               "prodGenreNotes VARCHAR(255)" +
                           ");");
        
        /****************************************
         * KEY CONSTRAINT DECLARATIONS BEGIN HERE
         ****************************************/
        
        // Set Region key constraints
        System.out.println("Adding Region key constraints");
        stmt.executeUpdate("ALTER TABLE Region " + 
                           "ADD CONSTRAINT PRIMARY KEY (regionID);");
        
        // Set Language key constraints
        System.out.println("Adding Language key constraints");
        stmt.executeUpdate("ALTER TABLE Language " +
                           "ADD CONSTRAINT PRIMARY KEY (languageID);");
        
        // Set VersionType key constraints
        System.out.println("Adding VersionType key constraints");
        stmt.executeUpdate("ALTER TABLE VersionType " +
                           "ADD CONSTRAINT PRIMARY KEY (versionTypeID, sequence);");
        
        // Set Genre key constraints
        System.out.println("Adding Genre key constraints");
        stmt.executeUpdate("ALTER TABLE Genre " +
                           "ADD CONSTRAINT PRIMARY KEY (genreID);");
        
        // Set ProductionType key constraints
        System.out.println("Adding ProductionType key constraints");
        stmt.executeUpdate("ALTER TABLE ProductionType " +
                           "ADD CONSTRAINT PRIMARY KEY (typeID);");
        
        // Set Job key constraints
        System.out.println("Adding Job key constraints");
        stmt.executeUpdate("ALTER TABLE Job " +
                           "ADD CONSTRAINT PRIMARY KEY (jobID);");
        
        // Set Production key constraints
        System.out.println("Adding Production key constraints");
        stmt.executeUpdate("ALTER TABLE Production " +
                           "ADD CONSTRAINT PRIMARY KEY (prodID);");
        stmt.executeUpdate("ALTER TABLE Production " +
                           "ADD CONSTRAINT FOREIGN KEY (typeID) " +
                           "REFERENCES ProductionType (typeID);");
        
        // Set Version key constraints
        System.out.println("Adding Version key constraints");
        stmt.executeUpdate("ALTER TABLE Version " +
                           "ADD CONSTRAINT PRIMARY KEY (prodID, sequence);");
        stmt.executeUpdate("ALTER TABLE Version " +
                           "ADD CONSTRAINT FOREIGN KEY (prodID) " +
                           "REFERENCES Production(prodID);");
        stmt.executeUpdate("ALTER TABLE Version " +
                           "ADD CONSTRAINT FOREIGN KEY (regionID) " +
                           "REFERENCES Region (regionID);");
        stmt.executeUpdate("ALTER TABLE Version " +
                           "ADD CONSTRAINT FOREIGN KEY (languageID) " +
                           "REFERENCES Language (languageID);");
        
        // Set Episode key constraints
        System.out.println("Adding Episode key constraints");
        stmt.executeUpdate("ALTER TABLE Episode " +
                           "ADD CONSTRAINT PRIMARY KEY (prodID);");
        stmt.executeUpdate("ALTER TABLE Episode " +
                           "ADD CONSTRAINT FOREIGN KEY (prodID) " +
                           "REFERENCES Production(prodID);");
        stmt.executeUpdate("ALTER TABLE Episode " +
                           "ADD CONSTRAINT FOREIGN KEY (parentProdID_FK) " +
                           "REFERENCES Production(prodID);");
        
        // Set Person key constraints
        System.out.println("Adding Person key constraints");
        stmt.executeUpdate("ALTER TABLE Person " +
                           "ADD CONSTRAINT PRIMARY KEY (personID);");
        
        // Set Ratings key constraints
        System.out.println("Adding Ratings key constraints");
        stmt.executeUpdate("ALTER TABLE Ratings " +
                           "ADD CONSTRAINT PRIMARY KEY (prodID);");
        stmt.executeUpdate("ALTER TABLE Ratings " +
                           "ADD CONSTRAINT FOREIGN KEY (prodID) " +
                           "REFERENCES Production(prodID);");
        
        // Set CastAndCrew key constraints
        System.out.println("Adding CastAndCrew key constraints");
        stmt.executeUpdate("ALTER TABLE CastAndCrew " +
                           "ADD CONSTRAINT PRIMARY KEY (prodID, personID, jobID, sequence);");
        stmt.executeUpdate("ALTER TABLE CastAndCrew " +
                           "ADD CONSTRAINT FOREIGN KEY (prodID) " +
                           "REFERENCES Production(prodID);");
        stmt.executeUpdate("ALTER TABLE CastAndCrew " +
                           "ADD CONSTRAINT FOREIGN KEY (personID) " +
                           "REFERENCES Person(personID);");
        stmt.executeUpdate("ALTER TABLE CastAndCrew " +
                           "ADD CONSTRAINT FOREIGN KEY (jobID) " +
                           "REFERENCES Job (jobID);");
        
        // Set ProductionVersionType key constraints
        System.out.println("Adding ProductionVersionType key constraints");
        stmt.executeUpdate("ALTER TABLE ProductionVersionType " +
                           "ADD CONSTRAINT PRIMARY KEY (prodID, sequence, versionTypeID);");
        stmt.executeUpdate("ALTER TABLE ProductionVersionType " +
                           "ADD CONSTRAINT FOREIGN KEY (prodID) " +
                           "REFERENCES Version (prodID);");
        stmt.executeUpdate("ALTER TABLE ProductionVersionType " +
                           "ADD CONSTRAINT FOREIGN KEY (sequence) " +
                           "REFERENCES Version (sequence);");
        stmt.executeUpdate("ALTER TABLE ProductionVersionType " +
                           "ADD CONSTRAINT FOREIGN KEY (versionTypeID) " +
                           "REFERENCES VersionType(versionTypeID);");
        
        // Set ProductionGenre key constraints
        System.out.println("Adding ProductionGenre key constraints");
        stmt.executeUpdate("ALTER TABLE ProductionGenre " +
                           "ADD CONSTRAINT PRIMARY KEY (prodID, genreID);");
        stmt.executeUpdate("ALTER TABLE ProductionGenre " +
                           "ADD CONSTRAINT FOREIGN KEY (prodID) " +
                           "REFERENCES Production(prodID);");
        stmt.executeUpdate("ALTER TABLE ProductionGenre " +
                           "ADD CONSTRAINT FOREIGN KEY (genreID) " +
                           "REFERENCES Genre(genreID);");
        
        System.out.println("Finished!");
        /****************************************
         * How are we even supposed to do step 4?
         ****************************************/
    }
    
    /**
     * Inner class to get unique instances of the genre.
     * 
     * @author Michael Norton
     * @version 
     */
    private static class TempSet {
        
        private ArrayList<String> list;
        
        /**
         * Default constructor.
         */
        public TempSet() {
            
            list = new ArrayList<String>();
        }
        
        /**
         * Add unique instances to the ArrayList.
         * 
         * @param str The string to add
         */
        public void add( String str ) {
            
            if ( !list.contains( str ) ) {
                list.add(  str );
            }
            
        }
        
        /**
         * Get an element from the ArrayList
         * 
         * @param which Which element to get
         * @return the requested element
         */
        public String get( int which ) {
            
            String val = null;
            
            if ( which >= 0 && which < list.size() ) {
                
                val = list.get( which );
            }
            
            return val;
        }
        
        /**
         * Return the size of the ArrayList
         * 
         * @return the size of the list
         */
        public int size() {
            
            return list.size();
        }
        
        /**
         * Return the arrayList as a String
         */
        public String toString() {
            
            String returnString = "";
            
            for ( String str: list ) {
                
                returnString += str + "\n";
            }
            
            return returnString;
        }
    }   
}
