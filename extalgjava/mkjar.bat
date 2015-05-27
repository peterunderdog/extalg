set JAVAHOME=h:\jdk1.1.8
%JAVAHOME%\bin\javac -classpath "L:/extalg/extalgjava/deploy;L:\JavaSource\1.1collections\lib\collections.jar" -d L:/extalg/extalgjava/deploy -Xdepend SpecialBiserialApplet.java
pushd deploy
%JAVAHOME%\bin\jar cvf SpecialBiserial.jar *.class qtools quiver
copy SpecialBiserial.jar ..\..
del SpecialBiserial.jar
popd

