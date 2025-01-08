/* myUtils.java
 * Includes useful utilities as well as 
 * the hashmaps for both compression and decompression
 *
 * Author: Anthony Frago
 *
 * Last Modified: 12/04/24
 */
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;

public class myUtils{

  public static String strip(String fName){
    int cutoff=fName.length()-4;
    char[] result=new char[cutoff];
    char[] name=fName.toCharArray();
    
    //Copy over each char up to the cutoff
    for(int i=0;i<cutoff;i++){result[i]=name[i];}

    return new String(result);
  }
  //Insertion methods are useful for inserting at the first open space
  //avaliable for various types
  public static void insert(Cell cell,Cell[] arr){
    for(int i=0;i<arr.length;i++){
      if(arr[i]==null){
        arr[i]=cell;
        return;
      }
    }
  }
  public static void insertD(DCell cell,DCell[] arr){
    for(int i=0;i<arr.length;i++){
      if(arr[i]==null){
        arr[i]=cell;
        return;
      }
    }
  }
  public static void insertInt(int n,int[] arr){
    for(int i=0;i<arr.length;i++){
      if(arr[i]==0){
        arr[i]=n;
        return;
      }
    }
  }
  public static String getFileName(Scanner s){
    System.out.println("Enter the name of the file");
    return s.nextLine();
  }
  public static int nearestPrime(int n){
    while(true){
      if(isPrime(n))return n;   
      else n++;
    }
  }
  public static boolean isPrime(int num){
    if(num<=1)return false; 
    for(int i=2;i<num;i++){if(num%i==0)return false;}
    return true; 
  }
}
//Cells are used as the entrys of a hashtable
class Cell{
  String rawKey;
  int value;

  public Cell(String key,int value){
    this.rawKey=key;
    this.value=value;
  }
  public String toString(){
    //Useful for debugging
    return String.format("%s:%d",rawKey,value);
  }
}
class DCell{
  int key;
  String value;

  public DCell(int key,String value){
    this.key=key;
    this.value=value;
  }
  public String toString(){
    //Should print out the key value pair 
    return String.format("%d:%s",key,value);
  }
}

class ChainedHashMap{
  LinkedList<Cell>[] dict;
  int ts;
  int numEntries;
  int reHashCount;
  private final double LFCAP=.75;

  @SuppressWarnings("unchecked")
  //Suppression is needed due to usage of generic array
  //In this case we are confident that Cell will be the exclusive type

  public ChainedHashMap(int ts){
    reHashCount=0;
    this.ts=myUtils.nearestPrime(ts); 
    dict=(LinkedList<Cell>[]) new LinkedList[this.ts];
  }
  
  private int hash(String key){
    return Math.abs(key.hashCode())%ts;  
  }
  public void put(String key,int value){
    numEntries++;
    int index=hash(key);    

    if(dict[index]==null){
      dict[index]=new LinkedList<Cell>();
      dict[index].add(new Cell(key,value));
      return;
    }
    else{
      //First check to see if that raw key already exists, if so update.
      //If it does not exist, then add it as a new cell 
      ListIterator<Cell> itr=dict[index].listIterator();
      while(itr.hasNext()){
        Cell cell=itr.next();

        if(cell.rawKey.equals(key)){
          cell.value=value;
          return;
        } 
      }
      dict[index].add(new Cell(key,value));
    }
    if((numEntries/ts)>LFCAP)reHash();
  }
  public int get(String key){
    int index=hash(key);

    if(dict[index]!=null){
      ListIterator<Cell> itr=dict[index].listIterator();

      while(itr.hasNext()){
        Cell cell=itr.next();
        if(cell.rawKey.equals(key))return cell.value;
      }
    }
    return -1;
  }
  public boolean contains(String key){
    int index=hash(key);

    if(dict[index]!=null){
      ListIterator<Cell> itr=dict[index].listIterator();

      while(itr.hasNext()){
        Cell cell=itr.next();
        if(cell.rawKey.equals(key)){
          return true;
        } 
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  //See above suppression 
  private void reHash(){
    //The current cells need to be stored seperately before replacing
    //the dict with a new one
    Cell[] oldCells=new Cell[numEntries];

    for(LinkedList<Cell> list:dict){
      ListIterator<Cell> itr=list.listIterator();
      while(itr.hasNext()){myUtils.insert(itr.next(),oldCells);}
    }

    ts=myUtils.nearestPrime(ts*2); 
    dict=(LinkedList<Cell>[]) new LinkedList[ts];
    
    //All cells need to be rehashed 
    for(Cell cell:oldCells){this.put(cell.rawKey,cell.value);}
    reHashCount++;
  }
  //Useful for verifying maps
  @Override
  public String toString(){
    String result="";
    for(LinkedList<Cell> list:dict){if(list!=null)result+=list;}
    return result;
  }
}
class PerfectHashMap{
  DCell[] dict;
  int ts;
  int numEntries;
  int reHashCount;
  private final double LFCAP=.75;

  public PerfectHashMap(int ts){
    reHashCount=0;
    this.ts=myUtils.nearestPrime(ts); 
    dict=new DCell[this.ts];
  }
  private int hash(int key){
    return Math.abs((key^31))%ts;
  }
  //Perfect hash table should not have conflicts
  public void put(int key,String value){
    numEntries++;
    dict[hash(key)]=new DCell(key,value);

    if((double)(numEntries/ts)>LFCAP)reHash();
  }
  public String get(int key){
    return dict[hash(key)].value;
  }
  public boolean contains(int key){
    DCell result=dict[hash(key)];

    if(result==null)return false;
    else return true;
  }
  public void reHash(){
    DCell[] oldCells=new DCell[numEntries];
    for(DCell cell:dict){if(!(cell==null))myUtils.insertD(cell,oldCells);}

    dict=new DCell[myUtils.nearestPrime(ts*2)];
    //Rehash each cell 
    for(DCell cell:oldCells){this.put(cell.key,cell.value);}
    reHashCount++;
  }
}

