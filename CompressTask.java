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

public class CompressTask implements Runnable {
    public boolean hasDic;
    public Block c_b;
    public byte[] dic;
    public final static int BLOCK_SIZE = 131072;
    public final static int DICT_SIZE = 32768;
    private final static int GZIP_MAGIC = 0x8b1f;
    Deflater compressor;
    int nBytes;
    boolean lastBlock;

    public CompressTask(Block cur, byte[] dic, boolean hasDic, int nBytes, boolean lastBlock) {
        this.c_b = cur;
        this.dic = dic;
        this.hasDic = hasDic;
        this.compressor = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        this.nBytes = nBytes;
        this.lastBlock = lastBlock;

        if (hasDic) {
            compressor.setDictionary(dic);
        }
    }

    public void run() {
        compressor.setInput(c_b.getUncompressed(), 0, nBytes);
        byte[] cmpBlockBuf = new byte[BLOCK_SIZE * 2];

        if (lastBlock) {
            /*
             * If we've read all the bytes in the file, this is the last block.
             * We have to clean out the deflater properly
             */
            if (!compressor.finished()) {
                compressor.finish();
                while (!compressor.finished()) {
                    int deflatedBytes = compressor.deflate(
                            cmpBlockBuf, 0, cmpBlockBuf.length, Deflater.NO_FLUSH);
                    if (deflatedBytes > 0) {
                        c_b.getOutputStream().write(cmpBlockBuf, 0, deflatedBytes);
                    }
                }
            }
        } else {
            int deflatedBytes = compressor.deflate(cmpBlockBuf, 0, cmpBlockBuf.length, Deflater.SYNC_FLUSH);
            if (deflatedBytes > 0) {
                c_b.getOutputStream().write(cmpBlockBuf, 0, deflatedBytes);
            }
        }

    }
}