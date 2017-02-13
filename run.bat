@ECHO off
IF "%1" == "walk" (
    java -Xmx20M -cp tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/artifacts/WalkTest.jar;./out/production/JavaAdvanced info.kgeorgiy.java.advanced.walk.Tester Walk ru.ifmo.ctddev.makarenko.walk.Walk aaa
)
IF "%1" == "recursivewalk" (
    java -Xmx20M -cp tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/artifacts/WalkTest.jar;./out/production/JavaAdvanced info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.ctddev.makarenko.walk.RecursiveWalk
)