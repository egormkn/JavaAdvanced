@ECHO off
REM ########################################################################    
REM #                                                                      #
REM #                  Universal Test Runner for Windows                   #
REM #                                                                      #
REM #                      https://github.com/egormkn                      #
REM #                                                                      #
REM ########################################################################    

SETLOCAL ENABLEEXTENSIONS ENABLEDELAYEDEXPANSION
SET scriptname=%~n0
SET v=1.0
SET workdir=%~dp0
SET shell=0
ECHO %CMDCMDLINE% | FINDSTR /L %COMSPEC% > NUL 2>&1 && SET shell=1
ECHO ############################
ECHO ##### Test script v%v% #####
ECHO ############################
ECHO/
CALL :main %1 %2 %3 %4 %5 %6 %7 %8 %9
IF "%shell%"=="1" PAUSE
EXIT /B %ERRORLEVEL%
 

REM ###########################  Main function  ############################    
:main
SET name=%1
SET salt=%2
SET package=ru.ifmo.ctddev.makarenko
SET classpath=tests/lib/hamcrest-core-1.3.jar tests/lib/junit-4.11.jar ./out/production/JavaAdvanced
2>NUL CALL :SETTINGS_%name% || CALL :SETTINGS_DEFAULT
IF "%name%" NEQ "all" (
  FOR %%A IN (%classpath%) DO (
    IF NOT DEFINED cp (
  	  SET cp=%%A
    ) ELSE (
  	  SET cp=!cp!;%%A
    )
  )
  CALL :test "!cp!" %tester% %name% %package%.%class% %salt%
) ELSE (
  ECHO ##### Running all tests #####
)
EXIT /B %ERRORLEVEL%

REM #############################  Settings  ###############################    
:SETTINGS_walk
:SETTINGS_hw1_easy
:SETTINGS_1_easy
  SET name=Walk
  SET classpath=%classpath% tests/artifacts/WalkTest.jar
  SET tester=info.kgeorgiy.java.advanced.walk.Tester
  SET class=walk.Walk
  GOTO END_CASE
:SETTINGS_recursive
:SETTINGS_recursivewalk
:SETTINGS_hw1_hard
:SETTINGS_1_hard
  SET name=RecursiveWalk
  SET classpath=%classpath% tests/artifacts/WalkTest.jar
  SET tester=info.kgeorgiy.java.advanced.walk.Tester
  SET class=walk.RecursiveWalk
  GOTO END_CASE
:SETTINGS_sorted
:SETTINGS_sortedset
:SETTINGS_hw2_easy
:SETTINGS_2_easy
  SET name=SortedSet
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/ArraySetTest.jar
  SET tester=info.kgeorgiy.java.advanced.arrayset.Tester
  SET class=arrayset.ArraySet
  GOTO END_CASE
:SETTINGS_navigable
:SETTINGS_navigableset
:SETTINGS_hw2_hard
:SETTINGS_2_hard
  SET name=NavigableSet
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/ArraySetTest.jar
  SET tester=info.kgeorgiy.java.advanced.arrayset.Tester
  SET class=arrayset.ArraySet
  GOTO END_CASE
:SETTINGS_interface
:SETTINGS_hw3_easy
:SETTINGS_3_easy
  SET name=interface
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/ImplementorTest.jar
  SET tester=info.kgeorgiy.java.advanced.implementor.Tester
  SET class=implementor.Implementor
  GOTO END_CASE
:SETTINGS_class
:SETTINGS_hw3_hard
:SETTINGS_3_hard
  SET name=class
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/ImplementorTest.jar
  SET tester=info.kgeorgiy.java.advanced.implementor.Tester
  SET class=implementor.Implementor
  GOTO END_CASE
:SETTINGS_jarinterface
:SETTINGS_hw4_easy
:SETTINGS_4_easy
  SET name=jar-interface
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/JarImplementorTest.jar
  SET tester=info.kgeorgiy.java.advanced.implementor.Tester
  SET class=implementor.Implementor
  GOTO END_CASE
:SETTINGS_jarclass
:SETTINGS_hw4_hard
:SETTINGS_4_hard
  SET name=jar-class
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/JarImplementorTest.jar
  SET tester=info.kgeorgiy.java.advanced.implementor.Tester
  SET class=implementor.Implementor
  GOTO END_CASE
:SETTINGS_scalar
:SETTINGS_hw6_easy
:SETTINGS_6_easy
  SET name=scalar
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/IterativeParallelismTest.jar
  SET tester=info.kgeorgiy.java.advanced.concurrent.Tester
  SET class=concurrent.IterativeParallelism
  GOTO END_CASE
:SETTINGS_list
:SETTINGS_hw6_hard
:SETTINGS_6_hard
  SET name=list
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/IterativeParallelismTest.jar
  SET tester=info.kgeorgiy.java.advanced.concurrent.Tester
  SET class=concurrent.IterativeParallelism
  GOTO END_CASE
:SETTINGS_scalarmap
:SETTINGS_hw7_easy
:SETTINGS_7_easy
  SET name=scalar
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/ParallelMapperTest.jar
  SET tester=info.kgeorgiy.java.advanced.mapper.Tester
  SET class=mapper.ParallelMapperImpl","ru.ifmo.ctddev.makarenko.mapper.IterativeParallelism
  GOTO END_CASE
:SETTINGS_listmap
:SETTINGS_hw7_hard
:SETTINGS_7_hard
  SET name=list
  SET classpath=%classpath% tests/lib/quickcheck-0.6.jar tests/artifacts/ParallelMapperTest.jar
  SET tester=info.kgeorgiy.java.advanced.mapper.Tester
  SET class=mapper.ParallelMapperImpl","ru.ifmo.ctddev.makarenko.mapper.IterativeParallelism
  GOTO END_CASE
:SETTINGS_DEFAULT
  SET /P name="Please, enter test name: "
  2>NUL GOTO SETTINGS_%name% || GOTO SETTINGS_DEFAULT
:SETTINGS_all
  CALL :main walk %salt%
  CALL :main recursive %salt%
  
  CALL :main sorted %salt%
  CALL :main navigable %salt%
  
  CALL :main interface %salt%
  CALL :main class %salt%
  
  CALL :main jarinterface %salt%
  CALL :main jarclass %salt%
  
  CALL :main scalar %salt%
  CALL :main list %salt%
  
  CALL :main scalarmap %salt%
  CALL :main listmap %salt%
  
  GOTO END_CASE
:END_CASE
  VER > NUL
  GOTO :EOF


REM Test function
REM @param %1 classpath
REM @param %2 tester
REM @param %3 version
REM @param %4 main class
REM @param %5 salt
:test
ECHO Running java -Dfile.encoding=UTF-8 -cp %1 %2 %3 %4 %5
ECHO/
java -Dfile.encoding=UTF-8 -cp %1 %2 %3 %4 %5
EXIT /B 0