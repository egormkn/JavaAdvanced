import java.io.Closeable;
import java.io.Flushable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

public abstract class Testificate<K, J extends String, E extends Map<? extends ArrayList<? extends String>, J>, T extends Writer & Appendable & Closeable & Flushable, N extends Throwable > {


    protected abstract void test();
}
