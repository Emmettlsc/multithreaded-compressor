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

public class Pigzj {
  public static boolean isStringInt(String s) {
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  public static void main(String[] args) throws IOException {
    int nThreads = Runtime.getRuntime().availableProcessors();
    if (args.length != 0) {
      if (!args[0].equals("-p") || !(isStringInt(args[1])) || args.length > 2) {
        System.err.println("Invalid arguments. Accepted argumets are:\n\t\"-p <num of desired threads>\"");
        System.exit(1);
      } else {
        nThreads = Integer.parseInt(args[1]);
      }
    }
    MultiThreadedGZipCompressor cmp = new MultiThreadedGZipCompressor(nThreads);
    cmp.compress();
  }
}