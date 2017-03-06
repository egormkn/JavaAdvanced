@ECHO off
SET DOCS=".\doc"
@RD /S /Q %DOCS%
javadoc -locale ru src\ru\ifmo\ctddev\makarenko\implementor\package-info.java src\ru\ifmo\ctddev\makarenko\implementor\Implementor.java src\ru\ifmo\ctddev\makarenko\implementor\ClassWriter.java  -link http://docs.oracle.com/javase/8/docs/api -private -classpath tests\lib\hamcrest-core-1.3.jar;tests\lib\junit-4.11.jar;tests\lib\quickcheck-0.6.jar;tests\artifacts\JarImplementorTest.jar -d %DOCS%
