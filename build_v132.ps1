$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
Set-Location "C:\Users\ihebn\OneDrive\Documents\progects\SmarterMobs"
cmd.exe /c ".\gradlew.bat build --no-daemon"
