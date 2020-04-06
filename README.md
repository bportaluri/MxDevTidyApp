# MxDevTidyApp

Analyze a Maximo application and detect all unused fields generating a simplified version of the same app removing all such fields.

## Installation

Extract MxDevTidyApp.zip folder in a directory of your choice.
Copy database JDBC drivers in the lib directory.
They can be found in [SMPDIR]\maximo\applications\maximo\lib

Edit MxDevTidyApp.properties entering database credentials.
These are used to verify used fields. No changes are made to the database.


## Usage

* Login to Maximo and clone the application that you want to simplify.
* Export the application definition. Save it under MxDevTidyApp folder.
* Edit the MxDevTidyApp.properties file according to your Maximo environment (DB2 and Oracle are supported)
* Edit the MxDevTidyApp.cmd and set the JAVABIN variable
* Launch MxDevTidyApp.cmd. The simplified XML file is generated
* Login to Maximo and import the out.xml application definition
