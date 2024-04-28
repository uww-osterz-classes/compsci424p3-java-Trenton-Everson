/* COMPSCI 424 Program 3
 * Name:
 * 
 * This is a template. Program3.java *must* contain the main class
 * for this program. 
 * 
 * You will need to add other classes to complete the program, but
 * there's more than one way to do this. Create a class structure
 * that works for you. Add any classes, methods, and data structures
 * that you need to solve the problem and display your solution in the
 * correct format.
 */

package Program3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

/**
 * Main class for this program. To help you get started, the major
 * steps for the main program are shown as comments in the main
 * method. Feel free to add more comments to help you understand
 * your code, or for any reason. Also feel free to edit this
 * comment to be more helpful.
 */
public class Program3 {
	// Declare any class/instance variables that you need here.
	static int[] available;
	static int[][] max;
	static int[][] allocation;
	static int[][] request;
	static int[] total;
	static int[][] potentialRequests;
	static int[] work;
	static boolean[] finish;
	static int numResources;
	static int numProcesses;
	static Semaphore sem = new Semaphore(1);

	/**
	 * @param args Command-line arguments. 
	 * 
	 * args[0] should be a string, either "manual" or "auto". 
	 * 
	 * args[1] should be another string: the path to the setup file
	 * that will be used to initialize your program's data structures. 
	 * To avoid having to use full paths, put your setup files in the
	 * top-level directory of this repository.
	 * - For Test Case 1, use "424-p3-test1.txt".
	 * - For Test Case 2, use "424-p3-test2.txt".
	 */

	public static void main(String[] args) 
	{

		
		if (args.length < 2) {
            System.err.println("Not enough command-line arguments provided, exiting.");
            return;
        }
        System.out.println("Selected mode: " + args[0]);
        System.out.println("Setup file location: " + args[1]);
        
		// 1. Open the setup file using the path in args[1]
		String currentLine;
		BufferedReader setupFileReader;

		try 
		{
			setupFileReader = new BufferedReader(new FileReader(args[1]));
		} catch (FileNotFoundException e) 
		{
			System.err.println("Cannot find setup file at " + args[1] + ", exiting.");
			return;
		}

		// 2. Get the number of resources and processes from the setup
		// file, and use this info to create the Banker's Algorithm
		// data structures

		// For simplicity's sake, we'll use one try block to handle
		// possible exceptions for all code that reads the setup file.

		try 
		{
			// Get number of resources
			currentLine = setupFileReader.readLine();

			if (currentLine == null) {
				System.err.println("Cannot find number of resources, exiting.");
				setupFileReader.close();
				return;
			}
			else 
			{
				numResources = Integer.parseInt(currentLine.split(" ")[0]);
				System.out.println(numResources + " resources");
			}

			// Get number of processes
			currentLine = setupFileReader.readLine();
			if (currentLine == null) 
			{
				System.err.println("Cannot find number of processes, exiting.");
				setupFileReader.close();
				return;
			}
			else 
			{
				numProcesses = Integer.parseInt(currentLine.split(" ")[0]);
				System.out.println(numProcesses + " processes");
			}

			// Create the Banker's Algorithm data structures, in any
			// way you like as long as they have the correct size

			available = new int[numResources];
			max = new int[numProcesses][numResources];
			allocation = new int[numProcesses][numResources];
			request = new int[numProcesses][numResources];
			total = new int [numResources];
			work = new int[numResources];
			finish = new boolean[numProcesses];
			potentialRequests = new int[numProcesses][numResources];


			// 3. Use the rest of the setup file to initialize the
			// data structures

			currentLine = setupFileReader.readLine();

			if (currentLine.equalsIgnoreCase("available"))
			{
				currentLine = setupFileReader.readLine();

				for(int i = 0; i < numResources; i++) //Parse the string into the available Array
				{
					available[i] = Integer.parseInt(currentLine.split(" ")[i]);
				}
				currentLine = setupFileReader.readLine();
			}
			else
			{
				System.err.println("File is not formatted properly | Looking for: Available | File read: " + currentLine);
				setupFileReader.close();
				return;
			}
			if (currentLine.equalsIgnoreCase("max"))
			{
				currentLine = setupFileReader.readLine();

				for(int i = 0; i < numProcesses; i++)
				{
					for(int j = 0; j < numResources; j++)
					{
						max[i][j] = Integer.parseInt(currentLine.split(" ")[j]);
					}
					currentLine = setupFileReader.readLine();
				}
			}
			else
			{
				System.err.println("File is not formatted properly | Looking for: max | File read: " + currentLine);
				setupFileReader.close();
				return;
			}
			if (currentLine.equalsIgnoreCase("allocation"))
			{
				currentLine = setupFileReader.readLine();

				for(int i = 0; i < numProcesses; i++)
				{
					for(int j = 0; j < numResources; j++)
					{
						allocation[i][j] = Integer.parseInt(currentLine.split(" ")[j]);
					}
					currentLine = setupFileReader.readLine();
				}
			}
			else
			{
				System.err.println("File is not formatted properly | Looking for: allocation | File read: " + currentLine);
				setupFileReader.close();
				return;
			}

			setupFileReader.close(); // done reading the file, so close it
		}
		catch (IOException e) 
		{
			System.err.println("Something went wrong while reading setup file "
					+ args[1] + ". Stack trace follows. Exiting.");
			e.printStackTrace(System.err);
			System.err.println("Exiting.");
			return;
		}

		// 4. Check initial conditions to ensure that the system is 
		// beginning in a safe state: see "Check initial conditions"
		// in the Program 3 instructions


		for (int i = 0; i < numProcesses; i++) //Check 1
		{
			for (int j = 0; j < numResources; j++)
			{
				if (!(allocation[i][j] <= max[i][j]))
				{
					System.err.println("Trying to allocate more than maximum allowed. Exiting.");
					return;
				}
			}
		}

		int[] tmp = new int[numResources];

		for(int i = 0; i < numResources; i++)
		{
			for(int j = 0; j < numProcesses; j++)
			{
				tmp[i] += allocation[j][i];
			}
		}

		for (int i = 0; i < numResources; i++)
		{
			total[i] = tmp[i] + available[i];
		}

		for (int i = 0; i < numProcesses; i++) //Calculate need
		{
			for (int j = 0; j < numResources; j++)
			{
				potentialRequests[i][j] = max[i][j] - allocation[i][j];
			}
		}

		for (int i = 0; i < available.length; i++)
		{
			work[i] = available[i];
		}

		if (IsSafe(available, max, allocation, potentialRequests) == true)
		{
			System.out.println("System is in a safe state");
		}
		else
		{
			System.out.println("System isn't in a safe state");
			return;
		}


		// 5. Go into either manual or automatic mode, depending on
		// the value of args[0]; you could implement these two modes
		// as separate methods within this class, as separate classes
		// with their own main methods, or as additional code within
		// this main method.
		if (args[0].matches("manual"))
		{
			ManualMode();
		}
		else if (args[0].matches("auto"))
		{
			AutomaticMode();
		}

	}

	static boolean IsSafe( int avail[], int max[][], int allot[][],int[][] need)
	{
		int [][]potentialRequests = new int[numProcesses][numResources];
		int[]   available = new int[numResources];

		for (int i = 0; i < numProcesses; i++)
		{
			for (int j = 0; j < numResources; j++)
			{
				potentialRequests[i][j] += need[i][j];
			}
		}
		for (int i = 0; i < numResources; i++)
		{
			available[i] += avail[i];
		}


		boolean []finish = new boolean[numProcesses];

		int count = 0;
		while (count < numProcesses)
		{
			boolean found = false;

			for (int p = 0; p < numProcesses; p++)
			{
				if (finish[p] == false)
				{
					int j;
					for (j = 0; j < numResources; j++)
					{
						if (potentialRequests[p][j] > available[j]){break;}
					}

					if (j == numResources)
					{
						for (int k = 0 ; k < numResources ; k++)
						{
							available[k] += allot[p][k];
						}
						count++;
						finish[p] = true;
						found = true;
					}
				}
			}
			if (found == false)
			{
				return false;
			}
		}
		return true;
	}


	static void ManualMode()
	{
		System.out.println("Manual Mode Started, please enter commands under this line");
		Scanner sc = new Scanner(System.in);
		String currLine = "";
		String[] replySplitUp = new String[6];

		while (true)
		{
			currLine = sc.nextLine();
			if (currLine.contains("end")) {break;}

			for (int i = 0; i < replySplitUp.length; i++)
			{
				replySplitUp[i] = currLine.split(" ")[i];
			}

			int currRequest = Integer.parseInt(replySplitUp[1]);
			int resource = Integer.parseInt(replySplitUp[3]);
			int process = Integer.parseInt(replySplitUp[5]);

			if (replySplitUp[0].contains("request"))
			{
				if (CheckRequest(currRequest, resource, process, allocation, request, potentialRequests, available) == true)
				{
					GrantRequest(currRequest, resource, process);
					System.out.println("Process " + process + " requests " + currRequest + " units of resource " + resource + ": granted");
				}
				else
				{
					System.out.println("Process " + process + " requests " + currRequest + " units of resource " + resource + ": DENIED");
				}

			}
			else if (replySplitUp[0].contains("release"))
			{
				CheckRelease(currRequest, resource, process, allocation, request, potentialRequests, available);
			}
		}
		sc.close();
	}

	static void AutomaticMode()
	{
		System.out.println("Automatic Mode Started");
		Thread[] threads = new Thread[numProcesses];

		for (int i = 0; i < numProcesses; i++)
		{
			threads[i] = new Thread(new Runnable() 
			{
				public void run() 
				{
					Random rnd = new Random();
					int randResource = 0;
					int randProcess = 0;
					int randRequest = 0;

					try {
						for (int j = 0; j < 3; j++) 
						{
							randResource = rnd.nextInt(numResources);
							randProcess = rnd.nextInt(numProcesses);
							randRequest = rnd.nextInt(10);
							sem.acquire();
							if (CheckRequest(randRequest, randResource, randProcess, allocation, request, potentialRequests, available) == true)
							{
								GrantRequest(randRequest, randResource, randProcess);
								System.out.println("Process " + randProcess + " requests " + randRequest + " units of resource " + randResource + ": granted");
							}
							else
							{
								System.out.println("Process " + randProcess + " requests " + randRequest + " units of resource " + randResource + ": DENIED");
							}
							sem.release();
							randResource = rnd.nextInt(numResources);
							randProcess = rnd.nextInt(numProcesses);
							randRequest = rnd.nextInt(10);
							sem.acquire();
							CheckRelease(randRequest, randResource, randProcess, allocation, request, potentialRequests, available);
							sem.release();
						}
					} catch (InterruptedException e)
					{
						System.err.println("no");
					}
					
				}

			});
			threads[i].start();
		}
	}

	static boolean CheckRequest(int request, int resource, int process, int[][]allocations, int[][]requests, int[][]potentials, int[]avail)
	{
		int[][] allocation = new int[numProcesses][numResources];
		int[][] currRequests = new int[numProcesses][numResources];
		int[][] potentialRequests = new int[numProcesses][numResources];
		int[]   available = new int[numResources];



		for (int i = 0; i < numProcesses; i++)
		{
			for (int j = 0; j < numResources; j++)
			{
				allocation[i][j] += allocations[i][j];
				currRequests[i][j] += requests[i][j];
				potentialRequests[i][j] += potentials[i][j];
			}
		}

		for (int i = 0; i < numResources; i++)
		{
			available[i] += avail[i];
		}

		allocation[process][resource] += request;
		currRequests[process][resource] += request;
		potentialRequests[process][resource] -= request;
		available[resource] -= request;

		if (IsSafe(available, max, allocation, potentialRequests) == true)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	static boolean CheckRelease(int request, int resource, int process, int[][]allocations, int[][]requests, int[][]potentials, int[]avail)
	{
		if (request >= 0 && allocations[process][resource] >= request)
		{
			allocations[process][resource] -= request;
			requests[process][resource] -= request;
			potentials[process][resource] += request;
			avail[resource] += request;
			System.out.println("Process " + process + " releases " + request + " units of resource " + resource);
			return true;
		}
		else
		{
			System.out.println("Process " + process + " cannot release " + request + " units of resource " + resource);
			return false;
		}
	}
	static void GrantRequest(int requestToBe, int resource, int process)
	{
		allocation[process][resource] += requestToBe;
		request[process][resource] += requestToBe;
		potentialRequests[process][resource] -= requestToBe;
		available[resource] -= requestToBe;
	}
}

