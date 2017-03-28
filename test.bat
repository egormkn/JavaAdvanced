@ECHO off

IF "%1" == "Walk" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/artifacts/WalkTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.walk.Tester Walk ru.ifmo.ctddev.makarenko.walk.Walk %2
)
IF "%1" == "RecursiveWalk" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/artifacts/WalkTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.ctddev.makarenko.walk.RecursiveWalk %2
)
IF "%1" == "SortedSet" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ArraySetTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.arrayset.Tester %1 ru.ifmo.ctddev.makarenko.arrayset.ArraySet %2
)
IF "%1" == "NavigableSet" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ArraySetTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.arrayset.Tester %1 ru.ifmo.ctddev.makarenko.arrayset.ArraySet %2
)
IF "%1" == "class" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ImplementorTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2
)
IF "%1" == "interface" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/ImplementorTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2
)
IF "%1" == "jar-class" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/JarImplementorTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2
)
IF "%1" == "jar-interface" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/JarImplementorTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.implementor.Tester %1 ru.ifmo.ctddev.makarenko.implementor.Implementor %2
)
IF "%1" == "scalar" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/IterativeParallelismTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.concurrent.Tester scalar ru.ifmo.ctddev.makarenko.concurrent.IterativeParallelism %2
)
IF "%1" == "list" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/IterativeParallelismTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.concurrent.Tester list ru.ifmo.ctddev.makarenko.concurrent.IterativeParallelism %2
)
IF "%1" == "scalarmapper" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/IterativeParallelismTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.mapper.Tester scalar ru.ifmo.ctddev.makarenko.concurrent.ParallelMapperImpl,ru.ifmo.ctddev.makarenko.concurrent.IterativeParallelism %2
)
IF "%1" == "listmapper" (
    java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar;tests/lib/junit-4.11.jar;tests/lib/quickcheck-0.6.jar;tests/artifacts/IterativeParallelismTest.jar;./out/production/JavaAdvanced" info.kgeorgiy.java.advanced.mapper.Tester list ru.ifmo.ctddev.makarenko.concurrent.ParallelMapperImpl,ru.ifmo.ctddev.makarenko.concurrent.IterativeParallelism %2
)