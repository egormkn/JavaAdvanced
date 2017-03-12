#!/bin/bash

echo " "
echo ">>> " "$1" "<<<"
echo " "

case "$1" in
    "Walk")
        java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar:tests/lib/junit-4.11.jar:./out/production/JavaAdvanced:tests/artifacts/WalkTest.jar" info.kgeorgiy.java.advanced.walk.Tester Walk ru.ifmo.ctddev.makarenko.walk.Walk $2 ;;
    "RecursiveWalk")
        java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar:tests/lib/junit-4.11.jar:./out/production/JavaAdvanced:tests/artifacts/WalkTest.jar" info.kgeorgiy.java.advanced.walk.Tester RecursiveWalk ru.ifmo.ctddev.makarenko.walk.RecursiveWalk $2 ;;
    "SortedSet")
        java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar:tests/lib/junit-4.11.jar:tests/lib/quickcheck-0.6.jar:./out/production/JavaAdvanced:tests/artifacts/ArraySetTest.jar" info.kgeorgiy.java.advanced.arrayset.Tester SortedSet ru.ifmo.ctddev.makarenko.arrayset.ArraySet $2 ;;
    "NavigableSet")
        java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar:tests/lib/junit-4.11.jar:tests/lib/quickcheck-0.6.jar:./out/production/JavaAdvanced:tests/artifacts/ArraySetTest.jar" info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet ru.ifmo.ctddev.makarenko.arrayset.ArraySet $2 ;;
    "interface")
        java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar:tests/lib/junit-4.11.jar:tests/lib/quickcheck-0.6.jar:./out/production/JavaAdvanced:tests/artifacts/ImplementorTest.jar" info.kgeorgiy.java.advanced.implementor.Tester interface ru.ifmo.ctddev.makarenko.implementor.Implementor $2 ;;
    "class")
        java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar:tests/lib/junit-4.11.jar:tests/lib/quickcheck-0.6.jar:./out/production/JavaAdvanced:tests/artifacts/ImplementorTest.jar" info.kgeorgiy.java.advanced.implementor.Tester class ru.ifmo.ctddev.makarenko.implementor.Implementor $2 ;;
    "jar-interface")
        java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar:tests/lib/junit-4.11.jar:tests/lib/quickcheck-0.6.jar:./out/production/JavaAdvanced:tests/artifacts/JarImplementorTest.jar" info.kgeorgiy.java.advanced.implementor.Tester jar-interface ru.ifmo.ctddev.makarenko.implementor.Implementor $2 ;;
    "jar-class")
        java -Dfile.encoding=UTF-8 -cp "tests/lib/hamcrest-core-1.3.jar:tests/lib/junit-4.11.jar:tests/lib/quickcheck-0.6.jar:./out/production/JavaAdvanced:tests/artifacts/JarImplementorTest.jar" info.kgeorgiy.java.advanced.implementor.Tester jar-class ru.ifmo.ctddev.makarenko.implementor.Implementor $2 ;;
    "all")
        #./test.sh Walk $2
        #./test.sh RecursiveWalk $2
        ./test.sh SortedSet $2
        ./test.sh NavigableSet $2
        ./test.sh interface $2
        ./test.sh class $2
        ./test.sh jar-interface $2
        ./test.sh jar-class $2
    ;;
    *) echo "Unexpected option" ;;
esac
