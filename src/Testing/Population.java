package Testing;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Population {

	//--important parameters
	//population Size
	int popSize = 600;
	//crossover rate
	double crossoverRate = 0.75; //can be changed where it starts high and gradually decrease (can be bw 0.45 - 0.95)
	//mutation rate
	double mutationRate = 0.20; //can be changed where it starts high and gradually decrease (can be bw 0.05 - 0.25)
	
	//population
	List<Individual> pop;
	
	//new population after crossover and mutation
	List<Individual> newPop;
	
	//fitness corresponding to population array
	double [] fitness;
	
	//best fit
//	Individual best;
	List<Individual> bestFits; // we can check if the bestfits repeat in number of generations as a termination criterion
	double [] bestFitsValues;       //also we can let the best fits go to the next generation without having to do selection and others  
	double fitnessSum=0;
	
	//is best fits selected based on top % (true) or fixed number of solutions 
	boolean isRatebased=true;
	double percentage = 0.1; //get the top #% (e.g., 10%)
	
	int numOfSolutions=5; //get the top # (e.g., 5) solutions
	
	//best fitness value
	double bestfitness;
	
	//number of generations
	int numOfGenerations=0;
	
	//for selection of new generation production.
	//parents selection based on rejection sampling, and mutation
	Individual p1;
	Individual p2;
	Individual child;
	
	public Population(){
		initialize();
		
	}
	
	public void initialize(){
		fitness = new double [popSize];
		pop = new ArrayList<Individual>();
		newPop = new ArrayList<Individual>();
	//	best = new Individual();
		bestFits = new ArrayList<Individual>();
		
		for(int i=0;i<popSize;i++){
		pop.add(new Individual());	
		fitness[i]=0.001;
		}
		
		//initially the best is the first individual in the population
		//best = pop.get(0);
		
		measurefitness();
		setBestFits();
	
	}
	
	//selection of the two parents for crossover
	//different methods of selection can be implemented such as rejection sampling
	public void select(){
		Random r1 = new Random();
		
		/**pool method of selection*/
		//create a pool where individuals with higher fitness have more copies of themselves
		/*int index=0;
		ArrayList<Individual> pool = new ArrayList<Individual>();
		if(isFitnessNonZero()){
		for (int i=0;i<fitness.length;i++){
			for(int j=0;j<fitness[i];j++){
				pool.add(pop.get(i));
			}
		}
		index = r1.nextInt(pool.size());
		p1 = pool.get(index);
		
		index = r1.nextInt(pool.size());
		p2 = pool.get(index);
		} else {
			index = r1.nextInt(pop.size());
			p1 = pop.get(index);
			
			index = r1.nextInt(pop.size());
			p2 = pop.get(index);
		}*/
		
		/**rejection sampling*/
		//create probability matrix
		/*int sum=1;
		boolean isAccepted = false;
		int hack = 0;
		int in1; 
		int perc=0;
		double ran; 
		double prob=0;
		for(int i=0;i<fitness.length;i++){
			sum+=fitness[i];
		}
		while(!isAccepted && hack<100000){
			in1 = r1.nextInt(pop.size());
			ran = r1.nextDouble();
			prob = fitness[in1]/sum;
			perc = (int)(prob*100);
		//	System.out.println("ran ="+ran);
			if((int)(ran*100)<=perc){
				isAccepted = true;
				p1 = pop.get(in1);
			}
			hack++;
		}
	//	System.out.println("hack for P1 ="+hack);
		
		isAccepted = false;
		hack=0;
		
		while(!isAccepted && hack<100000){
			in1 = r1.nextInt(pop.size());
			ran = r1.nextDouble();
			prob = fitness[in1]/sum;
			perc = (int)(prob*100);
		//	System.out.println("ran ="+ran);
			if((int)(ran*100)<=perc){
				isAccepted = true;
				p2 = pop.get(in1);
			}
			hack++;
		}*/
		//System.out.println("hack for P2 ="+hack);
		
		/**another way to choose depending on normalising fitness to probabilities*/
		//make sure that fitness is not all zero for all elements
		double sum = 0;
		int index=-1;
		double ran=0;
		
		for(int i=0;i<fitness.length;i++){
			sum+=fitness[i];
		}
		
		double [] prob = new double[fitness.length];
		for(int i=0;i<prob.length;i++){
			prob[i] = (double)fitness[i]/sum;
		}
		
		ran = r1.nextDouble();
		
		while(ran>=0){
			index++;
			ran-=prob[index];
		}
		p1 = pop.get(index);
		
		ran = r1.nextDouble();
		index=-1;
		while(ran>=0){
			index++;
			ran-=prob[index];
		}
		p2 = pop.get(index);
	}
	
/*	public void crossOver(){
		child = p1.crossOver(p2);
		
	}
	*/
	public void mutate(){
		Random r = new Random();
		child = pop.get(r.nextInt(pop.size()));
		child.mutate();
	}
	
	public void measurefitness(){
		fitnessSum =0;
		for (int i=0;i<popSize;i++){
			fitnessSum+= fitness[i] = pop.get(i).measurefitness();
			/*if (fitness[i] >= bestfitness){
				bestfitness = fitness[i];
				best = pop.get(i);
			}*/
		}
	}
	
	//this is based on the fitness values for determining best fits
	void updateBestFits(){
		
		//anything about the cutoff fitness is returned
		int size=0;
		double[] ary;
		ArrayList<Individual> inds = new ArrayList<Individual>();
		int index1=0;
		int index2=0;
		int temp=0;
		boolean isContained=false;
		
		//sort population and fitness
		quickSort(fitness, pop, 0, popSize-1);
		
		if(isRatebased) {
		///determine who many individuals are above cutoff
			size = (int)(popSize*percentage);
			
		} else {
			size = numOfSolutions;
		}
		
		//create new set of individuals who are above cutoff
		ary = new double[size];
	
		for(int j=0;j<size && temp<popSize;){
			if(!inds.contains(pop.get(temp))){
				ary[j] = fitness[temp];
				inds.add(pop.get(temp));
				j++;
			} 
			temp++;
			
		}
		
		//compare with current best individuals
		//if the maximum in the new is less or equal to the current no need for update otherwise we can compare
		if (ary[0] >= bestFitsValues[bestFitsValues.length-1]) {
			for(;index1<ary.length;index1++){
				for(;index2<bestFitsValues.length;index2++){
					if(ary[index1]>=bestFitsValues[index2]) {
						isContained = bestFits.contains(inds.get(index1));
						bestFitsValues[index2] = ary[index1]; 
						if(!isContained){
						bestFits.add(index2, inds.get(index1));
						bestFits.remove(bestFits.size()-1);
						}
						index2++;
						break;
						
					}
				}
				if(index2>=bestFitsValues.length){
					break;
				}
			}
			
			//if there are still elements in the new fitness that are above cutoff expand (to the new array size) the best fits array
			//the if statm means that if the last element in the current is replace with the corresponding in the new then replace the
			//whole current fitness with the new
			if(ary.length>bestFitsValues.length && bestFitsValues[bestFitsValues.length-1] == ary[bestFitsValues.length-1]){
				for(;index1<ary.length;index1++){
					bestFits.add(inds.get(index1));
				}
				//update best fitness values
				double [] newfit = new double[ary.length];
				//get old values of fitness
				for(int i=0;i<bestFitsValues.length;i++){
					newfit[i] = bestFitsValues[i];
				}
				//add new values of fitness
				bestFits = inds;
				bestFitsValues = ary;
			}
		}
		
		
	}
	
	//highest to lowest
	 void quickSort(double [] array, List<Individual> inds, int lowerIndex, int higherIndex) {
         
	        int i = lowerIndex;
	        int j = higherIndex;
	        // calculate pivot number, I am taking pivot as middle index number
	        double pivot = array[lowerIndex+(higherIndex-lowerIndex)/2];
	        // Divide into two arrays
	        while (i <= j) {
	            /**
	             * In each iteration, we will identify a number from left side which
	             * is greater then the pivot value, and also we will identify a number
	             * from right side which is less then the pivot value. Once the search
	             * is done, then we exchange both numbers.
	             */
	            while (array[i] > pivot) {
	                i++;
	            }
	            while (array[j] < pivot) {
	                j--;
	            }
	            if (i <= j) {
	                exchangeNumbers(array, inds, i, j);
	                //move index to next position on both sides
	                i++;
	                j--;
	            }
	        }
	        // call quickSort() method recursively
	        if (lowerIndex < j)
	            quickSort(array, inds, lowerIndex, j);
	        if (i < higherIndex)
	            quickSort(array, inds, i, higherIndex);
	    }
	 
	    private void exchangeNumbers(double [] array, List<Individual> inds, int i, int j) {
	        double temp = array[i];
	        Individual ind = pop.get(i);
	        
	        array[i] = array[j];
	        
	        inds.add(i, pop.get(j));
	        inds.remove(i+1);
	        
	        array[j] = temp;
	        inds.add(j, ind);
	        inds.remove(j+1);
	    }
	    
	public void newGeneration(){
		
		Random r = new Random();
		double ran;
		
		//create new population
		for (int i=0;i<popSize;i++){
			select();
			ran = r.nextDouble(); 
			if (ran <crossoverRate){
			child= p1.crossOver(p2);
			if(ran<mutationRate) {
				child.mutate();
			}
			} else {
				
				mutate();
			}
			
			newPop.add(child);
		}
		
		//replace old with new
		pop.clear();
		for (Individual ind:newPop){
			pop.add(ind);
		}
		newPop.clear();
		
		measurefitness();
		updateBestFits();
		numOfGenerations++;
	}
	
	void setBestFits() {
		
		int index =0;
		int index2=0;
		int size= (int)(popSize*percentage);
		
		//sort the population
		quickSort(fitness, pop, 0, popSize-1);
	
		//get all individuals who are above the cutoff
		for(int i=0;i<popSize;i++) {
			if(isRatebased) {
			if (!bestFits.contains(pop.get(i))){
				bestFits.add(pop.get(i));
				index2++;
				if(index2 == size){
					break;
				}
			}
			} else {
				if (!bestFits.contains(pop.get(i))){
					bestFits.add(pop.get(i));
				}
				index++;
				if(index == numOfSolutions) {
					break;
				}
			}
			
		}
		
		//assign fitness values to the best fits
		bestFitsValues = new double[bestFits.size()];
		
		for(int j=0;j<bestFitsValues.length;j++) {
			bestFitsValues[j] = fitness[j];
		}
		
		
	}
}
