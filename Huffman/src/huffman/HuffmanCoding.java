package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class contains methods which, when used together, perform the
 * entire Huffman Coding encoding and decoding process
 * 
 * @author Ishaan Ivaturi
 * @author Prince Rawal
 */
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * DO NOT EDIT
     * @param f The file we want to encode
     */
    public HuffmanCoding(String f) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
    public void makeSortedList() {
        /* Your code goes here */
        StdIn.setFile(fileName);

        sortedCharFreqList = new ArrayList<>();
        int[] probability = new int[128];
        double total = 0;

        while(StdIn.hasNextChar()){
            Character c = StdIn.readChar();
            probability[c]++;
            total++;
        }

        int amountOfChar = 0;
        char ch = 0;
        for(int i = 0; i < probability.length; i++){
            if(probability[i] >= 1){
                ch = (char) i;
                double a = probability[i]/total;
                CharFreq newChar = new CharFreq(ch, a);
                sortedCharFreqList.add(newChar);
                amountOfChar++;
            }
        }
        if(amountOfChar == 1){
            ch++;
            CharFreq extraChar = new CharFreq(ch, 0);
            sortedCharFreqList.add(extraChar);
        }
        Collections.sort(sortedCharFreqList);

	
    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {

	/* Your code goes here */
    Queue<CharFreq> source = new Queue<>();
    Queue<TreeNode> target = new Queue<>();

    for(int i = 0; i < sortedCharFreqList.size(); ++i){
        source.enqueue(sortedCharFreqList.get(i));
    }

    while(!source.isEmpty() || target.size() != 1){
        TreeNode leftNode = new TreeNode(null, null, null);
        TreeNode rightNode = new TreeNode(null, null, null);

        for(int i = 0; i <= 1; i++){
            CharFreq cf;
            TreeNode tn;
            if(target.isEmpty()){
                cf = source.dequeue();
                if(i == 0){
                    leftNode.setData(cf);
                }
                else{
                    rightNode.setData(cf);
                }
            }
            else if(source.isEmpty()){
                tn = target.dequeue();
                if(i == 0){
                    leftNode = tn;
                }
                else{
                    rightNode = tn;
                }
            }
            else{
                double targetProbOcc = target.peek().getData().getProbOcc();
                double sourceProbOcc = source.peek().getProbOcc();
                if(targetProbOcc >= sourceProbOcc){
                    cf = source.dequeue();
                    if(i == 0){
                        leftNode.setData(cf);
                    }
                    else{
                        rightNode.setData(cf);
                    }
                }
                else{
                    tn = target.dequeue();
                    if(i == 0){
                        leftNode = tn;
                    }
                    else{
                        rightNode = tn;
                    }
                }
            }
            
        }
        double probOccurenceSum = leftNode.getData().getProbOcc() + rightNode.getData().getProbOcc();
        CharFreq newCharFreq = new CharFreq(null, probOccurenceSum);
        TreeNode newTreeNode = new TreeNode(newCharFreq, leftNode, rightNode);
        target.enqueue(newTreeNode);
    }
    huffmanRoot = target.dequeue();   
}

public static void traverse(String[] characterCode, TreeNode node, String charValue){
    if(node == null){
        return;
    }
    if(node.getData().getCharacter() != null){
        char chr = node.getData().getCharacter();
        characterCode[chr] = charValue;
    }
    traverse(characterCode, node.getLeft(), charValue + "0");
    traverse(characterCode, node.getRight(), charValue + "1");
}
    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {

	/* Your code goes here */
    encodings = new String[128];
    traverse(encodings, huffmanRoot.getLeft(), "0");
    traverse(encodings, huffmanRoot.getRight(), "1");
    }


    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     * 
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);

	/* Your code goes here */
    String bitString = "";
    
        while(StdIn.hasNextChar()){
            char chr = StdIn.readChar();
            bitString += encodings[chr];
        }

        writeBitString(encodedFile, bitString);
    }
    
    /**
     * Writes a given string of 1's and 0's to the given file byte by byte
     * and NOT as characters of 1 and 0 which take up 8 bits each
     * DO NOT EDIT
     * 
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
    public static void writeBitString(String filename, String bitString) {
        byte[] bytes = new byte[bitString.length() / 8 + 1];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pad the string with initial zeroes and then a one in order to bring
        // its length to a multiple of 8. When reading, the 1 signifies the
        // end of padding.
        int padding = 8 - (bitString.length() % 8);
        String pad = "";
        for (int i = 0; i < padding-1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, add it to the right spot in the corresponding byte,
        // and store bytes in the array when finished
        for (char c : bitString.toCharArray()) {
            if (c != '1' && c != '0') {
                System.out.println("Invalid characters in bitstring");
                return;
            }

            if (c == '1') currentByte += 1 << (7-byteIndex);
            byteIndex++;
            
            if (byteIndex == 8) {
                bytes[bytesIndex] = (byte) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }
        
        // Write the array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            out.close();
        }
        catch(Exception e) {
            System.err.println("Error when writing to file!");
        }
    }

    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the 
     * tree, and writes it to a decoded file. 
     * 
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
    public void decode(String encodedFile, String decodedFile) {
        StdOut.setFile(decodedFile);

	/* Your code goes here */
    String bitString = readBitString(encodedFile);
        
        TreeNode firstroot = huffmanRoot;
        for(int i = 0; i < bitString.length(); i++){
            char ch = bitString.charAt(i);
            if(ch == '0'){
                huffmanRoot = huffmanRoot.getLeft();
            }
            else{
                huffmanRoot = huffmanRoot.getRight();
            }
            if(huffmanRoot.getData().getCharacter() != null){
                StdOut.print(huffmanRoot.getData().getCharacter());
                huffmanRoot = firstroot;
            }
        }
    }

    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's
     * representing the bits in the file
     * DO NOT EDIT
     * 
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
    public static String readBitString(String filename) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream(filename);
            File file = new File(filename);

            byte bytes[] = new byte[(int) file.length()];
            in.read(bytes);
            in.close();
            
            // For each byte read, convert it to a binary string of length 8 and add it
            // to the bit string
            for (byte b : bytes) {
                bitString = bitString + 
                String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            }

            // Detect the first 1 signifying the end of padding, then remove the first few
            // characters, including the 1
            for (int i = 0; i < 8; i++) {
                if (bitString.charAt(i) == '1') return bitString.substring(i+1);
            }
            
            return bitString.substring(8);
        }
        catch(Exception e) {
            System.out.println("Error while reading file!");
            return "";
        }
    }

    /*
     * Getters used by the driver. 
     * DO NOT EDIT or REMOVE
     */

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
