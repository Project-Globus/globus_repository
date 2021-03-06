Project Globus Android Readme
=========================================
###Members
- David Crane
- Kelsey Crea
- Jesse Miller
- Taylor Olson



* **Source:** The source code for the Android software is located in the "Source" folder
under "GlobusAndroidAppFile". The file naming system consists of "ProjectGlobus_YYYYMMDD_HHmm"�
YYYY = Four digit year, MM = Two digit month, DD = Two digit day, HH = Two digit hour (24 hour
time cycle), mm = Two digit minute. So for example, if someone were to submit revisions on May 6
at 8:13 pm, the name of the file would be "ProjectGlobus_20150506_2013.zip"�

* **Resources:** Caldroid, Jim Ward's Mobile Programming Repository

##Walkthrough

These instructions are under the assumption that Eclipse is set up with the Android SDK as found with
the instructions from this website: http://developer.android.com/sdk/installing/installing-adt.html

Importing the CalDroid Library:

1. Launch Eclipse
2. Open the "CaldroidProject"� folder, NOTE: separate import instructions are located in this folder if needed.
3. In Eclipse, right click in the workspace and select "Import"
4. Now you want to import a new project to your workspace. When selecting what type of file to import, go to the Android subfolder and select "Existing Android Code Into Workspace"

Importing Globus:

1. Extract the most recent version of Project Globus out of the zip file.
2. In Eclipse, right click in the workspace and select "Import".
3. Click "General"� and click "Add existing project to work space".
4. Right click on "Globus"� and then go to BuildPath -> ConfigureBuildPath
5. From the new screen that pops up, click on Android in the window on the left and then at the bottom of the screen, inside the library window, you will see the reference to the Library project. Remove this reference and re add it, and you should be set.

Project Globus is designed with three Activities consisting of 16 fragments. 
The primary code you will be looking at is the Android Source code written in Java and the design layout code
written in XML.
- The Java code is located in Globus > src > project.globus.android
- The XML code is located in Globus > res > layout

##Navigation of Java Code
The Java code is broken up into 20 class files. The code that runs the main activities of the program, and the services that 
connect with the server, all have names beginning with "Globus_". When looking through our project, those would be the best 
places to begin looking. The rest of the files can be explored at your preference. Files ending with "_Screen" are the fragments 
of our app. All other files define more specific aspects of these fragments, and are referenced 
throughout the "_Screen" files.

##Navigation of XML Code
The XML code is broken up into 21 layout files. These layout files define the appearance of the app. The naming conventions for
these files are as follows:
		- The first word of every file designates which feature/section of the app the layout file is associated with.
		- Files with "_main_screen" are the primary layout files for each section.
		- All files without this are secondary layout files. They define more specific parts of the layout for each of the main files.
			- I.E. where the layout file "whiteboard_main_screen" contains a listview to be populated by messages and events,
				the layout file "whiteboard_listview" defines what each object in this list will look like.
			- Any files ending with "_dialog" are files defining the layout for pop-up windows that will appear on the designated screen
		
