package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

	private static List<Person> people;
	private static List<Friend> friends;

	public static void main(String[] args) {

		Scanner consoleInput = new Scanner(System.in);
		people = new ArrayList<Person>();
		friends = new ArrayList<Friend>();

		int userChoice = -1;

		System.out.println("1.  Load 'people' into the network \n" + "2.  Load 'relationships' \n"
				+ "3.  Print out people \n" + "4.  Print out friendships \n" + "14. Log out");

		try {
			while ((userChoice >= 1 && userChoice <= 15) || userChoice == -1) {
				userChoice = consoleInput.nextInt();
				switch (userChoice) {
				case 1:
					new FileHandler().loadPeople(people);
					break;
				case 2:
					new FileHandler().loadFriendships(friends, people);
					loadFriendsForEachPerson();
					break;
				case 3:
					printPeople();
					break;
				case 4:
					printFriendships();
					break;
				case 5: 
					printPersonsFriends();

				case 14:
					break;
				default:
					userChoice = -1;
					System.out.println("Select a valid choice");
				}
			}

		} catch (Exception e) {
			System.out.println("Something went wrong");
		} finally {
			consoleInput.close();
			System.out.println("Goodbye :)");
		}

	}

	private static void loadFriendsForEachPerson() {
		for (Friend friend : friends) {
			if ()
			
		}
		
	}

	private static void printPersonsFriends() {
		// TODO Auto-generated method stub
		
	}

	private static void printPeople() {
		/*try {
			for (Person person : people) {
				System.out.println(person.toString());
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		} */
		String createdFile = "StoredPeople.txt";
		File fileName = new File (createdFile);
		try {
			
			PrintWriter writer = new PrintWriter (fileName);
			writer.println("Number of people: " + people.size() + "\n");
			for (Person p : people) {
				writer.println(p);
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void printFriendships() {
		try {
			for (Friend friend : friends) {
				System.out.println(friend.toString());
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

}
