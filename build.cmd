
set JDKHOME=C:\GoogleDrive\Apps\Java\jdk7_x64\bin
set ZHOME=C:\GoogleDrive\Apps\Utils\7za
set DESTDIR=.\build


%JDKHOME%\jar cvf MxDevTidyApp.jar -C MxDevTidyApp/bin/ .

rmdir /S /Q %DESTDIR%
mkdir %DESTDIR%
mkdir %DESTDIR%\lib\*

move MxDevTidyApp.jar %DESTDIR%
copy MxDevTidyApp\lib\*.jar %DESTDIR%\lib
copy MxDevTidyApp.cmd %DESTDIR%
copy MxDevTidyApp\MxDevTidyApp.properties %DESTDIR%

pushd %DESTDIR%

%ZHOME%\7za a -tzip MxDevTidyApp.zip -r *.*

popd

move %DESTDIR%\MxDevTidyApp.zip .

rmdir /S /Q %DESTDIR%
