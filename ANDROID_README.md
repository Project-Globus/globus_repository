Project Globus
=========================================
**Members**
* David Crane
* Kelsey Crea
* Jesse Miller
* Taylor Olson

* **Source:** The source code for the Android software is located in the “Documents” folder
under “GlobusAndroidAppFiles”. The file naming system consists of “ProjectGlobus_YYYYMMDD_HHmm”
YYYY = Four digit year, MM = Two digit month, DD = Two digit day, HH = Two digit hour (24 hour
time cycle), mm = Two digit minute. So for example, if someone were to submit revisions on May 6
at 8:13 pm, the name of the file would be “ProjectGlobus_20150506_2013.zip”

* **Documents:** Project Globus is a group management mobile application, written for Android systems. 
The application is designed to work with a variety of groups, both casual and professional. Project Globus’ 
targeted audience includes student’s working on a school project, fraternities managing philanthropic 
events and business teams working on their next biggest products by providing an innovative, simplistic 
and versatile experience to keep the group organized. Project Globus creates the experience via access 
to a remote server using a customized database to manage users, groups and other Globus services.  The 
services that Project Globus provides include access to file sharing, a group calendar, and group messaging.

* **Resources:** Caldroid, Jim Ward’s Mobile Programming Repository

TODO:

These instructions are under the assumption that Eclipse is set up with the Android SDK as found with
the instructions from this website: http://developer.android.com/sdk/installing/installing-adt.html

Importing the CalDroid Library:
[1]: Launch Eclipse 
[2]: Open the “CaldroidProject” folder, NOTE: separate import instructions are located in this folder
	if needed.
[3]: In Eclipse, right click in the workspace and select “Import”
[4]: Now you want to import a new project to your workspace. When selecting what type of file to import, go to the 
	Android subfolder and select "Existing Android Code Into Workspace"

Importing Globus:
[1]: Extract the most recent version of Project Globus out of the zip file.
[2]: In Eclipse, right click in the workspace and select “Import”
[3]: Click “General” and click “Add existing project to work space.”
[4]: Right click on “Globus” and then go to BuildPath -> ConfigureBuildPath
[5]: From the new screen that pops up, click on Android in the window on the left and then at the bottom of 
	the screen, inside the library window, you will see the reference to the Library project. Remove this
	reference and re add it, and you should be set.

Project Globus is designed with three Activities consisting of 16 fragments. 
The primary code you will be looking at is the Android Source code written in Java and the design layout code
written in XML.
	The Java code is located in Globus > src > project.globus.android
	The XML code is located in Globus > res > layout