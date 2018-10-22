/******************************************************************************
 * STUDENT     : Jordan Carnaggio
 * PROFESSOR   : Nima Davarpanah
 * COURSE      : Parallel Processing
 * ASSIGNMENT  : Project 1
 * DUE         : 22 October 2018
 * DESCRIPTION : Compresses a text file using Huffman coding and parallel
 *               processing.
 ******************************************************************************/

package huffman;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.PriorityQueue;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Huffman {

    private static final String IN_FILE_NAME = "in.txt";
    private static final String OUT_FILE_NAME = "out.txt";
    private static final int ALPHABET_SIZE = 256;
    
    private static char[] decoded;
    private static String[] coded;
    private static int[] frequencies;
    private static Node root;
    private static String[] codes;
    
    private static long averageTree = 0;
    private static long averageEncode = 0;
    
    private static class Node implements Comparable<Node> {
        
        public final char CHARACTER;
        public final int FREQUENCY;
        public final Node LEFT;
        public final Node RIGHT;
        
        Node(char character, int frequency, Node left, Node right) {
            CHARACTER = character;
            FREQUENCY = frequency;
            LEFT = left;
            RIGHT = right;
        }
        
        public boolean isLeaf() {
            return LEFT == null && RIGHT == null;
        }
        
        public int compareTo(Node that) {
            return this.FREQUENCY - that.FREQUENCY;
        }
        
    }
    
    private static class Coder implements Runnable {
        
        public final int FIRST;
        public final int LAST;
        
        Coder(int first, int last) {
            FIRST = first;
            LAST = last;
        }
        
        @Override
        public void run() {
            for(int i = FIRST; i < LAST; i++)
                coded[i] = codes[decoded[i]];
        }
        
    }
    
    public static void main(String[] args) {
        int repeatRun = 1000;
        int numThreads = 1;
        long average = 0;
        for(int i = 0; i < repeatRun; i++)
            average += compress(numThreads);
        average /= repeatRun;
        averageTree /= repeatRun;
        averageEncode /= repeatRun;
        System.out.println("... " + numThreads + " threads averaged tree " + averageTree + " ns.\n");
        System.out.println("... " + numThreads + " threads averaged encode " + averageEncode + " ns.\n");
        System.out.println("... " + numThreads + " threads averaged total " + average + " ns.\n");
    }
    
    public static long compress(int numThreads) {
        initializeCompression();
        long start = System.nanoTime();
        long then = System.nanoTime();
        processInput();
        buildTree();
        long now = System.nanoTime();
        averageTree += now - then;
        then = System.nanoTime();
        buildCodes();
        processOutput(numThreads);
        now = System.nanoTime();
        averageEncode += now - then;
        long finish = System.nanoTime();
        return finish - start;
    }
    
    public static void initializeCompression() {
        int fileLength = (int)new File(IN_FILE_NAME).length();
        decoded = new char[fileLength];
        coded = new String[fileLength];
        frequencies = new int[ALPHABET_SIZE];
        root = null;
        codes = new String[ALPHABET_SIZE];
    }
    
    public static void processInput() {
        try(BufferedReader reader = new BufferedReader(new FileReader(IN_FILE_NAME))) {
            int character, index = 0;
            while((character = reader.read()) != -1) {
                frequencies[character]++;
                decoded[index] = (char)character;
                index++;
            }
            reader.close();
        } catch(Exception e) { }
    }
    
    public static void buildTree() {
        PriorityQueue<Node> nodes = new PriorityQueue<>();
        for(int i = 0; i < ALPHABET_SIZE; i++)
            if(frequencies[i] > 0)
                nodes.add(new Node((char)i, frequencies[i], null, null));
        
        if(nodes.size() == 1) {
            if(frequencies['\0'] == 0)
                nodes.add(new Node('\0', 0, null, null));
            else
                nodes.add(new Node('\1', 0, null, null));
        }
        
        while(nodes.size() > 1) {
            Node left = nodes.poll();
            Node right = nodes.poll();
            Node parent = new Node('\0', left.FREQUENCY + right.FREQUENCY, left, right);
            nodes.add(parent);
        }
        root = nodes.poll();
    }
    
    public static void buildCodes() {
        buildCodes(root, "");
    }
    
    private static void buildCodes(Node node, String code) {
        if(!node.isLeaf()) {
            buildCodes(node.LEFT, code + '0');
            buildCodes(node.RIGHT, code + '1');
        } else {
            codes[node.CHARACTER] = code;
        }
    }
    
    private static void processOutput(int numThreads) {
        int chunkSize = decoded.length / numThreads;
        int offsetSize = chunkSize + (decoded.length % numThreads);
        
        ExecutorService executor = Executors.newCachedThreadPool();
        Thread[] coders = new Thread[numThreads];
        
        coders[0] = new Thread(new Coder(0, offsetSize));
        executor.execute(coders[0]);
        for(int i = 1; i < numThreads; i++) {
            coders[i] = new Thread(new Coder(offsetSize + (i - 1) * chunkSize,
                    offsetSize + i * chunkSize));
            executor.execute(coders[i]);
        }
        
        executor.shutdown();
        while(!executor.isTerminated());

        try {
            File outFile = new File(OUT_FILE_NAME);
            if(outFile.exists())
                outFile.delete();
            outFile.createNewFile();
            
            BitOutputStream writer = new BitOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(OUT_FILE_NAME)));
            for(String code : coded)
                for(int i = 0; i < code.length(); i++)
                    writer.write(code.charAt(i));
            writer.close();
        } catch(Exception e) { }
        
    }
    
    public static void printDecompress() {
        Node current = root;
        for(String code : coded) {
            for(int i = 0; i < code.length(); i++) {
                if(code.charAt(i) == '0')
                    current = current.LEFT;
                else
                    current = current.RIGHT;
                if(current.isLeaf()) {
                    System.out.print(current.CHARACTER);
                    current = root;
                }
            }
        }
    }
    
}
