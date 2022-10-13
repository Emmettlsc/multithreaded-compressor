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

public class MultiThreadedGZipCompressor {
    public final static int BLOCK_SIZE = 131072;
    public final static int DICT_SIZE = 32768;
    private final static int GZIP_MAGIC = 0x8b1f;
    private final static int TRAILER_SIZE = 8;
    private Compressor cmpr;

    public ByteArrayOutputStream outStream;
    private CRC32 crc = new CRC32();

    private final BlockingQueue<Block> blockPool;

    public MultiThreadedGZipCompressor(int nThreads) {
        this.cmpr = new Compressor(nThreads);
        this.outStream = new ByteArrayOutputStream();
        this.blockPool = new LinkedBlockingQueue<>();
    }

    private void writeHeader() throws IOException {
        outStream.write(new byte[] {
                (byte) GZIP_MAGIC, // Magic number (short)
                (byte) (GZIP_MAGIC >> 8), // Magic number (short)
                Deflater.DEFLATED, // Compression method (CM)
                0, // Flags (FLG)
                0, // Modification time MTIME (int)
                0, // Modification time MTIME (int)
                0, // Modification time MTIME (int)
                0, // Modification time MTIME (int)Sfil
                0, // Extra flags (XFLG)
                0 // Operating system (OS)
        });
    }

    /*
     * Writes GZIP member trailer to a byte array, starting at a given
     * offset.
     */
    private void writeTrailer(long totalBytes, byte[] buf, int offset)
            throws IOException {
        writeInt((int) crc.getValue(), buf, offset); // CRC-32 of uncompr. data
        writeInt((int) totalBytes, buf, offset + 4); // Number of uncompr. bytes
    }

    /*
     * Writes integer in Intel byte order to a byte array, starting at a
     * given offset.
     */
    private void writeInt(int i, byte[] buf, int offset) throws IOException {
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     */
    private void writeShort(int s, byte[] buf, int offset) throws IOException {
        buf[offset] = (byte) (s & 0xff);
        buf[offset + 1] = (byte) ((s >> 8) & 0xff);
    }

    public void compress() throws FileNotFoundException, IOException {
        this.writeHeader();
        this.crc.reset();

        byte[] blockBuf = new byte[BLOCK_SIZE];
        byte[] dictBuf = new byte[DICT_SIZE];
        Deflater compressor = new Deflater(Deflater.DEFAULT_COMPRESSION, true);

        boolean lastBlock = false;
        long totalBytesRead = 0;
        boolean hasDict = false;
        int nBytes = System.in.read(blockBuf);
        totalBytesRead += nBytes;

        if (System.in.available() == 0) {
            lastBlock = true;
        }

        while (nBytes != -1) {
            // System.out.println(Arrays.toString(blockBuf));

            /* Update the CRC every time we read in a new block. */
            crc.update(blockBuf, 0, nBytes);

            Block cur = new Block();
            cur.setUncompressed(blockBuf);
            try {
                blockPool.put(cur);
            } catch (InterruptedException e) {
                System.err.println(e);
                System.exit(1);
            }

            cmpr.compress(cur, dictBuf, hasDict, nBytes, lastBlock);

            /*
             * If we read in enough bytes in this block, store the last part as the diction
             * arr for the next iteration
             */
            if (nBytes >= DICT_SIZE) {
                System.arraycopy(blockBuf, nBytes - DICT_SIZE, dictBuf, 0, DICT_SIZE);
                hasDict = true;
            } else {
                hasDict = false;
            }

            nBytes = System.in.read(blockBuf);
            totalBytesRead += nBytes;
            if (System.in.available() == 0) {
                lastBlock = true;
            }
        }
        cmpr.end(); // makes executer not wait for any more threads but lets them finish execution
        /* Finally, write the trailer and then write to STDOUT */

        for (Block item : blockPool) {
            outStream.write(item.getOutputStream().toByteArray());
            // System.out.println(Arrays.toString(item.getOutputStream().toByteArray()));
            // System.out.println(item.getOutputStream().toByteArray().length);
        }
        byte[] trailerBuf = new byte[TRAILER_SIZE];
        writeTrailer(totalBytesRead + 1, trailerBuf, 0);// -1 to account for when nBytes is -1
        outStream.write(trailerBuf);
        outStream.writeTo(System.out);
    }
}
