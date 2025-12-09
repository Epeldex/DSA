package main;

import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class FileHandler {

	private static List<File> selectedPeopleFiles = new ArrayList<File>();
	private static List<File> selectedFriendFiles = new ArrayList<File>();

	public FileHandler() {
	}

	public void loadPeople(Map<String, Person> people) {

		if (selectedPeopleFiles.isEmpty()) {
			selectedPeopleFiles = selectFiles("people");
		}

		for (File peopleFile : selectedPeopleFiles) {
			try (Scanner input = new Scanner(peopleFile)) {
				if (input.hasNextLine()) {
					input.nextLine();
				}
				while (input.hasNextLine()) {
					String line = input.nextLine().trim();
					if (line.isEmpty()) {
						continue;
					}
					Person person = parsePerson(line);
					people.putIfAbsent(person.getIdperson(), person);
				}

			} catch (Exception e) {
				System.err.println("Error reading file " + peopleFile.getName() + ": " + e.getMessage());
			}
		}
	}

	private Person parsePerson(String rawPerson) {
		String[] parts = rawPerson.split(",", -1);

		if (parts.length < 11) {
			throw new IllegalArgumentException("Invalid person line (expected 11 fields): " + rawPerson);
		}

		Person person = new Person(parts[0].trim());
		person.setName(parts[1].trim());
		person.setLastname(parts[2].trim());
		person.setBirthdate(parts[3].trim());
		person.setGender(parts[4].trim());
		person.setBirthplace(parts[5].trim());
		person.setHome(parts[6].trim());
		person.setStudiedAt(splitListField(parts[7]));
		person.setWorkplaces(splitListField(parts[8]));
		person.setFilms(splitListField(parts[9]));
		person.setGroupcode(parts[10].trim());

		return person;
	}

	private List<String> splitListField(String field) {
		field = field.trim();
		if (field.isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.stream(field.split(";")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
	}

	public void loadFriendships(Map<String, Person> people) {

		if (selectedPeopleFiles.isEmpty()) {
			System.out.println("You cannot load friendships without people. Loading people first...");
			loadPeople(people);
		}

		if (selectedFriendFiles.isEmpty()) {
			selectedFriendFiles = selectFiles("friends");
		}

		// adjacency map: id -> set of friend ids
		Map<String, Set<String>> adjacency = new HashMap<>();

		// ---- Parse all friendship files ----
		for (File friendFile : selectedFriendFiles) {
			try (Scanner input = new Scanner(friendFile)) {

				if (input.hasNext()) {
					input.nextLine(); // skip header
				}

				while (input.hasNextLine()) {
					String line = input.nextLine().trim();
					if (line.isEmpty())
						continue;

					String[] parts = line.split(",");
					if (parts.length < 2)
						continue;

					String a = parts[0].trim();
					String b = parts[1].trim();

					// only accept friendships between people that actually exist
					if (!people.containsKey(a) || !people.containsKey(b)) {
						// Optional debug:
						// System.out.println("Skipping unknown IDs: " + a + " or " + b);
						continue;
					}

					// Add b to a's set
					adjacency.computeIfAbsent(a, k -> new HashSet<>()).add(b);

					// Add a to b's set (undirected friendship)
					adjacency.computeIfAbsent(b, k -> new HashSet<>()).add(a);
				}

			} catch (Exception e) {
				System.err.println("Error while reading friendships: " + e.getMessage());
			}
		}

		// ---- Apply adjacency to Person objects ----
		for (Map.Entry<String, Set<String>> entry : adjacency.entrySet()) {
			String id = entry.getKey();
			Set<String> friendsSet = entry.getValue();

			Person p = people.get(id);
			if (p != null) {
				// Person.friends is already a Set<String>, so this is safe
				p.getFriends().addAll(friendsSet);
			}
		}

		System.out.println("Friendships loaded successfully.");
	}

	public List<File> selectFiles(String restriction) {
		List<File> fileList = new ArrayList<File>();

		try {
			String currentDir = System.getProperty("user.dir");
			JFrame owner = new JFrame();
			owner.setUndecorated(true);
			owner.setType(Window.Type.UTILITY);
			owner.setAlwaysOnTop(true);
			owner.setLocationRelativeTo(null);
			owner.setSize(1, 1);
			owner.setVisible(true);

			try {
				JFileChooser chooser = new JFileChooser(currentDir);
				chooser.setMultiSelectionEnabled(true);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {
						if (f.isDirectory())
							return true;
						String name = f.getName().toLowerCase();
						return name.startsWith(restriction) && name.endsWith(".txt");
					}

					@Override
					public String getDescription() {
						return restriction + " text files (" + restriction + "*.txt)";
					}
				});
				int result = chooser.showOpenDialog(owner); // modal to our on-top owner
				if (result == JFileChooser.APPROVE_OPTION) {
					fileList = Arrays.asList(chooser.getSelectedFiles());
				}
			} finally {
				owner.dispose();
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return fileList;
	}

	private boolean personExists(Map<String, Person> people, Person person) {
		return people.containsValue(person);
	}

	public List<String> loadResidentialIds() {
		List<String> ids = new ArrayList<>();

		File residentialFile = new File("residential.txt");

		if (!residentialFile.exists()) {
			System.out.println("File 'residential.txt' not found in the current directory.");
			return ids;
		}

		try (Scanner input = new Scanner(residentialFile)) {
			while (input.hasNextLine()) {
				String line = input.nextLine().trim();
				if (!line.isEmpty()) {
					ids.add(line);
				}
			}
		} catch (Exception e) {
			System.err.println("Error reading 'residential.txt': " + e.toString());
		}

		return ids;
	}
}
