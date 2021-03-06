package edu.ucalgary.ensf409;

import java.io.*;
import java.util.*;

/**
 * 
 * @author Adeshpal Virk, Hannah Oluyemisi Asaolu, Tyler Galea, Cole Barraclough
 * @since March 27, 2021
 * @version 1.0
 * {@summary} This class takes in the user input for 
 * a) furnitrure category b)its type c) the number of items requested
 * 
 * This class also  generates an order form as a .txt file if the request can be filled.
 * Otherwise, it outputs the names of suggested manufacturers
 *
 */

public class User {
	private static String filename = "Orders/orderform1.txt";
	private static Management myJDBC;
	private static FurnitureBuilder builder;
	/**
	 * mysql URL 
	 */
	protected static final String URL = "jdbc:mysql://localhost/inventory"; //Change the url if neccasary
	/**
	 * mysql Username 
	 */
	protected static final String USERNAME = "root"; //Add your username
	/**
	 * mysql Password 
	 */
	protected static final String PASSWORD = "Cole_8899"; //Add your password
	
	
	//INSTRUCTIONS TO RUN PROGRAM CAN BE FOUND IN README.md

	/**
	 * The main class that takes in user requests and tries
	 * to proccess the data
	 * @param args[] An optional command line arguement
	 */
	public static void main (String[] args) {
		try {
			Scanner sc = new Scanner(System.in);
			
			System.out.print("Please enter a furniture category:");
			String val1 = sc.nextLine();
			while (isAlpha(val1) == false) {
				System.out.println("Enter a valid category containing only alphabetic characters");
				val1 = sc.nextLine();
			}
			String category = formatString(val1);
			
			System.out.print("Please enter a furniture type:");
			String val2 = sc.nextLine();
			while (isAlpha(val2) == false) {
				System.out.println("Enter a valid type containing only alphabetic characters");
				val2 = sc.nextLine();
			}
			String type = formatString(val2);
			
			String number = "";
			boolean natural = false;
			while(!natural){	
				System.out.print("Please enter the number of items you need:");
				number = sc.next();
				if(isNum(number)){
					if(Integer.parseInt(number) > 0){
						natural = true;
					} else {
						System.out.println("Please enter a number greater than zero");
					}
				} else {
					System.out.println("Please enter a valid integer");
				}		
			}
			sc.close();
			System.out.println("You requested "  + number + " "+ category.trim() + " " + "of type" + " "+ type);
			myJDBC = new Management(URL,USERNAME,PASSWORD);
			myJDBC.initializeConnection();
			String[] input1 = {type.trim(),category.trim(),number};
			myJDBC.createArray(input1);
			myJDBC.toArray();
			builder = new FurnitureBuilder();
			if(!builder.buildFurniture(myJDBC.listOfFurnitures,Integer.parseInt(number))) {
				System.out.println("Order cannot be fulfilled based on current inventory. Suggested manufacturers are " +myJDBC.manuIDstoNames(myJDBC.manufacturers(myJDBC.getListOfFurniture()))+".");
			}
			else {
				orderForm(type.trim(),category.trim(),number,builder);
				myJDBC.updateDatabase(category.trim(),builder);
			}
			myJDBC.close();
		} catch (IllegalArgumentException e) {
			System.out.println("The furniture requested does not exist.");
		}
		catch(Exception e){
			System.out.println("The system encountered an error.");
			
		}	
	}


	/**
	 * Tests to see if the input is all 
	 * characters in the alphabet or spaces
	 * @param str the input to test
	 * @return true if it is all alphabet or spaces
	 */
	public static boolean isAlpha(String str) {
	   char [] arr = str.toCharArray();
	   
	   for (int i = 0; i < arr.length; i++) {
		   if ( !((arr[i] >= 'a'  && arr[i] <='z') || (arr[i] >= 'A' && arr[i] <= 'Z') || (arr[i] == ' ' ))) {
			   return false;
		   }
	   }
	   return true;
		
	}
	
	/**
	 * Tests to see if the input is a number
	 * @param num the input to test
	 * @return true if it is a number
	 */
	public static boolean isNum(String num) {
		try {
			Integer.parseInt(num);
		}
		catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Changes the format of a String to all capital first letters in between spaces
	 * @param arg the string to change
	 * @return the formatted string
	 */
	public static String formatString(String arg) {
		String str = arg.trim();
		String result ="";
		String[] arr = str.split("\\s");
		
		int i=0;
		
		while(i<arr.length){
			String test = arr[i];
			result += test.substring(0,1).toUpperCase();
			result += test.substring(1).toLowerCase();
			result+=" ";
			i++;
		}
		return result;
	}
	
	/**
	 * Generates the order form and makes a new file
	 * @param type The type of furniture
	 * @param category the category of furniture
	 * @param number The number of items made
	 * @param builder the furniture builder object
	 */
	public static void orderForm(String type,String category,String number,FurnitureBuilder builder) {
		File form = new File(filename);
		BufferedWriter writer = null;
		
		//Filename must have been specified
		if (filename == null) {
			System.err.printf("Filename must be specified");
		    System.exit(1);
		}
		
		try {	//Create the file if it doesnt exist
			File directory = new File("Orders");
			if (! directory.exists()){
				directory.mkdir();
				// If you require it to make the entire directory path including parents,
				// use directory.mkdirs(); here instead.
			}
			int i=1;
			while(form.exists()) {
				String yes = "Orders/orderform";
				yes += Integer.toString(i);
				yes += ".txt";
				filename = yes;
				i++;
				form = new File(filename);
			}
			form.createNewFile();
			
		}
		catch (IOException e) {
		      System.out.println("An error occurred in creating the file.");
		      e.printStackTrace();
		}
		String faculty = "";
		String contact = "";
		String date = "";
		
		//Now write the order to the file
		try {
			writer = new BufferedWriter(new FileWriter(filename));
			writer.write("Furniture Order Form " + "\n\n" + "Faculty Name: " + faculty + "\n");
			writer.write("Contact: " + contact +"\n" + "Date: " + date + "\n\n");
			
			writer.write("Original Request: ");
			writer.write(type.toLowerCase() +" "+ category.toLowerCase() + ", " + number+ "\n\n");
			
			writer.write("Items Ordered" + "\n");
			String[] ID = builder.getIds();
			System.out.print("Purchase ");
			for(int i =0; i< ID.length ;i++) {
				writer.write("ID: " + ID[i] + "\n");
				if(i == (ID.length-1)) {
					System.out.print(" and ");
				}
				System.out.print(ID[i]);
				if(!(i >= (ID.length-2))) {
					System.out.print(", ");

				}
			}
			String price = Integer.toString(builder.getPrice());
			System.out.print(" for $"+price+".");


			writer.write("\n" +"Total Price: $" + price+".");
		}
		catch (Exception e) {
		      System.err.println("I/O error opening/writing file.");
		      System.err.println(e.getMessage());
		      System.exit(1);
		    }
		
		//Close the file
		finally {
			try {
				if (writer != null) {
					writer.close();
				}
			}catch(IOException e) {
				System.err.println("Error closing file.");
				System.exit(1);
			}
			
		}
		
		
	}

}
