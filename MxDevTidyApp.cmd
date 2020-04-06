@echo off

if [%1]==[] (
    echo Usage: MxDevTidyApp [INPUTFILE]
    goto :eof
)

java -classpath MxDevTidyApp/bin;MxDevTidyApp/lib/jdom.jar;MxDevTidyApp/lib/oraclethin.jar;MxDevTidyApp/lib/db2jcc.jar mxdev.tidyapp.TidyApp %1
