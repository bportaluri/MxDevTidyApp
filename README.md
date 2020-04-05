# MxDevTidyApp


## Installation

Extract MxDevTidyApp.zip folder in a directory of your choice.
Copy database JDBC drivers in the lib directory.
They can be found in [SMPDIR]\maximo\applications\maximo\lib

Edit MxDevTidyApp.properties entering database credentials.
These are used to verify used fields. No changes are made to the database.


## Usage

1. Login to Maximo and clone the application that you want to simplify.
2. Export the application definition. Save it under MxDevTidyApp folder.
3. Edit the MxDevTidyApp.properties file according to your Maximo environment (DB2 and Oracle are supported)
4. Edit the MxDevTidyApp.cmd and set the JAVABIN variable
5. Launch MxDevTidyApp.cmd. The simplified XML file is generated
6. Login to Maximo and import the out.xml application definition
