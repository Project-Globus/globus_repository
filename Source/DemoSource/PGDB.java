
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

public class PGDB {
	private static final String hostname = "globus2.csarh2gerrai.us-west-2.rds.amazonaws.com"; //db.projectglobus.com
	private static final String port = "3307";
	private static final String database = "globus";
	private static final String user = "blank";
	private static final String password = "blank";
	
	private static Connection connection;
	private static Statement statement = null;
	private static ResultSet result = null;
	private static String statusLog = "PGDB NOT INITIALIZED";
	private static boolean INIT = false;
		
	public static boolean getInit(){
		return INIT;
	}
	
	// Prevents SQL injection of our DB by replacing escape strings
	public static String validate(String input) {
		input = input.replace('\'', '~').replace('"', '~');
		return input;
	}
	
	// This must be called before any PGDB functions can be called.
	// init() establishes the connection to the database.
	public static String init(){
		// Initialize the connection driver.
		try {
			Class.forName("com.mysql.jdbc.Driver");
			statusLog = "<HTML>PGDB: MySQL Driver successfully initialized.<BR>";
		} catch (Exception ex) {
			statusLog = "<HTML>PGDB: ERROR - MySQL Driver failed to initialize.<BR>";
        }
		
		// Create Connection to database.
		// TODO: Create beanstalk daemon to handle secure information connection
		try {
			connection = DriverManager.getConnection(
                    "jdbc:mysql://" + hostname + ":" + port + "/" + database ,
                    user, password);
			
			statusLog += "PGDB: Connection to Database established.<BR>";
			
			// Mark PGDB as initialized.
			INIT = true;
		} catch (SQLException ex){
			statusLog += "PGDB: ERROR - SQL EXCEPTION" +
					 "<BR>SQLException: " + ex.getMessage() +
					 "<BR>SQLState: " + ex.getSQLState() +
					 "<BR>VendorError: " + ex.getErrorCode();
		}
		
		return statusLog + "</HTML>";
	}

	// Sends the final message, does not handle return object queries
	private static String sendMessage(String query){
		if (!getInit()) return "PGDB NOT INITIALIZED";
		
		
		try { 
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			if (statement.execute(query)) {
				result = statement.getResultSet();
				statusLog = "Update successful.";
			}
		}
		catch (SQLException ex) { 
			statusLog = "<html>ERROR: SQL EXCEPTION" +
					 "<br>SQLException: " + ex.getMessage() +
					 "<br>SQLState: " + ex.getSQLState() +
					 "<br>VendorError: " + ex.getErrorCode() + "</html>";
		}
		
		return statusLog;
	}
	
	// Sends a query, returning results. Returns null on error, or on result-less query
	private static ResultSet sendQuery(String query){
		if (!getInit()) return null;
		
		try { 
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			
			if (statement.execute(query)) {
				result = statement.getResultSet();
				statusLog = "Query successful.";
			}
		}
		catch (SQLException ex) { 
			statusLog = "<html>ERROR: SQL EXCEPTION" +
					 "<br>SQLException: " + ex.getMessage() +
					 "<br>SQLState: " + ex.getSQLState() +
					 "<br>VendorError: " + ex.getErrorCode() + "</html>";
		}
		
		return result;
	}
	
	public static String createAccount(String accountName, String accountBio,
	  String accountUser, String accountPass) {
		accountUser = validate(accountUser);
		accountPass = validate(accountPass);
		accountBio = validate(accountBio);
		accountName = validate(accountName);
		String hash = "";
		
		try {
			ResultSet rs = sendQuery("SELECT 1 FROM globus.Users WHERE email = '" + accountUser + "'");
			if (rs.first()){
				if(rs.getInt(1) == 1)
					return "Email (username) exists.";
			}
		} catch (SQLException e1) {
				return e1.getMessage();
		}
		
		try {
			hash = PasswordHash.createHash(accountPass);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}

		return sendMessage("INSERT INTO globus.Users (name,email,password,biography,last_update) VALUES ('" + 
					accountName + "','" + 
					accountUser + "','" + 
					hash + "','" + 
					accountBio + "','" + "0')");
	}

	public static String login(String accountUser, String accountPass){
		accountUser = validate(accountUser);
		accountPass = validate(accountPass);
		String name = "";
		try {
			ResultSet rs = sendQuery("SELECT password,name FROM globus.Users WHERE email = '" + accountUser + "'");
			if (!rs.first()){
					return "No such username exists. Please register.";
			}
			try {
				if(!PasswordHash.validatePassword(accountPass,rs.getString(1)))
					return "No such username and password combination exists. ";
				name = rs.getString(2);
			} catch (NoSuchAlgorithmException e) {
			} catch (InvalidKeySpecException e) { }
		} catch (SQLException e1) {
				return e1.getMessage();
		}
		
		return "Login successful! Welcome " + name;
	}
	
	public static String dropTables() {
		try { 
			statement.execute("DROP TABLE globus.EventMembers"); 
			statement.execute("DROP TABLE globus.Messages"); 
			statement.execute("DROP TABLE globus.Events"); 
			statement.execute("DROP TABLE globus.GroupMembers"); 
			statement.execute("DROP TABLE globus.Calendar"); 
			statement.execute("DROP TABLE globus.Groups");
			statement.execute("DROP TABLE globus.Users"); 
			statusLog = "Dropping of Tables successful.";
		}
		catch (SQLException ex) { 
			statusLog = "<html>ERROR: SQL EXCEPTION" +
					 "<br>SQLException: " + ex.getMessage() +
					 "<br>SQLState: " + ex.getSQLState() +
					 "<br>VendorError: " + ex.getErrorCode() + "</html>";
		} finally {
			if (result != null) try { result.close(); } catch (SQLException ex) {}
			if (statement != null) try { statement.close(); } catch (SQLException ex) {}
		}
		return statusLog;
	}
	
	public static String createTables() {
		try { 
			statement.execute("CREATE TABLE globus.Groups ( group_id INT NOT NULL AUTO_INCREMENT, group_name VARCHAR(255), creator INT, description VARCHAR(2500), password VARCHAR(255), google_username VARCHAR(255), google_password VARCHAR(255), PRIMARY KEY (group_id) )");
			statement.execute("CREATE TABLE globus.Users ( user_id INT NOT NULL AUTO_INCREMENT, name VARCHAR(255), email VARCHAR(255), password VARCHAR(255), biography VARCHAR(2500), last_update INT, PRIMARY KEY (user_id) )");
			statement.execute("CREATE TABLE globus.GroupMembers ( group_id INT, user_id INT, FOREIGN KEY(group_id) REFERENCES globus.Groups(group_id), FOREIGN KEY(user_id) REFERENCES globus.Users(user_id) )"); 
			statement.execute("CREATE TABLE globus.Calendar ( cal_id INT NOT NULL AUTO_INCREMENT, group_id INT, PRIMARY KEY(cal_id), FOREIGN KEY (group_id) REFERENCES globus.Groups(group_id) )");
			statement.execute("CREATE TABLE globus.Events ( cal_id INT, event_id INT NOT NULL AUTO_INCREMENT, time_start BIGINT, time_end BIGINT, name VARCHAR(255), description VARCHAR(2500), PRIMARY KEY(event_id), FOREIGN KEY (cal_id) REFERENCES globus.Calendar(cal_id) )");
			statement.execute("CREATE TABLE globus.EventMembers ( cal_id INT, event_id INT, user_id INT, permissions ENUM('Attendee','Organizer','Administrator','Creator'), FOREIGN KEY (cal_id) REFERENCES globus.Calendar(cal_id), FOREIGN KEY (event_id) REFERENCES globus.Events(event_id), FOREIGN KEY (user_id) REFERENCES globus.Users(user_id) )");
			statement.execute("CREATE TABLE globus.Messages ( message_id INT NOT NULL AUTO_INCREMENT, group_id INT, type ENUM('Announcement','Broadcast','Emergency','Normal'), contents VARCHAR(2500), timestamp BIGINT, PRIMARY KEY (message_id), FOREIGN KEY (group_id) REFERENCES globus.Groups(group_id) )");
			statusLog = "Creation of Tables successful.";
		}
		catch (SQLException ex) { 
			statusLog = "<html>ERROR: SQL EXCEPTION" +
					 "<br>SQLException: " + ex.getMessage() +
					 "<br>SQLState: " + ex.getSQLState() +
					 "<br>VendorError: " + ex.getErrorCode() + "</html>";
		} finally {
			if (result != null) try { result.close(); } catch (SQLException ex) {}
			if (statement != null) try { statement.close(); } catch (SQLException ex) {}
		}
		return statusLog;
	}
}