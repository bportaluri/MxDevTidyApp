@echo off

if [%1]==[] (
    echo Usage: MxDevTidyApp [INPUTFILE]
    goto :eof
)

java -classpath MxDevTidyApp.jar;lib/jdom.jar;lib/oraclethin.jar;lib/db2jcc.jar;lib/sqljdbc.jar mxdev.tidyapp.TidyApp %1
