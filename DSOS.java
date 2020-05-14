/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
//import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;



public class DSOS {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting  DSOS...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			//Third step: Create Broker
			DSOS_Scheduler broker = createBroker("Broker_0");
			int brokerId = broker.getId();

			//Fourth step: Create one virtual machine
			vmlist = new ArrayList<Vm>();

			//VM description
			int vmid = 0;
			int mips = 200;// chnage 40 250
			long size = 30000; //image size (MB)
			int ram = 100; //vm memory (MB)
			long bw = 2000;
			int pesNumber = 1; //number of cpus
			String vmm = "Xen"; //VMM name

			//create two VMs
			Vm vm1 = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

			//the second VM will have twice the priority of VM1 and so will receive twice CPU time
			vmid++;
			Vm vm2 = new Vm(vmid, brokerId, mips +600, pesNumber, ram+300, bw+1000, size*2, vmm, new CloudletSchedulerTimeShared());

			//add the VMs to the vmList
			vmlist.add(vm1);
			vmlist.add(vm2);

			//submit vm list to the broker
			broker.submitVmList(vmlist);


			//Fifth step: Create two Cloudlets
			cloudletList = new ArrayList<Cloudlet>();

			//Cloudlet properties
			int id = 0;
			long length = 4000000;
			long fileSize = 600;
			long outputSize = 600;
			UtilizationModel utilizationModel = new UtilizationModelFull();
			
			System.out.println("Enter the number of tasks you want  to create ");
			Scanner sc = new Scanner (System.in);
			int no_of_tasks=sc.nextInt();
			sc.close();
			
			
			for (int i=1;i<no_of_tasks+1;i++)
			{
				
				if(i%2==0) //this logic can be changed to change the skew of data 
				{
					
					//bigger tasks 
					Cloudlet cloudlet2 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
					
					cloudlet2.setUserId(brokerId);
					
					cloudletList.add(cloudlet2);
					
				}
				
				else
				{
					//smaller tasks 
					
					Cloudlet cloudlet1= new Cloudlet(id, length/100, pesNumber, fileSize/2, outputSize/2, utilizationModel, utilizationModel, utilizationModel);

					cloudlet1.setUserId(brokerId);
					cloudletList.add(cloudlet1);
				}
				id++;
				
			}

			
			
			
			
			broker.submitCloudletList(cloudletList);


			
			CloudSim.startSimulation();


			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

        	printCloudletList(newList);

			Log.printLine("DSOS finished!");
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){

			List<Host> hostList = new ArrayList<Host>();

	
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;

	
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

		
		int hostId=0;
		int ram = 500; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 5000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList,
    				new VmSchedulerTimeShared(peList)
    			)
    		); 
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;        
		double cost = 3.0;              
		double costPerMem = 0.05;		
		double costPerStorage = 0.001;	
		double costPerBw = 0.0;			
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DSOS_Scheduler createBroker(String name){

		DSOS_Scheduler broker = null;
		try {
			broker = new DSOS_Scheduler(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");
		
		DecimalFormat dft = new DecimalFormat("###.##");
		double time=0;
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			
			
			
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");
				time=time+cloudlet.getActualCPUTime();
				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
						indent + indent + dft.format(cloudlet.getFinishTime()));
			}
			
		}
		System.out.println("\n"+"The total time for execution is "+time);

	}
}
