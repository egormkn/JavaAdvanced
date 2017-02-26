@ECHO off

IF "%1" == "Walk" (
    java -Xmx20M -cp tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/artifacts/WalkTest.jar;./out/production/JavaAdvanced info.kgeorgiy.java.advanced.walk.Tester %1 ru.ifmo.ctddev.makarenko.walk.%1 %2
)
IF "%1" == "RecursiveWalk" (
    java -Xmx20M -cp tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/artifacts/WalkTest.jar;./out/production/JavaAdvanced info.kgeorgiy.java.advanced.walk.Tester %1 ru.ifmo.ctddev.makarenko.walk.%1 %2
)
IF "%1" == "SortedSet" (
    java -Xmx20M -cp tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ArraySetTest.jar;./out/production/JavaAdvanced info.kgeorgiy.java.advanced.arrayset.Tester %1 ru.ifmo.ctddev.makarenko.arrayset.ArraySet %2
)
IF "%1" == "NavigableSet" (
    java -Xmx20M -cp tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ArraySetTest.jar;./out/production/JavaAdvanced info.kgeorgiy.java.advanced.arrayset.Tester %1 ru.ifmo.ctddev.makarenko.arrayset.ArraySet %2
)
IF "%1" == "class" (
    java -Xmx20M -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ImplementorTest.jar;./out/production/JavaAdvanced;C:\Program Files\Java\jdk1.8.0_111\lib\tools.jar" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2 %3
)
IF "%1" == "interface" (
    java -Xmx20M -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ImplementorTest.jar;./out/production/JavaAdvanced;C:/Program Files/Java/jdk1.8.0_111/lib/tools.jar" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2 %3
)