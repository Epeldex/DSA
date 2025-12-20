package main;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LongestPathUnweightedGraph {
	private Map<String, Set<String>> graph;
	private List<String> bestPath = new ArrayList<>();
	private static final int duration = 10;
	private static final int threadNum = 16;
	private static final long slice = 750000000; // 200ms

	private synchronized void updateBestPath(List<String> candidate) {
		if (bestPath.isEmpty() || candidate.size() > bestPath.size()) {
			bestPath = new ArrayList<>(candidate);
		}
	}

	public LongestPathUnweightedGraph(Map<String, Set<String>> graph) {
		this.graph = graph;
	}

	public void findLongestPath() {
		try {
			int option = chooseOption();
			String[] ids = choosePeople();
			if (ids == null)
				return;

			String source = ids[0], target = ids[1];
			Map<String, String> dfsParents = new HashMap<>();

			switch (option) {
				case 1:
					dfs(source, target, null, dfsParents);
					reconstructPathWithMap(dfsParents, target);
					break;
				case 2:
					runInParallel(threadNum, () -> {
						long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(duration);

						Set<String> onPath = new HashSet<>();
						List<String> currentPath = new ArrayList<>();

						dfsBacktracking(source, target, onPath, currentPath, deadline);
					});
					printPath(bestPath);
					break;
				case 3:
					runInParallel(threadNum, () -> {
						long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(duration);

						Set<String> onPath = new HashSet<>();
						List<String> currentPath = new ArrayList<>();

						dfsPruning(source, target, onPath, currentPath, deadline);
					});
					printPath(bestPath);
					break;
				case 4:
					runInParallel(threadNum, () -> {
						long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(duration);

						Set<String> onPath = new HashSet<>();
						List<String> currentPath = new ArrayList<>();

						dfsHeuristics(source, target, onPath, currentPath, deadline);
					});
					printPath(bestPath);
					break;
				case 5:
					runInParallel(threadNum, () -> {
						long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(duration);
						long time = System.nanoTime();

						while (time < deadline) {
							time = System.nanoTime();
							long sliceDeadline = time + slice;
							dfsRandomWithRestarts(source, target, new HashSet<>(), new ArrayList<>(),
									deadline, sliceDeadline, new SplittableRandom(time));
						}
					});
					printPath(bestPath);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void printPath(List<String> path) {
		System.out.println("Path size: " + path.size());
		for (int i = 0; i < path.size(); i++) {
			String id = path.get(i);
			System.out.print("Id: " + id);
			if (i < path.size() - 1) {
				System.out.print(" -> ");
			}
		}
		System.out.println("\n");

	}

	private int chooseOption() throws Exception {
		Scanner scanner = new Scanner(System.in);
		System.out.println("This code finds the longest path between two nodes in 5 seconds of execution. "
				+ "In graphs with big clusters, the time complexity explodes and its not realistic to"
				+ "find the actual longest path, however, we can try to give it the best chance it can have"
				+ "in a reasonable amount of time \n");

		int choice = -1;
		System.out.println("You can chose the algorithm you wish to use,"
				+ "all of them run with parallelization in mind (oderer from least accurate to most accurate): \n");
		while (true) {
			try {
				System.out.println("1. DFS (no time budget) \n"
						+ "2. DFS with backtracking \n"
						+ "3. DFS with backtracking and branch pruning\n"
						+ "4. DFS with backtracking, branch pruning and simple heuristics\n"
						+ "5. DFS with backtracking, branch pruning, heuristics, branch shuffling and random restarts\n");
				choice = scanner.nextInt();
				if (choice < 1 || choice > 5) {
					System.out.println("Not a valid choice");
					continue;
				}
				break;
			} catch (InputMismatchException e) {
				System.out.println("Write a valid choice");
				choice = -1;
			}
		}
		return choice;
	}

	private String[] choosePeople() {
		if (graph == null || graph.isEmpty()) {
			System.out.println("No people loaded. Use option 1 first.");
			return null;
		}

		boolean anyFriends = graph.values().stream()
				.anyMatch(p -> p != null && !p.isEmpty());
		if (!anyFriends) {
			System.out.println("No friendships loaded. Use option 2 first.");
			return null;
		}

		Scanner scanner = new Scanner(System.in);

		System.out.print("Enter source person's ID: ");
		String sourceId = scanner.nextLine().trim();

		System.out.print("Enter target person's ID: ");
		String targetId = scanner.nextLine().trim();

		if (!graph.containsKey(sourceId)) {
			System.out.println("Person with ID '" + sourceId + "' does not exist.");
			return null;
		}
		if (!graph.containsKey(targetId)) {
			System.out.println("Person with ID '" + targetId + "' does not exist.");
			return null;
		}

		if (sourceId.equals(targetId)) {
			System.out.println("Source and target are the same person");
			return null;
		}
		String[] a = { sourceId, targetId };
		return a;
	}

	private void reconstructPathWithMap(Map<String, String> dfsParents, String target) {
		List<String> path = new ArrayList<>();
		String node = target;
		while (node != null) {
			path.add(node);
			node = dfsParents.get(node);
		}
		Collections.reverse(path);
		printPath(path);
	}

	private void runInParallel(int threads, Runnable task) throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(threads);
		ExecutorService pool = Executors.newFixedThreadPool(threads);

		for (int i = 0; i < threads; i++) {
			pool.execute(() -> {
				try {
					task.run();
				} catch (Throwable t) {
					t.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		pool.shutdownNow();
	}

	private boolean dfs(String current, String target, Set<String> visited, Map<String, String> parent)
			throws Exception {
		if (visited == null)
			visited = new HashSet<>();
		if (parent.isEmpty())
			parent.put(current, null);

		visited.add(current);

		if (current.equals(target))
			return true;

		for (String id : graph.getOrDefault(current, Collections.emptySet())) {
			if (!visited.contains(id)) {
				parent.put(id, current);
				if (dfs(id, target, visited, parent))
					return true;
			}
		}
		return false;
	}

	private void dfsBacktracking(String current, String target, Set<String> onPath,
			List<String> currentPath,
			long deadline) {
		if (System.nanoTime() > deadline)
			return;

		try {
			currentPath.add(current);
			onPath.add(current);

			if (current.equals(target)) {
				if (currentPath.size() > bestPath.size())
					updateBestPath(currentPath);
				return;
			}
			for (String id : graph.getOrDefault(current, Collections.emptySet())) {
				if (!onPath.contains(id)) {
					dfsBacktracking(id, target, onPath, currentPath, deadline);
				}
			}
		} finally {
			currentPath.remove(currentPath.size() - 1);
			onPath.remove(current);
		}

	}

	private void dfsPruning(String current, String target, Set<String> onPath,
			List<String> currentPath,
			long deadline) {
		if (System.nanoTime() > deadline)
			return;

		try {
			currentPath.add(current);
			onPath.add(current);

			if (current.equals(target)) {
				if (currentPath.size() > bestPath.size())
					updateBestPath(currentPath);
				return;
			}
			for (String id : graph.getOrDefault(current, Collections.emptySet())) {
				if (onPath.contains(id))
					continue;
				// PATH EXISTS
				if (!pathExists(id, target, new HashSet<>(onPath)))
					continue;
				// POTENTIAL PATH SIZE
				int upperBound = currentPath.size() + connectedComponets(id, new HashSet<>(onPath));
				if (upperBound <= bestPath.size())
					continue;
				dfsPruning(id, target, onPath, currentPath, deadline);
			}
		} finally {
			currentPath.remove(currentPath.size() - 1);
			onPath.remove(current);
		}
	}

	/* BFS */
	private int connectedComponets(String current, Set<String> visited) {
		Queue<String> remaining = new ArrayDeque<>();
		Set<String> seen = new HashSet<>();

		seen.add(current);
		remaining.add(current);

		while (!remaining.isEmpty()) {
			current = remaining.remove();
			for (String id : graph.getOrDefault(current, Collections.emptySet())) {
				if (visited.contains(id) || seen.contains(id))
					continue;
				seen.add(id);
				remaining.add(id);
			}
		}
		return seen.size();
	}

	private boolean pathExists(String current, String target, Set<String> visited) {
		if (current.equals(target))
			return true;
		visited.add(current);

		for (String id : graph.getOrDefault(current, Collections.emptySet())) {
			if (!visited.contains(id)) {
				if (pathExists(id, target, visited))
					return true;
			}
		}
		return false;
	}

	private void dfsHeuristics(String current, String target, Set<String> onPath,
			List<String> currentPath,
			long deadline) {
		if (System.nanoTime() > deadline)
			return;

		try {
			currentPath.add(current);
			onPath.add(current);

			if (current.equals(target)) {
				if (currentPath.size() > bestPath.size())
					updateBestPath(currentPath);
				return;
			}
			for (String id : orderByDegree(graph.getOrDefault(current, Collections.emptySet()), onPath,
					new SplittableRandom())) {
				// PATH EXISTS
				if (!pathExists(id, target, new HashSet<>(onPath)))
					continue;
				// POTENTIAL PATH SIZE
				Set<String> blocked = new HashSet<>(onPath);
				blocked.remove(id);
				int upperBound = currentPath.size() + connectedComponets(id, blocked);
				if (upperBound <= bestPath.size())
					continue;
				dfsHeuristics(id, target, onPath, currentPath, deadline);
			}
		} finally {
			currentPath.remove(currentPath.size() - 1);
			onPath.remove(current);
		}
	}

	private Iterable<String> orderByDegree(Set<String> nodes, Set<String> onPath, SplittableRandom rnd) {
		List<String> nodeList = new ArrayList<>();
		for (String node : nodes) {
			if (!onPath.contains(node))
				nodeList.add(node);
		}
		Collections.shuffle(nodeList, rnd);
		Integer[] degreeArray = new Integer[nodeList.size()];
		for (int i = 0; i < nodeList.size(); i++) {
			String node = nodeList.get(i);
			int count = 0;
			for (String n : graph.getOrDefault(node, Collections.emptySet())) {
				if (!onPath.contains(n))
					count++;
			}
			degreeArray[i] = count;
		}
		for (int i = 0; i < degreeArray.length; i++) {
			for (int j = i + 1; j < degreeArray.length; j++) {
				if (degreeArray[i] < degreeArray[j]) {
					int aux = degreeArray[i];
					String auxN = nodeList.get(i);
					degreeArray[i] = degreeArray[j];
					nodeList.set(i, nodeList.get(j));
					degreeArray[j] = aux;
					nodeList.set(j, auxN);
				}
			}
		}
		return nodeList;
	}

	private void dfsRandomWithRestarts(String current, String target, Set<String> onPath,
			List<String> currentPath, long deadline, long sliceDeadline, SplittableRandom rnd) {
		long time = System.nanoTime();
		if (time > deadline || time > sliceDeadline)
			return;
		try {
			currentPath.add(current);
			onPath.add(current);

			if (current.equals(target)) {
				if (currentPath.size() > bestPath.size())
					updateBestPath(currentPath);
				return;
			}
			for (String id : orderByDegree(graph.getOrDefault(current, Collections.emptySet()), onPath, rnd)) {
				// PATH EXISTS
				if (!pathExists(id, target, new HashSet<>(onPath)))
					continue;
				// POTENTIAL PATH SIZE
				Set<String> blocked = new HashSet<>(onPath);
				blocked.remove(id);
				int upperBound = currentPath.size() + connectedComponets(id, blocked);
				if (upperBound <= bestPath.size())
					continue;
				dfsRandomWithRestarts(id, target, onPath, currentPath, deadline, sliceDeadline, rnd);
			}
		} finally {
			currentPath.remove(currentPath.size() - 1);
			onPath.remove(current);
		}
	}
}