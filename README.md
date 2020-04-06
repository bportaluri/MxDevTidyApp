# MxDevTidyApp

MxDevTidyApp is a small Java application to analyze an IBM Maximo application and detect all unused fields generating a simplified version of the same app removing all such fields.

Advantages of using MxDevTidyApp:
* Simplify user interaction with Maximo applications.
* Avoid use of untested/unsupported tabs and fields.
* Increase application load performances.

## Installation

Download latest version [MxDevTidyApp from GitHub](https://github.com/bportaluri/MxDevTidyApp.git).
Extract MxDevTidyApp.zip folder in a directory of your choice.
Edit MxDevTidyApp.properties entering database credentials. These are used to verify used fields. No changes are made to the database.


## Usage

* Open Maximo Application Designer and locate the application you want to tidy-up.
* Export the application XML definition. Save it under MxDevTidyApp folder.
* Launch MxDevTidyApp.cmd specifiying the XML file.
* Import the generated appname.tidy.xml application definition


## Change Log

### Version 1.1 (2020-04-06)
- Initial release
