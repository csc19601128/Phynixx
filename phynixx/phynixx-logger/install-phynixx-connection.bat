mvn install:install -DpomFile=target\pom-base.xml 
mvn install:install-file  -Dfile=target\phynixx-connection-1.0-SNAPSHOT.jar -Dversion=1.0-SNAPSHOT -DgroupId=org.csc.phynixx.phynixx -DartifactId=phynixx-connection -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
REM -DpomFile=pom.xml
