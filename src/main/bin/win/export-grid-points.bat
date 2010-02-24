@echo off

set BEAM4_HOME=${installer:sys.installationDir}

"%BEAM4_HOME%\jre\bin\java.exe" ^
    -Xmx1024M ^
    -Dceres.context=beam ^
    "-Dbeam.mainClass=org.esa.beam.smos.visat.export.GridPointExporter" ^
    "-Dbeam.home=%BEAM4_HOME%" ^
    -jar "%BEAM4_HOME%\bin\ceres-launcher.jar" %*

exit /B 0
