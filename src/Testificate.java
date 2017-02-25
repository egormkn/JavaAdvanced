import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Testificate<K, J extends String, E extends Map<? extends ArrayList<? extends String>, J>, T extends Writer & Appendable & Closeable & Flushable, N extends Throwable > {
    final int METHOD_MODIFIERS =
            Modifier.PUBLIC         | Modifier.PROTECTED    | Modifier.PRIVATE |
                    Modifier.ABSTRACT       | Modifier.STATIC       | Modifier.FINAL   |
                    Modifier.SYNCHRONIZED   | Modifier.NATIVE       | Modifier.STRICT;
    public E test() {
        return null;
    }

    public void test1() {

    }

    private void test2() {

    }

    protected void test3() {

    }

    private void test4(int[][]... j) {

    }
}
