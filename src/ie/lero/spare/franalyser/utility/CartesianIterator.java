package ie.lero.spare.franalyser.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntFunction;

public class CartesianIterator<T> implements Iterator<T[]> {
   
	private final T[][] sets;
    private final IntFunction<T[]> arrayConstructor;
    private int count = 0;
    private T[] next = null;
    private T previous = null;
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    
    public CartesianIterator(T[][] sets, IntFunction<T[]> arrayConstructor) {
        Objects.requireNonNull(sets);
        Objects.requireNonNull(arrayConstructor);

        this.sets = copySets(sets);
        this.arrayConstructor = arrayConstructor;
    }

    
    private static <T> T[][] copySets(T[][] sets) {
        // If any of the arrays are empty, then the entire iterator is empty.
        // This prevents division by zero in `hasNext`.
        for (T[] set : sets) {
            if (set.length == 0) {
                return Arrays.copyOf(sets, 0);
            
            }
        }
        return sets.clone();
    }

    @Override
    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        
        int tmp = count;
        T[] value = arrayConstructor.apply(sets.length);
        for (int i = 0; i < value.length; i++) {
            T[] set = sets[i];

            int radix = set.length;
            int index = tmp % radix;
            
            value[i] = set[index];
          
            tmp /= radix;
        }

        if (tmp != 0) {
            // Overflow.
            return false;
        }

        next = value;
        count++;

        return true;
    }

    @Override
    public T[] next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        	
        }
       
        T[] tmp = next;
        next = null;
        return tmp;
    }
    
    public LinkedList<LinkedList<T>> iterateElements() {
    	
   	 int tmp = count;
        boolean isDuplicate =  false;
        LinkedList<T> value;
        int num = calculateNumberOfElements();
        LinkedList<LinkedList<T>> results = new LinkedList<LinkedList<T>>();
        
        for(;count<num;count++) {
       	 isDuplicate = false;
       	 tmp = count;
       	value = new LinkedList<T>();
       	// T[] value = arrayConstructor.apply(sets.length);
        for (int i = 0; i < sets.length; i++) {
            T[] set = sets[i];

            int radix = set.length;
            int index = tmp % radix;
            
            if(value.contains(set[index])) {
           	 isDuplicate = true;
     			break;
            }
            value.add(set[index]);
            tmp /= radix;
        }
   
        if(!isDuplicate) {
       	results.add(value);
        }
        }

      return results;
   }
   
   private int calculateNumberOfElements() {
   	
   	return (int) Math.pow(sets[0].length, sets.length);
   }
   
    public static void main(String [] args){
    	
    	//represents number of system assets that match each incident asset assuming
    			int rows = 8;
    			//represents number of incident assets
    			int columns = 10;
//    			String [] a = {"a", "b", "c"};
//    			System.out.println(Arrays.toString(a));
    			String [][] tst = new String[rows][columns];
    			int cnt = 0;
    			//generate dummy array assuming they are all unique
    			for(int i = 0;i<rows;i++) {
    				for(int j=0;j<columns;j++) {
    					tst[i][j] = i+""+j;//cnt;//dummy[rand.nextInt(dummy.length)];
    					cnt++;
    				}
    			}
    			
    	CartesianIterator<String> car = new CartesianIterator<String>(tst, String[]::new);
    	
    	System.out.println("Testing [The generation of unqiue sequences WITHOUT threads] using a "+rows+""
				+ "*"+columns+ "\nstatring time [" + dtf.format(LocalDateTime.now())+"]");
    	
    	 LinkedList<LinkedList<String>> res = car.iterateElements();
    	 
     	System.out.println("End time [" + dtf.format(LocalDateTime.now())+"]");
    	 System.out.println(res.size());
    }
}