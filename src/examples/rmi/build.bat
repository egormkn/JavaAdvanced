@echo off
set classpath=../..

javac Server.java Client.java
rem call %java_home%\bin\rmic -d %classpath% examples.rmi.AccountImpl examples.rmi.BankImpl
