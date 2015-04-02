
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;

public class PGDB {
	private static final String hostname = "globus2.csarh2gerrai.us-west-2.rds.amazonaws.com"; //db.projectglobus.com
	private static final String port = "3307";
	private static final String database = "globus";
	private static final String user = "globus";
	private static final String password = "";

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
			statusLog = "PGDB: MySQL Driver successfully initialized.\n";
		} catch (Exception ex) {
			statusLog = "PGDB: ERROR - MySQL Driver failed to initialize.\n";
        }
		
		// Create Connection to database.
		// TODO: Create beanstalk daemon to handle secure information connection
		try {
			connection = DriverManager.getConnection(
                    "jdbc:mysql://" + hostname + ":" + port + "/" + database ,
                    user, password);
			
			statusLog += "PGDB: Connection to Database established.\n";
			
			// Mark PGDB as initialized.
			INIT = true;
		} catch (SQLException ex){
			statusLog += "PGDB: ERROR - SQL EXCEPTION" +
					 "\nSQLException: " + ex.getMessage() +
					 "\nSQLState: " + ex.getSQLState() +
					 "\nVendorError: " + ex.getErrorCode();
		}
		
		return statusLog + "";
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
			statusLog = "ERROR: SQL EXCEPTION" +
					 "\nSQLException: " + ex.getMessage() +
					 "\nSQLState: " + ex.getSQLState() +
					 "\nVendorError: " + ex.getErrorCode();
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
			statusLog = "ERROR: SQL EXCEPTION" +
					 "\nSQLException: " + ex.getMessage() +
					 "\nSQLState: " + ex.getSQLState() +
					 "\nVendorError: " + ex.getErrorCode();
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
				result[0] = "Login Successful! Welcome " + result[1] + ".";
				
				//result[0] += ". Attempting Authentication: " + (authenticate(result) ? "success" : "failed");
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
			ResultSet rs = sendQuery("SELECT group_name,Groups.group_id,password,description,creator,google_username,google_password FROM globus.Groups, globus.GroupMembers WHERE Groups.group_id=GroupMembers.group_id and GroupMembers.user_id = '" + userInfo[2] + "'");
			if (!rs.first()){
				row.add("ERROR RETRIEVING INFORMATION FROM TABLE");
				result.add(row);
				return result;
			} else {
				row = new ArrayList<String>();
				row.add("Query Successful.");
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
			}
		} catch (SQLException e1) {
			result.get(0).add(e1.getMessage());
				return result;
		}
		
		return result;
	}
	
	// Sends a Create Group request to the Database.
	// Returns a string of query statuses. 
	// Must call getMembership for newly updated list.
	public static String createGroup(String[] userInfo, String grpName, String grpDescription, 
	  String grpPassword){
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String status = "";
		
		if (!authenticate(userInfo)) { status = "Authentication Failed."; return status; }
		
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
		status += sendMessage("INSERT INTO globus.Groups (group_name,creator,description,password) VALUES ('" + 
			grpName + "'," + 
			userInfo[2] + ",'" + 
			grpDescription + "','" + 
			hash + "')");
		
		// Create the Group
		status += sendMessage("INSERT INTO globus.Calendar (group_id) SELECT MAX(group_id) FROM globus.Groups");
		
		// Tie User to Group for future queries
		status += sendMessage("INSERT INTO globus.GroupMembers VALUES ((SELECT MAX(group_id) FROM globus.Groups),'" + userInfo[2] +"')");
		
		
		return status;
	}
	
	// Sends a Join Group request to the Database.
	// Returns a string of query statuses. 
	// Must call getMembership for newly updated list.
	public static String joinGroup(String[] userInfo, String grpID, String grpPass) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String status = "";
		
		if (!authenticate(userInfo)) { status = "Authentication Failed."; return status; }
		
		grpID = validate(grpID);
		grpPass = validate(grpPass);
		
		String hash = new String();
		
		try {
			ResultSet rs = sendQuery("SELECT password FROM globus.Groups WHERE group_id = '" + grpID + "'");
			if (!rs.first()){
					status += "No such group ID exists. Please try again.";
					return status;
			} else hash = rs.getString(1);
			try {
				if(!PasswordHash.validatePassword(grpPass,hash)) {
					status += "Invalid password. ";
					return status;
				}
			} catch (NoSuchAlgorithmException e) {
			} catch (InvalidKeySpecException e) { }
		} catch (SQLException e1) {
			status += e1.getMessage();
		}

		status += sendMessage("INSERT INTO globus.GroupMembers (group_id,user_id) VALUES ('" + 
				grpID + "','" + 
				userInfo[2] + "')");
		
		return status;
	}
	
	public static ArrayList<ArrayList<String>> getMessages(String[] userInfo, String grpID, String timestamp) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		ArrayList<String> row = new ArrayList<String>();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		result.add(row);
		
		grpID = validate(grpID);
		timestamp = validate(timestamp);
		
		try {
			ResultSet rs = sendQuery("SELECT group_name FROM globus.GroupMembers WHERE group_id = '" + grpID + "' AND user_id ='" + userInfo[2] + "'");
			if (!rs.first()){
					result.get(0).add("Member does not belong to group.");
					return result;
			} else {
				rs = sendQuery("SELECT message_id,group_id,type,contents,timestamp FROM globus.Messages WHERE group_id = " + grpID + " and timestamp>"+ (Integer.parseInt(timestamp)-(86400*7)) + " ORDER BY timestamp ASC");
				if (!rs.first()){
					result.get(0).add("ERROR RETRIEVING MESSAGES! ");
					return result;
				} else {
					while (!rs.isAfterLast()) {
						row = new ArrayList<String>();
						row.add(rs.getString(1));
						row.add(rs.getString(2));
						row.add(rs.getString(3));
						row.add(rs.getString(4));
						row.add(rs.getString(5));
						result.add(row);
						rs.next();
					}
				}
			}
			
		} catch (SQLException e1) {
			result.get(0).add(e1.getMessage());
		}
		
		return result;
	}
	
	public static String addMessage(String[] userInfo, String grpID, String message, String type, String timestamp) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		ArrayList<String> row = new ArrayList<String>();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		result.add(row);
		
		String response = "";
		
		grpID = validate(grpID);
		timestamp = validate(timestamp);
		message = validate(message);
		type = validate(type);
		
		try {
			ResultSet rs = sendQuery("SELECT group_name FROM globus.GroupMembers WHERE group_id = '" + grpID + "' AND user_id ='" + userInfo[2] + "'");
			if (!rs.first()){
					response = "Member does not belong to group.";
					return response;
			} else {
				response = sendMessage("INSERT INTO globus.Messages (group_id,contents,type,timestamp) VALUES ('" + 
				grpID + "','" + 
				message + "','" + 
				type + "','" + 
				timestamp + "')");
			}
			
		} catch (SQLException e1) {
			result.get(0).add(e1.getMessage());
		}
		return response;
	}
	
	public static ArrayList<ArrayList<String>> getGroupMembers(String[] userInfo, String grpID) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		ArrayList<String> row = new ArrayList<String>();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		result.add(row);
		
		grpID = validate(grpID);
		
		try {
			ResultSet rs = sendQuery("SELECT group_name FROM globus.GroupMembers WHERE group_id = '" + grpID + "' AND user_id ='" + userInfo[2] + "'");
			if (!rs.first()){
				result.get(0).add("Member does not belong to group.");
				return result;
			} else {
				rs = sendQuery("SELECT user_id,name,email,biography FROM globus.Users, globus.GroupMembers WHERE GroupMembers.group_id = '" + grpID + "'");
				if (!rs.first()){
						result.get(0).add("ERROR RETRIEVING MEMBERS");
						return result;
				} else {
					while (!rs.isAfterLast()) {
						row = new ArrayList<String>();
						row.add(rs.getString(1));
						row.add(rs.getString(2));
						row.add(rs.getString(3));
						row.add(rs.getString(4));
						result.add(row);
						rs.next();
					}
				}
			}
		} catch (SQLException e1) {
			result.get(0).add(e1.getMessage());
		}
		
		return result;
	}

	public static String updateGDesc(String[] userInfo, String grpID, String desc) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String response = "";
		
		grpID = validate(grpID);
		desc = validate(desc);
		
		try {
			ResultSet rs = sendQuery("SELECT creator FROM globus.Groups WHERE group_id = '" + grpID + "'");
			if (!rs.first()){
				response = "Group does not exist.";
				return response;
			} else {
				if (rs.getString(1) != userInfo[2]) response = "You are not the creator of the group.";
				else 
					response = sendMessage("UPDATE globus.Groups SET description='" + desc + "' WHERE group_id='" + grpID + "'");
			}
		} catch (SQLException e1) {
			response = e1.getMessage();
		}
		
		return response;
	}
	
	public static String updateGName(String[] userInfo, String grpID, String name) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String response = "";
		
		grpID = validate(grpID);
		name = validate(name);
		
		try {
			ResultSet rs = sendQuery("SELECT creator FROM globus.Groups WHERE group_id = '" + grpID + "'");
			if (!rs.first()){
				response = "Group does not exist.";
				return response;
			} else {
				if (rs.getString(1) != userInfo[2]) response = "You are not the creator of the group.";
				else 
					response = sendMessage("UPDATE globus.Groups SET name='" + name + "' WHERE group_id='" + grpID + "'");
			}
		} catch (SQLException e1) {
			response = e1.getMessage();
		}
		
		return response;
	}
	
	public static String updateGPass(String[] userInfo, String grpID, String pass) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String response = "";
		
		grpID = validate(grpID);
		pass = validate(pass);
		
		try {
			ResultSet rs = sendQuery("SELECT creator FROM globus.Groups WHERE group_id = '" + grpID + "'");
			if (!rs.first()){
				response = "Group does not exist.";
				return response;
			} else {
				if (rs.getString(1) != userInfo[2]) response = "You are not the creator of the group.";
				else 
				{
					String hash = "";
					
					try {
						hash = PasswordHash.createHash(pass);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
						return "Error updating password";
					} catch (InvalidKeySpecException e) {
						e.printStackTrace();
						return "Error updating password";
					}
					response = sendMessage("UPDATE globus.Groups SET password='" + hash + "' WHERE group_id='" + grpID + "'");
			
				}
			}
		} catch (SQLException e1) {
			response = e1.getMessage();
		}
		
		return response;
	}

	public static String updateGoogleInfo(String[] userInfo, String grpID, String username, String pass) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String response = "";
		
		grpID = validate(grpID);
		pass = validate(pass);
		username = validate(username);
		
		try {
			ResultSet rs = sendQuery("SELECT creator FROM globus.Groups WHERE group_id = '" + grpID + "'");
			if (!rs.first()){
				response = "Group does not exist.";
				return response;
			} else {
				if (rs.getString(1) != userInfo[2]) response = "You are not the creator of the group.";
				else 
				{
					String hash = "";
					
					try {
						hash = PasswordHash.createHash(pass);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
						return "Error updating Google information.";
					} catch (InvalidKeySpecException e) {
						e.printStackTrace();
						return "Error updating Google information";
					}
					response = sendMessage("UPDATE globus.Groups SET google_password='" + hash + "', SET google_username='" + username + "' WHERE group_id='" + grpID + "'");
			
				}
			}
		} catch (SQLException e1) {
			response = e1.getMessage();
		}
		
		return response;
	}
	
	public static String updateBio(String[] userInfo, String bio) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String response = "";
		
		if (!authenticate(userInfo)) { response = "Authentication Failed."; return response; }
		
		bio = validate(bio);
		
		response = sendMessage("UPDATE globus.Users SET biography='" + bio + "' WHERE user_id='" + userInfo[2] + "'");
		
		return response;
	}
	
	public static String updateRecency(String[] userInfo, String timestamp) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String response = "";
		
		if (!authenticate(userInfo)) { response = "Authentication Failed."; return response; }
		
		timestamp = validate(timestamp);
		
		response = sendMessage("UPDATE globus.Users SET last_update='" + timestamp + "' WHERE user_id='" + userInfo[2] + "'");
		
		return response;
	}
	
	public static String updateName(String[] userInfo, String name) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String response = "";
		
		if (!authenticate(userInfo)) { response = "Authentication Failed."; return response; }
		
		name = validate(name);
		
		response = sendMessage("UPDATE globus.Users SET name='" + name + "' WHERE user_id='" + userInfo[2] + "'");
		
		return response;
	}
	
	public static String updatePass(String[] userInfo, String pass) {
		for (int i = 0; i < userInfo.length; ++i)
			userInfo[i] = validate(userInfo[i]);
		
		String response = "";
		
		if (!authenticate(userInfo)) { response = "Authentication Failed."; return response; }
		
		pass = validate(pass);
		
		String hash = "";
		
		try {
			hash = PasswordHash.createHash(pass);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "Error updating password";
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			return "Error updating password";
		}
		
		response = sendMessage("UPDATE globus.Users SET password='" + hash + "' WHERE user_id='" + userInfo[2] + "'");
		
		return response;
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