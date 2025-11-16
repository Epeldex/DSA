package main;

import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class FileHandler {

	private static List<File> selectedPeopleFiles = new ArrayList<File>();
	private static List<File> selectedFriendFiles = new ArrayList<File>();

	public FileHandler() {
	}

	public void loadPeople(List<Person> people) {
		Scanner input;
		String rawPerson;

		if (selectedPeopleFiles.isEmpty()) {
			selectedPeopleFiles = selectFiles("people");
		}
		try {
			for (File peopleFile : selectedPeopleFiles) {
				input = new Scanner(peopleFile);
				rawPerson = input.nextLine();
				while (input.hasNext()) {
					rawPerson = input.nextLine();
					// process the string
					String[] parts = rawPerson.split(",");
					Person person = new Person(parts[0]);
					person.setName(parts[1]);
					person.setLastname(parts[2]);
					person.setBirthdate(parts[3]);
					person.setGender(parts[4]);
					person.setBirthplace(parts[5]);
					person.setHome(parts[6]);
					person.setGroupcode(parts[10]);
					String[] studiedAt = parts[7].split(";");
					String[] workplaces = parts[8].split(";");
					String[] films = parts[9].split(";");
					person.setStudiedAt(Arrays.asList(studiedAt));
					person.setWorkplaces(Arrays.asList(workplaces));
					person.setFilms(Arrays.asList(films));

					if (!personExists(people, person)) {
						people.add(person);
					}

				}
			}

		} catch (Exception e) {
			System.err.println(e.toString());

		} finally {

		}

	}

	public void loadFriendships(List<Friend> friends, List<Person> people) {
		List<String> inputList = new ArrayList<>();

		if (selectedPeopleFiles.isEmpty()) {
			System.out.println("You cant select friendships when there are no people loaded, select people first");
			loadPeople(people);
		}

		if (selectedFriendFiles.isEmpty()) {
			selectedFriendFiles = selectFiles("friends");
		}
		for (File friendFile : selectedFriendFiles) {
			try (Scanner input = new Scanner(friendFile)) {

				if (input.hasNext())
					input.nextLine(); // skip header
				while (input.hasNext()) {
					String line = input.nextLine().trim();
					if (!line.isEmpty())
						inputList.add(line);
				}

				// Collect ALL unique ids from both columns
				List<String> allIds = new ArrayList<>();
				for (String line : inputList) {
					String[] parts = line.split(",");
					if (parts.length < 2)
						continue;
					String a = parts[0].trim();
					String b = parts[1].trim();
					for (Person p : people) {
						if (!allIds.contains(a) && p.getIdperson().equals(a)) {
							{
								allIds.add(a);
							}
							if (!allIds.contains(b) && p.getIdperson().equals(b)) {
								allIds.add(b);
							}
						}
					}
				}

				// For each id, gather friends from either side of every edge
				for (String id : allIds) {
					List<String> friendsOfId = new ArrayList<>();
					for (String line : inputList) {
						String[] parts = line.split(",");
						if (parts.length < 2)
							continue;
						String a = parts[0].trim();
						String b = parts[1].trim();
						for (Person p : people) {
							if (id.equals(a) && !friendsOfId.contains(b) && p.getIdperson().equals(b)) {
								friendsOfId.add(b);
							} else if (id.equals(b) && !friendsOfId.contains(a) && p.getIdperson().equals(a)) {
								friendsOfId.add(a);
							}
						}
					}
					friends.add(new Friend(id, friendsOfId.toArray(new String[0])));
				}

			} catch (Exception e) {
				System.err.println(e.toString());
			}

		}

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

	private boolean personExists(List<Person> people, Person person) {
		for (Person p: people) {
			if (person.getIdperson().equalsIgnoreCase(p.getIdperson()))
				return true;
		}
		return false;
	}
	/*
	 * private int[] filesSelected() throws Exception { int[] choices = new
	 * int[remainingFiles.size()]; Scanner scanner = new Scanner(System.in); int
	 * choice = -1;
	 * 
	 * while (choice == -1 || choice <= remainingFiles.size()) { int i = 0; if
	 * (remainingFiles.size() > 0) { System.out.println("People files found:"); int
	 * j = 0; for (File file : remainingFiles) { System.out.println(j + 1 + ") " +
	 * file.getName()); j++; } try { System.out.println("Select: "); choice =
	 * scanner.nextInt(); } catch (InputMismatchException e) {
	 * System.out.println("Write a valid character"); choice = -1; } for (int c :
	 * choices) { if (c == choice) { System.out.println("File already chosen");
	 * choice = -1; } } if (choice != -1) { choices[i] = choice; } i++; } else {
	 * System.out.println("No people files found"); } }
	 * 
	 * return choices; }
	 */

}
