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

    private static void treeNode(TreeNode f, TreeNode s, Queue<TreeNode> t){

        double sum = (firstValue(f)+firstValue(s));
        CharFreq targetSum = new CharFreq(null,sum);
        TreeNode targetNode = new TreeNode(targetSum,f,s);
        t.enqueue(targetNode);
    }
    private static double firstValue(TreeNode t){
        return t.getData().getProbOcc();
    }

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
        StdIn.setFile(fileName);
        sortedCharFreqList = new ArrayList<CharFreq>();
       int []array = new int[128];
       double count =0;
       while(StdIn.hasNextChar()){
           array[StdIn.readChar()]++;
           count++;
       }
       for(int i=0;i<=127;i++){
           if(array[i]!=0){
            sortedCharFreqList.add(new CharFreq((char)i, array[i]/count));
           }
       }
       if(sortedCharFreqList.size()==1){
           int secondValue = sortedCharFreqList.get(0).getCharacter();
           if(secondValue<127){
               secondValue++;
           }
           else{
               secondValue=0;
           }
           sortedCharFreqList.add(new CharFreq((char)secondValue,0));
       }
       Collections.sort(sortedCharFreqList);

    }

    /**
     * Uses sortedCharFreqList to build a huffman coding tree, and stores its root
     * in huffmanRoot
     */
    public void makeTree() {
        Queue<TreeNode> source = new Queue<>();
        Queue<TreeNode> target = new Queue<>();

        for(CharFreq c:sortedCharFreqList){
            TreeNode newNode = new TreeNode(c,null,null);
            source.enqueue(newNode);
        }

        while(!(source.isEmpty()&&target.size()==1)){
            TreeNode first;
            TreeNode second;
            if(source.isEmpty()){
                TreeNode tar1 = target.dequeue();
                TreeNode tar2 = target.dequeue();
                treeNode(tar1,tar2,target);
            }
            else if(target.isEmpty()){
                TreeNode sor1 = source.dequeue();
                TreeNode sor2 = source.dequeue();
                treeNode(sor1, sor2, target);
            }
            else{



                if(firstValue(source.peek())<=firstValue(target.peek())){
                    first = source.dequeue();
                }
                else{
                    first= target.dequeue();
                }
                if(source.isEmpty()){
                    second = target.dequeue();
                }
                else if(target.isEmpty()||(firstValue(source.peek())<=firstValue(target.peek()))){
                   second=source.dequeue();
                }
                else{
                    second = target.dequeue();
                }

                treeNode(first,second,target);
            }

        }

        TreeNode top = target.peek();
        huffmanRoot = top;
    }



    /**
     * Uses huffmanRoot to create a string array of size 128, where each
     * index in the array contains that ASCII character's bitstring encoding. Characters not
     * present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
    public void makeEncodings() {

        String [] encode = new String[128];
    String toBinary = "";

    encodes( huffmanRoot , toBinary , encode );
    encodings = encode;

    }
    private static void encodes(TreeNode huffmanRoot, String string, String[] arr){
        if(huffmanRoot.getData().getCharacter()==null){
            encodes(huffmanRoot.getLeft(),string+"0",arr);
            encodes(huffmanRoot.getRight(),string+"1",arr);

        }
        else{
            arr[huffmanRoot.getData().getCharacter()]=string;
        }
    }


    /**
     * Using encodings and filename, this method makes use of the writeBitString method
     * to write the final encoding of 1's and 0's to the encoded file.
     *
     * @param encodedFile The file name into which the text file is to be encoded
     */
    public void encode(String encodedFile) {
        StdIn.setFile(fileName);
        String tempFile="";
        while(StdIn.hasNextChar()){
            char num = StdIn.readChar();
            tempFile += encodings[(int)num];
        }
        writeBitString(encodedFile, tempFile);
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
        String readFile = readBitString(encodedFile);
       TreeNode r= huffmanRoot;
       decodedFile="";

       for(int i =0;i<readFile.length();i++){
           char character = (char)(readFile.charAt(i));
            if(character=='0'){
                r=r.getLeft();
                if(r.getData().getCharacter()!=null){
                    decodedFile+=r.getData().getCharacter();
                     r=huffmanRoot;
                }
            }
            else if(character =='1')
            {
                r=r.getRight();
                if(r.getData().getCharacter()!=null){
                    decodedFile+=r.getData().getCharacter();
                     r=huffmanRoot;
                }
            }

       }
       StdOut.print(decodedFile);

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
