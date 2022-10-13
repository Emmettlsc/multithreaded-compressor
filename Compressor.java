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

public class Compressor {

    public int nThreads;
    protected ExecutorService compressExecutor;

    public Compressor(int nThreads) {
        this.nThreads = nThreads;
        compressExecutor = new ThreadPoolExecutor(nThreads, nThreads, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    public void compress(Block cur, byte[] dic, boolean hasDic, int nBytes, boolean lastBlock) {
        Runnable compressTask;

        compressTask = new CompressTask(cur, dic, hasDic, nBytes, lastBlock);
        compressExecutor.execute(compressTask);
    }

    public void end() {
        compressExecutor.shutdown();
        try {
            compressExecutor.awaitTermination(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }
}