/* Compress.java
 * Includes the main loop for user interaction as well as 
 * the compression algorithm, with necessary components.
 *
 * Author: Anthony Frago
 *
 * Last Modified: 12/04/24
 */
import java.util.Scanner;
import java.io.*;
    
class CompressLogFile{
  String name;
  String fName;
  double initialSize;
  double compressedSize;
  double time;
  int numEntries;
  int reHashCount;

  public CompressLogFile(CompressFile cFile){
    name=cFile.fName+".log";
    fName=cFile.fName;
    initialSize=(cFile.wc)*.001;
  }
  public void calcSize(){
    File f;
    try{
      //Gets the size without needing to read the file
      f=new File(fName+".zzz");
      compressedSize=f.length()*.001;
    }
    catch(Exception e){System.out.println(e.getMessage());}
  }
  public void printToFile(){
    try{
      PrintWriter w=new PrintWriter(new FileWriter(name));
      w.println(this.toString());
      w.close();
    } 
    catch(IOException e){System.out.println(e.getMessage());}
  }

  @Override
  public String toString(){
    String result="Compression of "+fName+
    "\nCompressed from "+String.format("%.3f",initialSize)+" Kilobytes "+
    "to "+String.format("%.3f",compressedSize)+" Kilobytes"+
    "\nCompression took "+String.format("%.3f",time)+" seconds"+
    "\nThe dictionary contains "+numEntries+" total entries"+
    "\nThe table was rehashed "+reHashCount+" times";
    return result;
  }
}
//Contains the needed information is a centralized object
class CompressFile{
  String fName;  
  int wc;
  BufferedReader br;

  public CompressFile(String fName){
    this.fName=fName;
  }
  //Useful for unpacking the uncompressed string to compress
  @Override
  public String toString(){
    String result="";

    try{
      String line;
      while((line=br.readLine())!=null)result+=line;
    }
    catch(IOException e){System.out.println(e.getMessage());}

    return result;
  }
}
//Class that simplifies writing operations during compression
class CompressWriter{
  ObjectOutputStream os; 

  public CompressWriter(String fName){
    try{
      os=new ObjectOutputStream(new FileOutputStream(fName+".zzz"));
    }  
    catch(IOException e){System.out.println(e.getMessage());}
  }
  public void write(int n){
    try{
      os.writeInt(n);
    }
    catch(IOException e){System.out.println(e.getMessage());}
  }
  public void close(){
    try{
      os.close(); 
    }
    catch(IOException e){System.out.println(e.getMessage());}
  }
}

public class Compress{
  //Ensuring that all given information will be sufficient for compression
  public static void checkCompressionFile(CompressFile cFile,Scanner s){
    String fName=cFile.fName;
    BufferedReader br;
    File f;

    while(true){
      try{
        f=new File(fName); 
        br=new BufferedReader(new FileReader(f));
      }
      catch(FileNotFoundException e){
        System.out.println("File Not Found");
        fName=myUtils.getFileName(s);
        continue;
      }
      break;
    }
    cFile.fName=fName;  
    cFile.wc=(int)f.length()*4;
    cFile.br=br;
  }

  public static void compress(CompressFile cFile){
    CompressLogFile log=new CompressLogFile(cFile); 
    CompressWriter w=new CompressWriter(cFile.fName);

    long start=System.currentTimeMillis();
    //Large enough to accomodate the ASCII table
    int initSize=256;
    ChainedHashMap map=new ChainedHashMap(initSize+cFile.wc);

    //Initialize the dictioary with all of the ASCI table
    for(int i=0;i<256;i++)map.put(""+(char)i,i);

    String p="";
    //Delivers the string to be compressed
    for(char c:cFile.toString().toCharArray()){
      String pc=p+c;
      if(map.contains(pc))p=pc;
      else{
        w.write((map.get(p)));
        map.put(pc,initSize++);
        p=""+c;
      }
    }
    if(!p.equals(""))w.write((map.get(p)));
    w.close();

    long end=System.currentTimeMillis();
    
    log.time=(end-start)*.001;
    log.calcSize();
    log.numEntries=map.numEntries;
    log.reHashCount=map.reHashCount;
    log.printToFile();

  }
  public static void main(String[] args){
    Scanner s=new Scanner(System.in);
    String fName;
    
    //Handle the cases for varying number of args
    if(args.length>1){
      System.out.println("Compress supports only single parameter arguments");
      fName=myUtils.getFileName(s);
    }
    if(args.length==0){
      fName=myUtils.getFileName(s);
    }
    else fName=args[0];

    CompressFile cFile=new CompressFile(fName);
    checkCompressionFile(cFile,s);
    compress(cFile);

    System.out.println("Would you like to run the program again?[y]");
    if(s.nextLine().equalsIgnoreCase("y"))main(new String[0]);
    else s.close();
  }
}
