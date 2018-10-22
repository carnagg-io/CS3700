package huffman;

import java.io.OutputStream;
import java.io.IOException;

public class BitOutputStream {
    
    private final int BYTE = 8;
    
    private OutputStream out;
    private char[] buffer;
    private int count;
    
    BitOutputStream(OutputStream out) {
        this.out = out;
        buffer = new char[BYTE];
        count = 0;
    }
    
    public void write(char b) throws IOException {
        count++;
        buffer[BYTE - count] = b;
        if(count == BYTE) {
            int n = 0;
            for(int i = 0; i < BYTE; i++)
                n = 2 * n + (buffer[i] == '1' ? 1 : 0);
            out.write(n - 128);
            count = 0;
        }
    }
    
    public void close() throws IOException {
        int n = 0;
        for(int i = 0; i < BYTE; i++)
            n = 2 * n + (buffer[i] == '1' ? 1 : 0);
        out.write(n - 128);
        out.close();
    }
    
}
