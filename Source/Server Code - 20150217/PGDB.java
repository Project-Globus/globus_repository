
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;

public class PGDB {
	private static final String hostname = "globus2.csarh2gerrai.us-west-2.rds.amazonaws.com"; //db.projectglobus.com
	private static final String port = "3307";
	private static final String database = "globus";
	private static final String user = "globus";
	private static final String password = "Pr0jectGl0bus!";

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
	
	// Ensures the access credentials are correct
	private static boolean authenticate(String[] userInfo) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		try {
			String hash = new String();
			ResultSet rs = sendQuery("SELECT password FROM globus.Users WHERE user_id = '" + userInfo[2] + "'");
			if (!rs.first()) return false;
				else hash = rs.getString(1);

				if(!userInfo[5].equals(hash)) return false;
		} catch (SQLException e1) {
				return false;
		}
		return true;
	}
	
	// Sends a Create User request to the Database.
	// Returns a String Array of the form
	// [0] - Query Status
	// [1] - User Name
	// [2] - User ID
	// [3] - User Email
	// [4] - User Biography
	// [5] - User Password
	// [6] - Last Update
	public static String[] createAccount(String accountName, String accountBio,
	  String accountUser, String accountPass) {
		accountUser = validate(accountUser);
		accountPass = validate(accountPass);
		accountName = validate(accountName);
		accountBio = validate(accountBio);
		
		String hash = "";
		
		String[] result = new String[7];
		
		try {
			ResultSet rs = sendQuery("SELECT 1 FROM globus.Users WHERE email = '" + accountUser + "'");
			if (rs.first()){
				if(rs.getInt(1) == 1) {
					result[0] = "Email (username) exists.";
					return result;
				}
			}
		} catch (SQLException e1) {
			result[0] = e1.getMessage();
			return result;
		}
		
		try {
			hash = PasswordHash.createHash(accountPass);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}

		result[0] = sendMessage("INSERT INTO globus.Users (name,email,password,biography,last_update) VALUES ('" + 
					accountName + "','" + 
					accountUser + "','" + 
					hash + "','" + 
					accountBio + "','" + "0')");
		
		try {
			ResultSet rs = sendQuery("SELECT name,user_id,email,biography,password,last_update FROM globus.Users WHERE email = '" + accountUser + "'");
			if (!rs.first()){
				result[0] = "ERROR RETRIEVING INFORMATION FROM TABLE";
				return result;
			} else {
				result[1] = rs.getString(1);
				result[2] = rs.getString(2);
				result[3] = rs.getString(3);
				result[4] = rs.getString(4);
				result[5] = rs.getString(5);
				result[6] = rs.getString(6);
			}
		} catch (SQLException e1) {
				result[0] = e1.getMessage();
				return result;
		}
		
		return result;
	}

	// Sends a Log In request to the Database.
	// Returns a String Array of the form
	// [0] - Query Status
	// [1] - User Name
	// [2] - User ID
	// [3] - User Email
	// [4] - User Biography
	// [5] - User Password
	// [6] - Last Update
	public static String[] login(String accountUser, String accountPass){
		accountUser = validate(accountUser);
		accountPass = validate(accountPass);
		String[] result = new String[7];
		try {
			ResultSet rs = sendQuery("SELECT password FROM globus.Users WHERE email = '" + accountUser + "'");
			if (!rs.first()){
					result[0] = "No such username exists. Please register.";
					return result;
			} else result[1] = rs.getString(1);
			try {
				if(!PasswordHash.validatePassword(accountPass,result[1])) {
					result[0] = "No such username and password combination exists. ";
					return result;
				}
			} catch (NoSuchAlgorithmException e) {
			} catch (InvalidKeySpecException e) { }
		} catch (SQLException e1) {
				result[0] = e1.getMessage();
				return result;
		}
		
		try {
			ResultSet rs = sendQuery("SELECT name,user_id,email,biography,password,last_update FROM globus.Users WHERE email = '" + accountUser + "'");
			if (!rs.first()){
				result[0] = "ERROR RETRIEVING INFORMATION FROM TABLE";
				return result;
			} else {
				result[1] = rs.getString(1);
				result[2] = rs.getString(2);
				result[3] = rs.getString(3);
				result[4] = rs.getString(4);
				result[5] = rs.getString(5);
				result[6] = rs.getString(6);
				result[0] = "Login Successful! Welcome " + result[1];
				
				result[0] += ". Attempting Authentication: " + (authenticate(result) ? "success" : "failed");
			}
		} catch (SQLException e1) {
				result[0] = e1.getMessage();
				return result;
		}
		return result;
	}

	// Sends a GetMembership request to the Database.
	// Returns a two dimensional String Array of the form
	// [0][x] - Query Statuses
	// [x][0] - Group Name
	// [x][1] - Group ID
	// [x][2] - Group Password
	// [x][3] - Group Description
	// [x][4] - Group Creator ID
	// [x][5] - Group Google Drive Username
	// [x][6] - Group Google Drive Password
	public static ArrayList<ArrayList<String>> getMembership(String[] userInfo) {
		
		ArrayList<String> row = new ArrayList<String>();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		result.add(row);
		
		try {
			ResultSet rs = sendQuery("SELECT group_name,group_id,password,description,creator,google_username,google_password FROM globus.Groups WHERE creator = '" + userInfo[2] + "'");
			if (!rs.first()){
				row.add("ERROR RETRIEVING INFORMATION FROM TABLE");
				result.add(row);
				return result;
			} else {
				while (!rs.isAfterLast()) {
					row = new ArrayList<String>();
					row.add(rs.getString(1));
					row.add(rs.getString(2));
					row.add(rs.getString(3));
					row.add(rs.getString(4));
					row.add(rs.getString(5));
					row.add(rs.getString(6));
					row.add(rs.getString(7));
					result.add(row);
					rs.next();
				}
				for (int i = 1; i < result.size(); ++i)
					result.get(0).add("Group Created! You are the creator for groups "+result.get(i).get(1));
			}
		} catch (SQLException e1) {
			result.get(0).add(e1.getMessage());
				return result;
		}
		
		return result;
	}
	
	// Sends a Create Group request to the Database.
	// Return is from getMembership(userInfo);
	// Returns a two dimensional String Array of the form
	// [0][x] - Query Statuses
	// [x][0] - Group Name
	// [x][1] - Group ID
	// [x][2] - Group Password
	// [x][3] - Group Description
	// [x][4] - Group Creator ID
	// [x][5] - Group Google Drive Username
	// [x][6] - Group Google Drive Password
	public static ArrayList<ArrayList<String>> createGroup(String[] userInfo, String grpName, String grpDescription, 
	  String grpPassword){
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		ArrayList<String> row = new ArrayList<String>();
		ArrayList<ArrayList<String>> groups = new ArrayList<ArrayList<String>>();
		groups.add(row);
		
		if (!authenticate(userInfo)) { groups.get(0).add("Authentication Failed."); return groups; }
		
		grpName = validate(grpName);
		grpPassword = validate(grpPassword);
		grpDescription = validate(grpDescription);	
		
		String hash = new String();
		
		try {
			hash = PasswordHash.createHash(grpPassword);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		// Create the Group
		groups.get(0).add(sendMessage("INSERT INTO globus.Groups (group_name,creator,description,password) VALUES ('" + 
			grpName + "'," + 
			userInfo[2] + ",'" + 
			grpDescription + "','" + 
			hash + "')"));
		
		groups = getMembership(userInfo);
		
		// Create the Group
		groups.get(0).add(sendMessage("INSERT INTO globus.Calendar (group_id) VALUES ('" + groups.get(groups.size()-1).get(1) + "')"));
		
		// Tie User to Group for future queries
		groups.get(0).add(sendMessage("INSERT INTO globus.GroupMembers VALUES ('" + groups.get(groups.size()-1).get(1) + "','" + userInfo[2] +"')"));
		
		return groups;
	}
	
	// Sends a Join Group request to the Database.
	// Return is from getMembership(userInfo);
	// Returns a two dimensional String Array of the form
	// [0][x] - Query Statuses
	// [x][0] - Group Name
	// [x][1] - Group ID
	// [x][2] - Group Password
	// [x][3] - Group Description
	// [x][4] - Group Creator ID
	// [x][5] - Group Google Drive Username
	// [x][6] - Group Google Drive Password
	public static ArrayList<ArrayList<String>> joinGroup(String[] userInfo, String grpID, String grpPass) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		ArrayList<String> row = new ArrayList<String>();
		ArrayList<ArrayList<String>> groups = new ArrayList<ArrayList<String>>();
		groups.add(row);
		
		if (!authenticate(userInfo)) { groups.get(0).add("Authentication Failed."); return groups; }
		
		grpID = validate(grpID);
		grpPass = validate(grpPass);
		
		String hash = new String();
		
		try {
			ResultSet rs = sendQuery("SELECT password FROM globus.Groups WHERE group_id = '" + grpID + "'");
			if (!rs.first()){
					groups.get(0).add("No such group ID exists. Please try again.");
					return groups;
			} else hash = rs.getString(1);
			try {
				if(!PasswordHash.validatePassword(grpPass,hash)) {
					groups.get(0).add("Invalid password. ");
					return groups;
				}
			} catch (NoSuchAlgorithmException e) {
			} catch (InvalidKeySpecException e) { }
		} catch (SQLException e1) {
			groups.get(0).add(e1.getMessage());
		}
		
		groups.get(0).add(sendMessage("INSERT INTO globus.GroupMembers (group_id,user_id) VALUES ('" + 
				grpID + "','" + 
				userInfo[2] + "')"));
		
		return groups;
	}
	
	public static String dropTables() {
		sendMessage("DROP TABLE globus.EventMembers"); 
		sendMessage("DROP TABLE globus.Messages"); 
		sendMessage("DROP TABLE globus.Events"); 
		sendMessage("DROP TABLE globus.GroupMembers"); 
		sendMessage("DROP TABLE globus.Calendar"); 
		sendMessage("DROP TABLE globus.Groups");
		sendMessage("DROP TABLE globus.Users"); 
			statusLog = "Dropping of Tables successful.";
		return statusLog;
	}
	
	public static String createTables() {
			sendMessage("CREATE TABLE globus.Groups ( group_id INT NOT NULL AUTO_INCREMENT, group_name VARCHAR(255), creator INT, description VARCHAR(2500), password VARCHAR(255), google_username VARCHAR(255), google_password VARCHAR(255), PRIMARY KEY (group_id) )");
			sendMessage("CREATE TABLE globus.Users ( user_id INT NOT NULL AUTO_INCREMENT, name VARCHAR(255), email VARCHAR(255), password VARCHAR(255), biography VARCHAR(2500), last_update INT, PRIMARY KEY (user_id) )");
			sendMessage("CREATE TABLE globus.GroupMembers ( group_id INT, user_id INT, FOREIGN KEY(group_id) REFERENCES globus.Groups(group_id), FOREIGN KEY(user_id) REFERENCES globus.Users(user_id) )"); 
			sendMessage("CREATE TABLE globus.Calendar ( cal_id INT NOT NULL AUTO_INCREMENT, group_id INT, PRIMARY KEY(cal_id), FOREIGN KEY (group_id) REFERENCES globus.Groups(group_id) )");
			sendMessage("CREATE TABLE globus.Events ( cal_id INT, event_id INT NOT NULL AUTO_INCREMENT, time_start BIGINT, time_end BIGINT, name VARCHAR(255), description VARCHAR(2500), PRIMARY KEY(event_id), FOREIGN KEY (cal_id) REFERENCES globus.Calendar(cal_id) )");
			sendMessage("CREATE TABLE globus.EventMembers ( cal_id INT, event_id INT, user_id INT, permissions ENUM('Attendee','Organizer','Administrator','Creator'), FOREIGN KEY (cal_id) REFERENCES globus.Calendar(cal_id), FOREIGN KEY (event_id) REFERENCES globus.Events(event_id), FOREIGN KEY (user_id) REFERENCES globus.Users(user_id) )");
			sendMessage("CREATE TABLE globus.Messages ( message_id INT NOT NULL AUTO_INCREMENT, group_id INT, type ENUM('Announcement','Broadcast','Emergency','Normal'), contents VARCHAR(2500), timestamp BIGINT, PRIMARY KEY (message_id), FOREIGN KEY (group_id) REFERENCES globus.Groups(group_id) )");
			statusLog = "Creation of Tables successful.";
		return statusLog;
	}
}