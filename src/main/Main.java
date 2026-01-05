package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Main {

	private static Map<String, Person> people;

	public static void main(String[] args) {

		Scanner consoleInput = new Scanner(System.in);
		people = new HashMap<String, Person>();

		int userChoice = -1;

		try {
			while (userChoice != 14) {
				System.out.println("1.  Load 'people' into the network \n" + "2.  Load 'relationships' \n"
						+ "3.  Print out people \n" + "4.  Print out friendships \n"
						+ "5.  Print friends of a certain person \n"
						+ "6.  Retrieve all the people that live in a certain city \n"
						+ "7.  Print out all the people born between 2 dates \n"
						+ "8.  People whose birthplace matches hometowns in 'residential.txt' \n"
						+ "9.  Create groups with the same favorite movies' \n"
						+ "10. Find shortest chain between two people (BFS) \n"
						+ "11. Find a DFS chain between two people \n"
						+ "12. Find friend cliques (crews) with 5 or more people \n"
						+ "13. Find longest chain between two people \n"
						+ "14. Log out");

				userChoice = consoleInput.nextInt();
				switch (userChoice) {
					case 1:
						new FileHandler().loadPeople(people);
						break;
					case 2:
						new FileHandler().loadFriendships(people);
						break;
					case 3:
						printPeople();
						break;
					case 4:
						printFriendships();
						break;
					case 5:
						printPersonsFriends();
						break;
					case 6:
						printPeopleCity();
						break;
					case 7:
						printPeopleBetweenDates();
						break;
					case 8:
						printPeopleMatchingResidentialHometowns();
						break;
					case 9:
						groupByProfile();
						break;
					case 10:
						findShortestChainBetweenTwoPeople();
						break;
					case 11:
						findDFSChainBetweenTwoPeople();
						break;
					case 12:
						findFriendCliques();
						break;
					case 13:
						new LongestPathUnweightedGraph(createFriendshipGraph()).findLongestPath();
						break;
					case 14:
						break;
					default:
						userChoice = -1;
						System.out.println("Select a valid choice");
				}
			}

		} catch (Exception e) {
			System.out.println("Something went wrong");
			e.printStackTrace();
		} finally {
			consoleInput.close();
			System.out.println("Goodbye :)");
		}

	}

	private static Map<String, Set<String>> createFriendshipGraph() {
		Map<String, Set<String>> g = new HashMap<>();
		for (Entry<String, Person> entry : people.entrySet()) {
			String id = entry.getKey();
			Set<String> friends = entry.getValue().getFriends();
			g.put(id, (friends == null) ? new HashSet<>() : new HashSet<>(friends));
		}
		return g;
	}

	private static void findDFSChainBetweenTwoPeople() {
		if (people == null || people.isEmpty()) {
			System.out.println("No people loaded. Use option 1 first.");
			return;
		}

		boolean anyFriends = people.values().stream()
				.anyMatch(p -> p.getFriends() != null && !p.getFriends().isEmpty());
		if (!anyFriends) {
			System.out.println("No friendships loaded. Use option 2 first.");
			return;
		}

		Scanner scanner = new Scanner(System.in);

		System.out.print("Enter source person's ID: ");
		String sourceId = scanner.nextLine().trim();

		System.out.print("Enter target person's ID: ");
		String targetId = scanner.nextLine().trim();

		if (!people.containsKey(sourceId)) {
			System.out.println("Person with ID '" + sourceId + "' does not exist.");
			return;
		}
		if (!people.containsKey(targetId)) {
			System.out.println("Person with ID '" + targetId + "' does not exist.");
			return;
		}

		if (sourceId.equals(targetId)) {
			Person p = people.get(sourceId);
			System.out.println("Source and target are the same person: " + p.getName() + " (" + p.getIdperson() + ")");
			return;
		}
		Map<String, Set<String>> graph = createFriendshipGraph();

		// --- DFS setup ---
		Set<String> visited = new HashSet<>();
		Map<String, String> prev = new HashMap<>();

		boolean found = dfsPath(sourceId, targetId, visited, prev, graph);

		if (!found) {
			System.out.println("No DFS chain found between " + sourceId + " and " + targetId + ".");
			return;
		}

		// --- Reconstruct path from target back to source using prev ---
		List<String> path = new ArrayList<>();
		String current = targetId;
		while (current != null) {
			path.add(current);
			if (current.equals(sourceId)) {
				break;
			}
			current = prev.get(current);
		}

		if (!path.get(path.size() - 1).equals(sourceId)) {
			System.out.println("No DFS chain found between " + sourceId + " and " + targetId + ".");
			return;
		}

		Collections.reverse(path);

		System.out.println("\nDFS chain from " + sourceId + " to " + targetId + ":");
		System.out.println("(Length: " + (path.size() - 1) + " edges)\n");

		for (int i = 0; i < path.size(); i++) {
			String id = path.get(i);
			Person p = people.get(id);
			System.out.print(p.getName() + " " + p.getLastname() + " (" + id + ")");
			if (i < path.size() - 1) {
				System.out.print("  ->  ");
			}
		}
		System.out.println("\n");
	}

	private static boolean dfsPath(String current, String target, Set<String> visited, Map<String, String> parent,
			Map<String, Set<String>> graph) {
		visited.add(current);
		if (current.equals(target))
			return true;
		for (String node : graph.get(current)) {
			if (!visited.contains(node)) {
				parent.put(node, current);
				if (dfsPath(node, target, visited, parent, graph))
					return true;
			}
		}
		return false;
	}

	private static void findShortestChainBetweenTwoPeople() {
		// Basic sanity checks
		if (people == null || people.isEmpty()) {
			System.out.println("No people loaded. Use option 1 (and 2 for friendships) first.");
			return;
		}

		boolean anyFriends = people.values().stream()
				.anyMatch(p -> p.getFriends() != null && !p.getFriends().isEmpty());
		if (!anyFriends) {
			System.out.println("No friendships loaded. Use option 2 first.");
			return;
		}

		Scanner scanner = new Scanner(System.in);

		System.out.print("Enter source person's ID: ");
		String sourceId = scanner.nextLine().trim();

		System.out.print("Enter target person's ID: ");
		String targetId = scanner.nextLine().trim();

		if (!people.containsKey(sourceId)) {
			System.out.println("Person with ID '" + sourceId + "' does not exist.");
			return;
		}
		if (!people.containsKey(targetId)) {
			System.out.println("Person with ID '" + targetId + "' does not exist.");
			return;
		}

		if (sourceId.equals(targetId)) {
			Person p = people.get(sourceId);
			System.out.println("Source and target are the same person: " + p.getName() + " (" + p.getIdperson() + ")");
			return;
		}
		Map<String, Set<String>> graph = createFriendshipGraph();

		Set<String> visited = new HashSet<>();
		Queue<String> remaining = new ArrayDeque<>();
		Map<String, String> parent = new HashMap<>();
		boolean found = false;

		visited.add(sourceId);
		remaining.add(sourceId);
		parent.put(sourceId, null); // to stop iterating while reconstructing the path

		while (!remaining.isEmpty() && !found) {
			String node = remaining.remove();
			if (node.equals(targetId))
				found = true;
			for (String friendId : graph.get(node)) {
				if (!visited.contains(friendId)) {
					visited.add(friendId);
					remaining.add(friendId);
					parent.put(friendId, node);
				}
			}
		}

		List<String> path = new ArrayList<>();
		String node = targetId;
		while (node != null) {
			path.add(node);
			node = parent.get(node);
		}
		Collections.reverse(path);
		// --- Print result ---
		System.out.println("\nShortest chain from " + sourceId + " to " + targetId + ":");
		System.out.println("(Degrees of separation: " + (path.size() - 1) + ")\n");

		for (int i = 0; i < path.size(); i++) {
			String id = path.get(i);
			Person p = people.get(id);
			System.out.print(p.getName() + " " + p.getLastname() + " (" + id + ")");
			if (i < path.size() - 1) {
				System.out.print("  ->  ");
			}
		}
		System.out.println("\n");
	}

	private static void printPeopleMatchingResidentialHometowns() {
		FileHandler fh = new FileHandler();
		List<String> residentialIds = fh.loadResidentialIds();

		if (residentialIds == null || residentialIds.isEmpty()) {
			System.out.println("No identifiers found in 'residential.txt' or file could not be read.");
			return;
		}

		HashSet<String> hometowns = new HashSet<>();

		for (String id : residentialIds) {
			Person p = people.get(id);

			if (p == null) {
				System.out.println("Warning: person with id '" + id + "' not found in the network.");
				continue;
			}

			String home = p.getHome();
			if (home != null) {
				home = home.trim();
			}

			if (home != null && !home.isEmpty()) {
				hometowns.add(home);
			}
		}

		if (hometowns.isEmpty()) {
			System.out.println("No valid hometowns found for the identifiers in 'residential.txt'.");
			return;
		}

		List<Person> result = new ArrayList<>();

		for (Person p : people.values()) {
			String birthplace = p.getBirthplace();
			if (birthplace != null) {
				birthplace = birthplace.trim();
			}

			if (birthplace != null && !birthplace.isEmpty() && hometowns.contains(birthplace)) {
				result.add(p);
			}
		}

		if (result.isEmpty()) {
			System.out.println("No people found whose birthplace matches the hometown(s) from 'residential.txt'.");
			return;
		}

		System.out.println("\nPeople whose birthplace matches hometown(s) in 'residential.txt':\n");

		System.out.printf("%-20s %-20s %-15s %-30s\n", "Name", "Surname", "Birthplace", "Studied at");
		System.out.println("-------------------------------------------------------------------------------");

		for (Person p : result) {
			String studiedAtText = "";
			if (p.getStudiedAt() != null && !p.getStudiedAt().isEmpty()) {
				studiedAtText = String.join(", ", p.getStudiedAt());
			}

			System.out.printf("%-20s %-20s %-15s %-30s\n", p.getName(), p.getLastname(), p.getBirthplace(),
					studiedAtText);
		}
	}

	private static void printPeopleBetweenDates() {
		Scanner scanner = new Scanner(System.in);

		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
		sdf.setLenient(false);

		Date d1 = null, d2 = null;

		while (true) {
			System.out.print("Enter first date (dd-M-yyyy): ");
			String input = scanner.nextLine();
			try {
				d1 = sdf.parse(input);
				break;
			} catch (Exception e) {
				System.out.println("Invalid format. Try again.");
			}
		}

		while (true) {
			System.out.print("Enter second date (dd-M-yyyy): ");
			String input = scanner.nextLine();
			try {
				d2 = sdf.parse(input);
				break;
			} catch (Exception e) {
				System.out.println("Invalid format. Try again.");
			}
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		int year1 = cal.get(Calendar.YEAR);

		cal.setTime(d2);
		int year2 = cal.get(Calendar.YEAR);

		int start = Math.min(year1, year2);
		int end = Math.max(year1, year2);

		List<Person> result = new ArrayList<>();

		for (Person p : people.values()) {

			String bd = p.getBirthdate();

			if (bd == null || bd.length() < 5)
				continue;

			int birthYear;
			try {
				int lastDash = bd.lastIndexOf('-');
				birthYear = Integer.parseInt(bd.substring(lastDash + 1));
			} catch (Exception e) {
				continue;
			}

			if (birthYear >= start && birthYear <= end) {
				result.add(p);
			}
		}

		Collections.sort(result);

		System.out.println("\nPeople born between years " + start + " and " + end + ":\n");

		if (result.isEmpty()) {
			System.out.println("No people found.");
			return;
		}

		System.out.printf("%-30s %-15s %-15s\n", "Full Name", "Birthdate", "Birthplace");
		System.out.println("---------------------------------------------------------------------");

		for (Person p : result) {
			System.out.printf("%-30s %-15s %-15s\n", p.getName() + " " + p.getLastname(), p.getBirthdate(),
					p.getBirthplace());
		}
	}

	// private static void loadFriendsForEachPerson(List<Friend> friends) {
	// for (Friend friend : friends) {
	// Set<String> friendsOfPerson = new
	// HashSet<>(Arrays.asList(friend.getFriends()));
	// Person person = people.get(friend.getIdperson());
	// person.setFriends(friendsOfPerson);
	// people.put(person.getIdperson(), person);
	// }
	//
	// }

	private static void printPersonsFriends() {
		try {
			Scanner scanner = new Scanner(System.in);
			System.out.println("Introduce the surname of the person to find: ");
			String surname = scanner.nextLine();

			List<Person> matches = new ArrayList<>();

			for (Person p : people.values()) {
				if (p.getLastname().equalsIgnoreCase(surname)) {
					matches.add(p);
				}
			}
			if (matches.isEmpty()) {
				System.out.println("No people found with lastname: " + surname);
				return;
			}
			for (Person person : matches) {
				System.out.println("Person: ID = " + person.getIdperson() + ", Lastname = " + person.getLastname());
				Set<String> friends = person.getFriends();

				if (friends == null || friends.isEmpty()) {
					System.out.println("    No friends found.");
					continue;
				}

				System.out.println("    Friends:");
				for (String friend : friends) {
					System.out.println("        ID = " + friend + ", Name = " + people.get(friend).getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void printPeople() {
		String createdFile = "StoredPeople.txt";
		File fileName = new File(createdFile);
		try {

			PrintWriter writer = new PrintWriter(fileName);
			writer.println("Number of people: " + people.keySet().size() + "\n");
			for (String p : people.keySet()) {
				writer.println(people.get(p).toString());
			}

			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void printFriendships() {
		try {
			for (Person a : people.values()) {
				System.out.println(a.getIdperson() + ": " + a.getFriends().toString());
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	private static void printPeopleCity() {
		Scanner consoleLine = new Scanner(System.in);
		System.out.println("Which city do you want to show people from?");
		String answer = consoleLine.nextLine();

		if (answer.isEmpty()) {
			System.out.println("No country entered.");
		}

		try {
			boolean found = false;

			for (Person p : people.values()) {
				if (answer.equalsIgnoreCase(p.getBirthplace())) {
					System.out.println(p.getIdperson() + " " + p.getLastname());
					found = true;
				}
			}

			if (!found) {
				System.out.println("No people found from " + answer);
			}

		} catch (Exception e) {
			System.out.println("An error has ocurred while searching for people");
		}
	}

	public static List<List<Person>> groupByProfile() {

		Map<String, List<Person>> groups = new HashMap<>();

		for (Person p : people.values()) {

			Set<String> sortedFavs = new TreeSet<>(p.getFilms());

			String key = String.join(",", sortedFavs);

			groups.computeIfAbsent(key, k -> new ArrayList<>()).add(p);
		}

		List<List<Person>> result = new ArrayList<>(groups.values());

		System.out.println("=== GROUPS OF USERS WITH SAME PROFILE ===");
		int groupNumber = 1;

		for (List<Person> group : result) {
			System.out.println("Group " + groupNumber++ + ":");

			System.out.println("Films in common: " + String.join(", ", group.get(0).getFilms()));
			for (Person p : group) {
				System.out.println("   - " + p.getName());
			}
			System.out.println();
		}

		return result;
	}

	private static void findFriendCliques() {
		if (people == null || people.isEmpty()) {
			System.out.println("No people loaded. Use option 1 first.");
			return;
		}

		boolean anyFriends = people.values().stream()
				.anyMatch(p -> p.getFriends() != null && !p.getFriends().isEmpty());
		if (!anyFriends) {
			System.out.println("No friendships loaded. Use option 2 first.");
			return;
		}

		// Build an adjacency map: id -> set of friend ids
		Map<String, Set<String>> adjacency = new HashMap<>();
		for (Map.Entry<String, Person> entry : people.entrySet()) {
			String id = entry.getKey();
			Person p = entry.getValue();

			Set<String> neighbours = (p.getFriends() == null) ? Collections.emptySet() : new HashSet<>(p.getFriends());
			adjacency.put(id, neighbours);
		}

		// Bron–Kerbosch: R = current clique, P = candidates, X = already processed
		Set<String> R = new HashSet<>();
		Set<String> P = new HashSet<>(adjacency.keySet()); // all vertices
		Set<String> X = new HashSet<>();

		List<Set<String>> cliques = new ArrayList<>();

		bronKerbosch(R, P, X, adjacency, cliques);

		// Filter: only cliques with size >= 5
		List<Set<String>> bigCliques = cliques.stream().filter(c -> c.size() >= 5).collect(Collectors.toList());

		if (bigCliques.isEmpty()) {
			System.out.println("No friend cliques (crews) with 5 or more people found.");
			return;
		}

		System.out.println("\nFriend cliques (crews) with 5 or more people:");

		int index = 1;
		for (Set<String> clique : bigCliques) {
			System.out.println("\nCrew #" + index + " (size " + clique.size() + "):");
			for (String id : clique) {
				Person p = people.get(id);
				if (p != null) {
					System.out.println(" - " + p.getName() + " " + p.getLastname() + " (" + id + ")");
				} else {
					System.out.println(" - (" + id + ")");
				}
			}
			index++;
		}

		System.out.println();
	}

	private static void bronKerbosch(Set<String> R, Set<String> P, Set<String> X, Map<String, Set<String>> adjacency,
			List<Set<String>> cliques) {

		// If no more candidates and no more excluded, R is a maximal clique
		if (P.isEmpty() && X.isEmpty()) {
			if (R.size() >= 2) { // we collect all maximal cliques, size filter later
				cliques.add(new HashSet<>(R));
			}
			return;
		}

		// Copy because we will modify P while iterating
		Set<String> Pcopy = new HashSet<>(P);

		for (String v : Pcopy) {
			Set<String> newR = new HashSet<>(R);
			newR.add(v);

			Set<String> neighbors = adjacency.getOrDefault(v, Collections.emptySet());

			Set<String> newP = new HashSet<>();
			for (String w : P) {
				if (neighbors.contains(w)) {
					newP.add(w);
				}
			}
			Set<String> newX = new HashSet<>();
			for (String w : X) {
				if (neighbors.contains(w)) {
					newX.add(w);
				}
			}
			bronKerbosch(newR, newP, newX, adjacency, cliques);
			P.remove(v);
			X.add(v);
		}
	}

}
