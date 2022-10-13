import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ExecutorService;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.zip.*;
import java.util.*;

public class Block {
    public byte[] uncompressed;
    int index;
    public ByteArrayOutputStream compressed;
    public final static int BLOCK_SIZE = 131072;
    public final static int DICT_SIZE = 32768;
    private final static int GZIP_MAGIC = 0x8b1f;
    private final static int TRAILER_SIZE = 8;

    public Block() {
        this.uncompressed = new byte[BLOCK_SIZE];
        this.compressed = new ByteArrayOutputStream();
    }

    public byte[] getUncompressed() {
        return uncompressed;
    }

    public ByteArrayOutputStream getOutputStream() {
        return compressed;
    }

    public void setUncompressed(byte[] data) {
        uncompressed = data.clone();
    }
}
