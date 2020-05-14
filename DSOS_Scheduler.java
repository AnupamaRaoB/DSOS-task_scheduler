package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.Random;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.Log;



public class DSOS_Scheduler extends DatacenterBroker  {
	
	 public DSOS_Scheduler(String name) throws Exception {
			super(name);
			// TODO Auto-generated constructor stub
		}
	
	 private static ArrayList<ArrayList<Double>> ETC_Matrix=new ArrayList<ArrayList<Double>>();;
	 private static ArrayList<Integer> Xbest = null;
    
	 
	 public static  int getRandomOrganism( int numOrg, int i)
	 {
		 	int x=0;
			numOrg--;
		    Random rand = new Random();
		    while(numOrg>=0){
		         x = (int) rand.nextInt((numOrg));
		        if(x != i) return x;
		    }
		    return x;
		    
	}


		@SuppressWarnings("null")
		public static ArrayList<Integer> Mutualism(ArrayList<Integer> organismi, ArrayList<Integer> organismj, int no_of_vm)
		{
			Random rand = new Random();
			int f=rand.nextInt(3);
			int r =rand.nextInt(2);
			ArrayList<Double> temp1=new ArrayList<Double>();
			ArrayList<Integer> temp2=new ArrayList<Integer>();
			ArrayList<Integer> temp3=new ArrayList<Integer>();
			for(int i=0;i<organismj.size();i++)
			{
				
				temp1.add( (((organismi.get(i)+organismj.get(i))/(double)2)*f));
			}
			for (int i=0;i<organismj.size();i++)
			{
				
				temp2.add((int) Math.abs((temp1.get(i)-Xbest.get(i))*r));
			}
			
			
			
			for (int i=0;i<organismj.size();i++)
			{
				
				temp3.add(((temp2.get(i)+organismi.get(i))%no_of_vm));
			}
			
			return temp3;
		    
		}

		@SuppressWarnings("null")
		public static ArrayList<Integer> Commensalim(ArrayList<Integer> Organismj, int no_of_vm)
		{
			Random rand = new Random();
			int r = rand.nextInt(2);
			ArrayList<Integer> temp1=new ArrayList<Integer>();
			for (int i=0;i<Organismj.size();i++)
			{
				
				temp1.add(((Math.abs((Xbest.get(i)-Organismj.get(i)))*r)%no_of_vm));
			}
			return temp1; 
			
		}

		private static Double Fitness(ArrayList<Integer> organism){
		    Double tempi =  Double.MAX_VALUE;
		    Double tempj=   0.0;
		    for(int i = 0; i < organism.size(); i++){
		       
		    	tempj = ETC_Matrix.get(organism.get(i)).get(i);
		        if( tempi > tempj){
		        	tempi = tempj;
		        }
		    }
		    return tempi;
		}

		@SuppressWarnings("null")
		public static ArrayList<Integer> Parasitism(ArrayList<Integer> Organismi, int no_of_vm)
		{
			Random rand = new Random();
			int r = rand.nextInt(2);
			ArrayList<Integer> temp1=new ArrayList<Integer>();
			for (int i=0;i<Organismi.size();i++)
			{
				temp1.add((( (Organismi.get(i)) *r )%no_of_vm));
				
			}
			
			return temp1;
		    
		}

		private static void fillETC_matrix(ArrayList<Cloudlet> tasklist, ArrayList<Vm> vmlist)
		{
			 
		  
		    for(int i = 0; i < vmlist.size(); i++){
		    	ArrayList<Double> temp = new ArrayList<Double>();
		        for(int j = 0; j < tasklist.size(); j++)
		        {
		        	
		        	temp.add((double) tasklist.get(j).getCloudletLength() /  vmlist.get(i).getMips());
		        }
		        ETC_Matrix.add(temp);
		    }
			
			
			
			
		}
		
		
		 private static ArrayList<ArrayList<Integer>> generateOrganisms(int numOrg, int num_of_VM,int no_of_tasks)
		 {
		    	ArrayList<ArrayList<Integer>> organism = new ArrayList<ArrayList<Integer>>();
		    	Random rand = new Random();
				
		        for(int i = 0; i < numOrg; i++)
		        {
		        	
		        	ArrayList<Integer> temp = new ArrayList<Integer>();
		            for(int j = 0; j < no_of_tasks; j++)
		            {
		            	
		            	int r = rand.nextInt(num_of_VM);
		            	temp.add(r);
		            	
		            }
		            organism.add(temp);
		           
		        }
		        return organism;
		    }
		 
		 
		  
    
	
    
		protected void submitCloudlets(){
    	ArrayList<Cloudlet> tasklist = new ArrayList<Cloudlet>();
    	ArrayList<Vm> vmlist= new ArrayList<Vm>();
    	
    	for (Cloudlet cloudlet : getCloudletList()) {
    		
    			tasklist.add(cloudlet);
    	}
      
    	for (Vm vm : getVmList())
    	{
    		vmlist.add(vm);
    	}
       
    	   fillETC_matrix(tasklist, vmlist);
    	   	 int numOrg = 3;
    	     int iterations = 10;
    	     ArrayList<ArrayList<Integer>> organisms = generateOrganisms(numOrg, vmlist.size(), tasklist.size());
    	     int iteration_number=0;
    	     int i=0;
    	     Double best =Double.MAX_VALUE;
    	     
    	     
    	     while(iteration_number < iterations)
    	     {
    	    	 System.out.print("Iteracion:"+iteration_number+"\n");
    	    	//seecting best organism
    	    	 while(i<numOrg)
    	    	 {	
    	    		// i++;
    	    		 for(int j = 0; j < numOrg; j++)
    	    		 {
    	    			 Double temp = Fitness(organisms.get(j));
    	    			 if(best > temp)
    	    			 {
    	             	 best = temp;
    	             	 Xbest = organisms.get(i);
    	    			 }
    	    		 }
    	    		 
    	    	
    	    	int randonOrganismj = getRandomOrganism(numOrg, i); //selecting random organism to interact with i
    	    	 //mutulalism interaction
    	         ArrayList<Integer> xinew = Mutualism(organisms.get(i), organisms.get(randonOrganismj), vmlist.size());
    	         ArrayList<Integer> xjnew = Mutualism(organisms.get(randonOrganismj), organisms.get(i), vmlist.size());
    	         
    	         
    	         
    	         if(Fitness(organisms.get(i)) >Fitness(xinew) )
    	         {
    	             organisms.set(i, xinew);
    	         }
    	         
    	         if( Fitness(organisms.get(randonOrganismj))>Fitness(xjnew) )
    	         {
    	             organisms.set(randonOrganismj, organisms.get(i));
    	         }
    	         
    	         //commensalism interaction
    	         
    	         randonOrganismj = getRandomOrganism( numOrg, i);
    	         xinew= Commensalim(organisms.get(randonOrganismj), vmlist.size());
    	         if(Fitness(organisms.get(i)) > Fitness(xinew))
    	         {
    	             organisms.set(i, xinew);
    	         }
    	         
    	         //parasitism interaction
    	         
    	         randonOrganismj = getRandomOrganism( numOrg, i);
    	         ArrayList<Integer> xParasite  = Parasitism(organisms.get(i), vmlist.size());
    	         if(Fitness(organisms.get(randonOrganismj)) > Fitness(xParasite)){
    	             organisms.set(randonOrganismj,xParasite);
    	         }
    	         
    	         i++;
    	         
    	    	 }
    	    	 iteration_number++;	 
    	    	 
    	     }
    	     
    	     for(int loop = 0; loop < Xbest.size(); loop++){
    	         System.out.print(loop);
    	         System.out.print(" ");
    	         System.out.println(Xbest.get(loop));
    	     }
    	     System.out.print("Fitness: "+Fitness(Xbest));
    	    int vmIndex=0;

		for (Cloudlet cloudlet : tasklist) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(Xbest.get(vmIndex));
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1);// % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}

		
		
		
		
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
        
        
        
        
    }
   

}
