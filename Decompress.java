/* Decompress.java
 * Includes the main loop for user interaction as well as 
 * the decompression algorithm, with necessary components.
 *
 * Author: Anthony Frago
 *
 * Last Modified: 12/04/24
 */
import java.util.Scanner;
import java.io.*;

class DecompressLogFile{
  String name;
  String fName;
  double time;
  int reHashCount;

  public DecompressLogFile(DecompressFile cFile){
    name=cFile.fName+".log";
    fName=cFile.fName;
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
    String result="Decompression of "+fName+
    "\nDecompression took "+String.format("%.3f",time)+" seconds"+
    "\nThe table was doubled "+reHashCount+" times";
    return result;
  }
}
//Data is used to group the first character separately from 
//the remainder of compressed data
class Data{
  int first;
  int[] cData;

  public Data(int first,int[] cData){
    this.first=first;
    this.cData=cData;
  }
}
class DecompressFile{
  String fName;  
  int wc;
  ObjectInputStream ois;

  public DecompressFile(String fName){
    this.fName=fName;
  }
  //Producing the compressed data formatted for decompression
  public Data getData(){
    int[] cData=new int[wc-1];
    int first=0;

    try{
      first=ois.readInt();
      int n;
      while((n=ois.readInt())!=-1){myUtils.insertInt(n,cData);}  
    }
    catch(EOFException e){}
    catch(IOException e){System.out.println(e.getMessage());}
    finally{
      if(ois!=null){
        try{
          ois.close();
        }
        catch(IOException e){
          System.out.println("Error Closing");
        }
      }
    }
    return new Data(first,cData);
  }
}
//Used to simplify writing operations during decompression
class DecompressWriter{
  PrintWriter pw; 

  public DecompressWriter(String fName){
    try{
      pw=new PrintWriter(new FileOutputStream(myUtils.strip(fName)));
    }  
    catch(IOException e){System.out.println(e.getMessage());}
  }
  public void write(String n){
    //Ignore null characters 
    if(!(n.charAt(0)=='\0'))pw.print(n);
  }
  public void close(){
    pw.close(); 
  }
}

public class Decompress{
  //Ensure we have recieved good information
  public static void checkDecompressionFile(DecompressFile dFile,Scanner s){
    String fName=dFile.fName;
    ObjectInputStream ois;
    File f;

    while(true){
      try{
        f=new File(fName); 
        ois=new ObjectInputStream(new FileInputStream(f));
      }
      catch(IOException e){
        System.out.println("File Not Found");
        fName=myUtils.getFileName(s);
        continue;
      }
      break;
    }
    dFile.fName=fName;  
    //f.length() returns number of bytes and int=4bytes
    dFile.wc=((int)f.length())*4;
    dFile.ois=ois;
  }

  public static void decompress(DecompressFile dFile){
    DecompressLogFile log=new DecompressLogFile(dFile); 
    DecompressWriter w=new DecompressWriter(dFile.fName);

    long start=System.currentTimeMillis();

    //covers all of the ASCI table
    int initSize=256;
    PerfectHashMap map=new PerfectHashMap(initSize+dFile.wc);

    //Inits with each element of ASCII table
    for(int i=0;i<256;i++)map.put(i,""+(char)i);
    
    Data data=dFile.getData();

    String p=""+(char)data.first;
    w.write(p);

    for(int s:data.cData){
      String item="";
      if(map.contains(s))item=map.get(s);
      else if(s==initSize)item=p+p.charAt(0);
      
      w.write(item);
      map.put(initSize++,p+item.charAt(0));
      p=item;
    }
    w.close();

    long end=System.currentTimeMillis();
    
    log.time=(end-start)*.001;
    log.reHashCount=map.reHashCount;
    log.printToFile();

  }
  public static void main(String[] args){
    Scanner s=new Scanner(System.in);
    String fName;
    
    //Handle the cases for varying number of args
    if(args.length>1){
      System.out.println("Decompress supports only single parameter arguments");
      fName=myUtils.getFileName(s);
    }
    if(args.length==0){
      fName=myUtils.getFileName(s);
    }
    else fName=args[0];

    DecompressFile dFile=new DecompressFile(fName);
    checkDecompressionFile(dFile,s);
    decompress(dFile);

    System.out.println("Would you like to run the program again?[y]");
    if(s.nextLine().equalsIgnoreCase("y"))main(new String[0]);
    else s.close();
  }
}
