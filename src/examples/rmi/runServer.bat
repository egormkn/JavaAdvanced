@echo off
set classpath=../..

rmiregistry
java examples.rmi.Server
