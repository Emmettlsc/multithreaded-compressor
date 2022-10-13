# Multithreaded Compressor

* A Java program which operates multiple compression threads to improve wall-clock compression performance. Each compression thread acts on an input block size of 128KiB along with a 32KiB dictionary from the previous block. The performance gain is large when compressing large files (> 1GB). A more detailed look at perfomance can be found in performance.txt. The compression format follows the GZIP file format standard, Internet RFC 1952 so other utilities can be used to decompress the output. 

## Running

Build your modified version.
```
javac $(find . -name '*.java')
```

Pipe input to compress 
```
cat <input file> | java Pigzj
```

You can specify the number of threads using
```
java -p <# of desired compression threads> Pigzj
```
If unspecified, the number of threads defaults to the number of processors avaliable to the JVM