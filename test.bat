@ECHO off

IF "%1" == "Walk" (
    java -Xmx20M -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/artifacts/WalkTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.walk.Tester %1 ru.ifmo.ctddev.makarenko.walk.Walk %2
)
IF "%1" == "RecursiveWalk" (
    java -Xmx20M -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/artifacts/WalkTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.walk.Tester %1 ru.ifmo.ctddev.makarenko.walk.RecursiveWalk %2
)
IF "%1" == "SortedSet" (
    java -Xmx20M -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ArraySetTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.arrayset.Tester %1 ru.ifmo.ctddev.makarenko.arrayset.ArraySet %2
)
IF "%1" == "NavigableSet" (
    java -Xmx20M -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ArraySetTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.arrayset.Tester %1 ru.ifmo.ctddev.makarenko.arrayset.ArraySet %2
)
IF "%1" == "class" (
    java -Xmx20M -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ImplementorTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2
)
IF "%1" == "interface" (
    java -Xmx20M -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ImplementorTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2
)
IF "%1" == "jar-class" (
    java -Xmx20M -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/JarImplementorTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2
)
IF "%1" == "jar-interface" (
    java -Xmx20M -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/JarImplementorTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2
)