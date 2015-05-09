Project Globus
=========================================
>Project Globus is a group management mobile application, written for Android systems. The application is designed to work with a variety of groups, both casual and professional. Project Globus' targeted audience includes students working on a school project, fraternities managing philanthropic events and business teams working on their next biggest products by providing an innovative, simplistic and versatile experience to keep the group organized. Project Globus creates the experience via access to a remote server using a customized database to manage users, groups and other Globus services.  The services that Project Globus provides include access to file sharing, a group calendar, and group messaging.

###Members
- David Crane
- Kelsey Crea
- Jesse Miller
- Taylor Olson


##Server Walkthrough

* **Server:** The "Server" subfolder of the Source directory contains every version of the server code written to date, as well as some early demonstration .jars that were user early in development. GUI development became tedious, so all testing after Senior Design I was done through console execution and hard-coded or user-input statements.
  * A description of the SQL tables used in the database is available in this subfolder under "Tables Description.txt". The code used to create these tables is found within the "PGDB.java" file of the server code, under "createTables()".
  * The SQL connector used by the server is available under the "lib" folder within the "Server" subfolder.
  * The most functional server code can be found under "Server Code - 20150428". 

###Server Code - 20150428
The server's core functionality is to act as an intermediary between our database and the client-side applications. This is done through the "PGDB.java" (ProjectGlobusDataBase) class.

####PGDB.java
This class provides every application thread the functionaily it needs to interact with the database. All necessary commands are found as functions within this file. Further, this file provides basic security information so each thread can act on behalf of a single database user.

This file depends on "PasswordHash.java", a library of PBKDF2 password hasing by Taylor Hornby (c 2013) for account security within our application.

PGDB.java also provides the functionality client threads would need to validate user information, as well as properly escape any user information that may contain SQL command statements; information security was a must within our program.

The class must be initialized by at least one thread on the server end, and then each thread can independently call PGDB's functionality as specified by the user input.

All commands eventually call either "SendMessage()" or "sendQuery()" depending on the need for returned information. The purpose of each function is to properly validate and construct the necessary SQL commands.

Current supported commands include
* Create Account
* Login
* Modify all user information
* Create a new Group
* Join an Existing Group
* Leave a Group
* Get all messages within a week of a given timestamp.
* Send a Message to the group with types 'Announcement', 'Broadcast', 'Emergency', and 'Normal'
* Get all events within a group
* Create a new event within the group
* See all members within the group
* Assign attendance (add and remove attendees) to each event within the group
* Update all group information fields (creator only)
* Update a member's "last_updated" field for stronger data queries

All of these commands must be called a an assigned "ClientWorker.java", the workhorse of the "Server.java" class.

####Server.java
This class runs persistently on the host as a service, so it automatically reboots as the host does. It sits silently on the host, listening to port 63400 for incoming connections, and as they're made creates a new thread and assigns it to a "ClientWorker.java" class, and starts the new class.

####ClientWorker.java
This is the class that mediates all interactions from the client to the database, using the aforementioned PGDB.java class.

This class handles the authentication of each user, takes the tokenized client input and processes it for correct PGDB function calls, then reroutes the information back to each client.

The threads run independently, with variable logging functionality. Given that all database interactions are atomic, there is little need for thread and function-locking within the PGDB class. Further, the threads are written to shut down and require new authentication from the server should a conflict arise, or other errors occur.

Once each thread is created, it runs until a timeout occurs from the network driver, or until the connection is severed.

No persistent data is held in memory, due to usage limits on the current host.

####TestClient.java
This class was used for most of the in-development functionality testing prior to deployment on the application. The code inside is provided as-is, as it is only a debugging tool.

####RunnableClient.java
This class was used as a test case of TestClient.java. Maximal testing peaked at 75 concurrent users running randomized calls as quickly as possible. Testing showed minimal data choke points, and the slowest part of operation was network speeds.
