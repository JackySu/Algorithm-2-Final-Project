import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import java.util.Arrays;
import java.util.List;




class TST<Value> {
    private int n;              // size
    private Node<Value> root;   // root of TST

    private static class Node<Value> {
        private char c;                        // character
        private Node<Value> left, mid, right;  // left, middle, and right sub tries
        private Value val;                     // value associated with string
    }

    /**
     * Initializes an empty string symbol table.
     */
    public TST() {
    }

    /**
     * Returns the number of key-value pairs in this symbol table.
     * @return the number of key-value pairs in this symbol table
     */
    public int size() {
        return n;
    }

    /**
     * Does this symbol table contain the given key?
     * @param key the key
     * @return {@code true} if this symbol table contains {@code key} and
     *     {@code false} otherwise
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public boolean contains(String key) {
        if (key == null) {
            throw new IllegalArgumentException("argument to contains() is null");
        }
        return get(key) != null;
    }

    /**
     * Returns the value associated with the given key.
     * @param key the key
     * @return the value associated with the given key if the key is in the symbol table
     *     and {@code null} if the key is not in the symbol table
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public Value get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("calls get() with null argument");
        }
        if (key.length() == 0) throw new IllegalArgumentException("key must have length >= 1");
        Node<Value> x = get(root, key, 0);
        if (x == null) return null;
        return x.val;
    }

    // return sub trie corresponding to given key
    private Node<Value> get(Node<Value> x, String key, int d) {
        if (x == null) return null;
        if (key.length() == 0) throw new IllegalArgumentException("key must have length >= 1");
        char c = key.charAt(d);
        if      (c < x.c)              return get(x.left,  key, d);
        else if (c > x.c)              return get(x.right, key, d);
        else if (d < key.length() - 1) return get(x.mid,   key, d+1);
        else                           return x;
    }
    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value
     * with the new value if the key is already in the symbol table.
     * If the value is {@code null}, this effectively deletes the key from the symbol table.
     * @param key the key
     * @param val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void put(String key, Value val) {
        if (key == null) {
            throw new IllegalArgumentException("calls put() with null key");
        }
        if (!contains(key)) n++;
        else if(val == null) n--;       // delete existing key
        root = put(root, key, val, 0);
    }

    private Node<Value> put(Node<Value> x, String key, Value val, int d) {
        char c = key.charAt(d);
        if (x == null) {
            x = new Node<Value>();
            x.c = c;
        }
        if      (c < x.c)               x.left  = put(x.left,  key, val, d);
        else if (c > x.c)               x.right = put(x.right, key, val, d);
        else if (d < key.length() - 1)  x.mid   = put(x.mid,   key, val, d+1);
        else                            x.val   = val;
        return x;
    }

    /**
     * Returns the string in the symbol table that is the longest prefix of {@code query},
     * or {@code null}, if no such string.
     * @param query the query string
     * @return the string in the symbol table that is the longest prefix of {@code query},
     *     or {@code null} if no such string
     * @throws IllegalArgumentException if {@code query} is {@code null}
     */
    public String longestPrefixOf(String query) {
        if (query == null) {
            throw new IllegalArgumentException("calls longestPrefixOf() with null argument");
        }
        if (query.length() == 0) return null;
        int length = 0;
        Node<Value> x = root;
        int i = 0;
        while (x != null && i < query.length()) {
            char c = query.charAt(i);
            if      (c < x.c) x = x.left;
            else if (c > x.c) x = x.right;
            else {
                i++;
                if (x.val != null) length = i;
                x = x.mid;
            }
        }
        return query.substring(0, length);
    }

    /**
     * Returns all keys in the symbol table as an {@code Iterable}.
     * To iterate over all of the keys in the symbol table named {@code st},
     * use the foreach notation: {@code for (Key key : st.keys())}.
     * @return all keys in the symbol table as an {@code Iterable}
     */
    public Iterable<String> keys() {
        Queue<String> queue = new LinkedList<>();
        collect(root, new StringBuilder(), queue);
        return queue;
    }

    /**
     * Returns all of the keys in the set that start with {@code prefix}.
     * @param prefix the prefix
     * @return all of the keys in the set that start with {@code prefix},
     *     as an iterable
     * @throws IllegalArgumentException if {@code prefix} is {@code null}
     */
    public Iterable<String> keysWithPrefix(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("calls keysWithPrefix() with null argument");
        }
        Queue<String> queue = new LinkedList<>();
        Node<Value> x = get(root, prefix, 0);
        if (x == null) return queue;
        if (x.val != null) queue.add(prefix);
        collect(x.mid, new StringBuilder(prefix), queue);
        return queue;
    }

    // all keys in subtrie rooted at x with given prefix
    private void collect(Node<Value> x, StringBuilder prefix, Queue<String> queue) {
        if (x == null) return;
        collect(x.left,  prefix, queue);
        if (x.val != null) queue.add(prefix.toString() + x.c);
        collect(x.mid,   prefix.append(x.c), queue);
        prefix.deleteCharAt(prefix.length() - 1);
        collect(x.right, prefix, queue);
    }


    /**
     * Returns all of the keys in the symbol table that match {@code pattern},
     * where the character '.' is interpreted as a wildcard character.
     * @param pattern the pattern
     * @return all of the keys in the symbol table that match {@code pattern},
     *     as an iterable, where . is treated as a wildcard character.
     */
    public Iterable<String> keysThatMatch(String pattern) {
        Queue<String> queue = new LinkedList<>();
        collect(root, new StringBuilder(), 0, pattern, queue);
        return queue;
    }

    private void collect(Node<Value> x, StringBuilder prefix, int i, String pattern, Queue<String> queue) {
        if (x == null) return;
        char c = pattern.charAt(i);
        if (c == '.' || c < x.c) collect(x.left, prefix, i, pattern, queue);
        if (c == '.' || c == x.c) {
            if (i == pattern.length() - 1 && x.val != null) queue.add(prefix.toString() + x.c);
            if (i < pattern.length() - 1) {
                collect(x.mid, prefix.append(x.c), i+1, pattern, queue);
                prefix.deleteCharAt(prefix.length() - 1);
            }
        }
        if (c == '.' || c > x.c) collect(x.right, prefix, i, pattern, queue);
    }


    /**
     * Unit tests the {@code TST} data type.
     *
     * @param args the command-line arguments
     */
    /*
    public static void main(String[] args) {

        // build symbol table from standard input
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; !StdIn.isEmpty(); i++) {
            String key = StdIn.readString();
            st.put(key, i);
        }

        // print results
        if (st.size() < 100) {
            StdOut.println("keys(\"\"):");
            for (String key : st.keys()) {
                StdOut.println(key + " " + st.get(key));
            }
            StdOut.println();
        }

        StdOut.println("longestPrefixOf(\"shellsort\"):");
        StdOut.println(st.longestPrefixOf("shellsort"));
        StdOut.println();

        StdOut.println("longestPrefixOf(\"shell\"):");
        StdOut.println(st.longestPrefixOf("shell"));
        StdOut.println();

        StdOut.println("keysWithPrefix(\"shor\"):");
        for (String s : st.keysWithPrefix("shor"))
            StdOut.println(s);
        StdOut.println();

        StdOut.println("keysThatMatch(\".he.l.\"):");
        for (String s : st.keysThatMatch(".he.l."))
            StdOut.println(s);
    }
     */
}


class DGraph {

    int v;
    int e;
    List<List<Vertex>> edges = new ArrayList<>();
    // List faster than ArrayList when in declaration?

    DGraph(int v, int e) {
        this.v = v;
        this.e = e;

        for (int i = 0; i < v; i++) {
            edges.add(new ArrayList<>());
        }
    }

    static class Vertex implements Comparator<Vertex> {
        int index;
        double weight;

        Vertex(int index, double weight) {
            this.index = index;
            this.weight = weight;
        }

        public Vertex() {
        }

        public int compare(Vertex e1, Vertex e2) {
            return Double.compare(e1.weight, e2.weight);
        }
    }

    void addEdge(int start, int end, double weight) {
        this.edges.get(start).add(new Vertex(end, weight));
    }

    double getWeight(int start, int end) {
        List<Vertex> temp = this.edges.get(start);
        for (Vertex edge: temp) if (edge.index == end) return edge.weight;
        return Double.MAX_VALUE;
    }


    String[] Dijkstra(int v0, int v1) {
        int v = this.v;
        boolean[] addedToPQ = new boolean[v];
        double[] dist = new double[v];  // dist[i] = min distance between v0 and vi
        int[] prev = new int[v];

        // initialize the visited array as all false and get distance from v0 to every other vertex

        Arrays.fill(addedToPQ, false);
        addedToPQ[v0] = true;
        Arrays.fill(dist, Double.MAX_VALUE);
        dist[v0] = 0;
        Arrays.fill(prev, -1);
        prev[v0] = v0;

        /*
        // initialize the visited array as all false and get distance from v0 to every other vertex
        for (int i = 0; i < v; i++) {
            visited[i] = false;
            dist[i] = this.graph.dist[v0][i];
        }
        dist[v0] = 0;
        visited[v0] = true;


         outer loop i has not really meaning we just do (n-1) times of loops (n = #(V)) since we already know which the started vertex v0 is
         the next step is a j loop traversing every vertex to find which unvisited vertex has min distance to v0
         and another j loop to relax
         Q: What is relax?
         A: We find the unvisited vertex u with min distance and see if the path v0 -> u -> j has less distance than v0 -> j directly
            (j is loop variable from 0 to n-1). In other words, take a 'shortcut'.

        for (int i = 1; i < v; i++) {
            int u = v0;
            double min = INF;
            for (int j = 0; j < v; j++)
                if (!visited[j] && dist[j] < min) {  // update if the current vertex is not visited and it has the min distance
                    u = j;
                    min = dist[j];
                }

            visited[u] = true;
            // relax
            for (int j = 0; j < v; j++) {
                if (!visited[j] && this.graph.dist[u][j] < INF) {
                    dist[j] = Double.min(dist[j], this.graph.dist[u][j] + dist[u]);
                }
            }
        }
        // a very very slow version
         */

        /*
        // a prototype with priority queue involved but with mistakes
        PriorityQueue<Vertex> vertexPQ = new PriorityQueue<>(v, new Vertex());
        vertexPQ.add(new Vertex(v0, 0));
        while(!vertexPQ.isEmpty()) {
            Vertex vertexClosestToV0 = vertexPQ.poll();
            visited[vertexClosestToV0.index] = true; // THIS LINE IS WRONG AND SHOULD BE DELETED. A mistake I made.
            for(int i = 0; i<this.edges.get(vertexClosestToV0.index).size(); i++) {
                // relax vertices that are connected to the vertexClosestToV0
                Vertex vertexToRelax = this.edges.get(vertexClosestToV0.index).get(i);
                if(!visited[vertexToRelax.index]) {
                    if(dist[vertexClosestToV0.index] + vertexToRelax.weight < dist[vertexToRelax.index]) {
                        dist[vertexToRelax.index] = dist[vertexClosestToV0.index] + vertexToRelax.weight;
                        prev[vertexToRelax.index] = vertexClosestToV0.index;
                    }
                    addedToPQ[vertexToRelax.index] = true; // THIS IS RIGHT, a better idea to record all the vertices
                    // that are adjacent to the nodes we already visited, these vertices are the next to take into consideration to relax.
                    vertexPQ.add(new Vertex(vertexToRelax.index, dist[vertexToRelax.index]));
                }
            }
        }

        ##
        #  The version 1 code has a typical waste of resources as it traverses all the vertices including those who are not adjacent
        #  to the vertices we already visited, hence visiting these vertices would simply do nothing but waste time let alone relaxing.
        #  version 2 fixes as it focus on maintaining a priority queue which consists of vertices that are to be relaxed.
        ##

        */

        int u;
        PriorityQueue<Vertex> verticesAdjacentPQ = new PriorityQueue<>(v, new Vertex());
        verticesAdjacentPQ.add(new Vertex(v0, 0));
        while (!verticesAdjacentPQ.isEmpty()) {
            Vertex vertexClosestToV0 = verticesAdjacentPQ.poll();
            u = vertexClosestToV0.index;
            List<Vertex> vertexList = this.edges.get(u);
            for (Vertex vertexToRelax : vertexList) {
                double w = vertexToRelax.weight;
                int i = vertexToRelax.index;
                if (!addedToPQ[i] && w < Double.MAX_VALUE) {
                    if (w + dist[u] < dist[i]) {
                        dist[i] = w + dist[u];
                        prev[i] = u;
                    }
                    addedToPQ[i] = true;
                    verticesAdjacentPQ.add(new Vertex(i, dist[i]));
                }
            }

        }

        if (prev[v1] == 0 || dist[v1] == 0) return null;  // no route exists

        List<Integer> RouteArrayList = new ArrayList<>();
        RouteArrayList.add(v1);
        int node = prev[v1];
        while (node != v0) {
            RouteArrayList.add(node);
            node = prev[node];
        }
        RouteArrayList.add(v0);
        Collections.reverse(RouteArrayList);
        String[] res = new String[RouteArrayList.size()];

        double cost;
        for (int i = 0; i < RouteArrayList.size() - 1; i++) {
            cost = getWeight(RouteArrayList.get(i), RouteArrayList.get(i+1));
            res[i] = "from index " + RouteArrayList.get(i) + " to index " + RouteArrayList.get(i+1) + " with cost of " + cost;
        }
        res[RouteArrayList.size() - 1] = "total cost: " + dist[v1];
        return res;
    }
}

public class Algorithm2FinalProject {
    static final String PATH_STOP_TIMES = "src/stop_times.txt";
    static final String PATH_STOPS = "src/stops.txt";
    static final String PATH_TRANSFERS = "src/transfers.txt";

    static int getLineNumber(File file) {
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
                lineNumberReader.skip(Integer.MAX_VALUE);
                int lines = lineNumberReader.getLineNumber() + 1;
                fileReader.close();
                lineNumberReader.close();
                return lines;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    static LinkedList<String> getStopsList(String keyword) {
        TST<String> stopsSearchTries = new TST<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(PATH_STOPS));
            bufferedReader.readLine();
            String stopName = bufferedReader.readLine().split(",")[2].trim(), flagStop;
            while (true) {
                flagStop = stopName.substring(0, 2);
                if (flagStop.equalsIgnoreCase("NB") || flagStop.equalsIgnoreCase("WB") || flagStop.equalsIgnoreCase("SB") || flagStop.equalsIgnoreCase("EB"))
                    stopName = stopName.substring(3) + " " + flagStop;
                stopsSearchTries.put(stopName, stopName);
                try {
                    stopName = bufferedReader.readLine().split(",")[2].trim();
                } catch (NullPointerException e) {break;}
            }
            return (LinkedList<String>)stopsSearchTries.keysWithPrefix(keyword);
        } catch (FileNotFoundException e) {
            System.out.println("File not found exception");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static String[] getShortestRoute(String start, String end) throws IOException {
        int largestStopID = 0;
        boolean startFound = false, endFound = false;
        int startID = 0, endID = 0;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(PATH_STOPS));
            String stopsInfoLine = bufferedReader.readLine();
            while (stopsInfoLine != null) {
                String[] stopsInfoArray = stopsInfoLine.split(",");
                try {
                    largestStopID = Math.max(Integer.parseInt(stopsInfoArray[0]), largestStopID);
                } catch (NumberFormatException ignored) {
                }

                String stopName = stopsInfoArray[2];
                if (start.equalsIgnoreCase(stopName)) {
                    startFound = true;
                    startID = Integer.parseInt(stopsInfoArray[0]);
                }
                if (end.equalsIgnoreCase(stopName)) {
                    endFound = true;
                    endID = Integer.parseInt(stopsInfoArray[0]);
                }

                stopsInfoLine = bufferedReader.readLine();
            }
            if (!startFound && endFound) {
                throw new Exception("start stop not found");
            } else if (startFound && !endFound) {
                throw new Exception("end stop not found");
            } else if (!startFound) {
                throw new Exception("neither start nor end stop found");
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found exception");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File fileStopTimes = new File(PATH_STOP_TIMES);
        File fileTransfers = new File(PATH_TRANSFERS);

        DGraph graph = new DGraph(largestStopID + 1, getLineNumber(fileStopTimes) + getLineNumber(fileTransfers));

        BufferedReader stopTimesReader = new BufferedReader(new FileReader(PATH_STOP_TIMES));
        BufferedReader stopTransfersReader = new BufferedReader(new FileReader(PATH_TRANSFERS));

        String stopTimesLastLine = stopTimesReader.readLine();
        String stopTimesCurrentLine = stopTimesReader.readLine();
        String transfersCurrentLine = stopTransfersReader.readLine();

        String[] stopsLastLines;
        String[] stopsCurrentLines;
        while (stopTimesCurrentLine != null) {
            stopsLastLines = stopTimesLastLine.split(",");
            stopsCurrentLines = stopTimesCurrentLine.split(",");
            if (stopsLastLines[0].equalsIgnoreCase(stopsCurrentLines[0])) {
                graph.addEdge(Integer.parseInt(stopsLastLines[3]), Integer.parseInt(stopsCurrentLines[3]), 1);
            }
            stopTimesLastLine = stopTimesCurrentLine;
            stopTimesCurrentLine = stopTimesReader.readLine();
        }

        String[] transfersCurrentLines;
        while (transfersCurrentLine != null) {
            transfersCurrentLines = transfersCurrentLine.split(",");
            if (transfersCurrentLines[2].equalsIgnoreCase("0")) {
                graph.addEdge(Integer.parseInt(transfersCurrentLines[0]), Integer.parseInt(transfersCurrentLines[1]), 2);
            } else if (transfersCurrentLines[2].equalsIgnoreCase("2")) {
                graph.addEdge(Integer.parseInt(transfersCurrentLines[0]), Integer.parseInt(transfersCurrentLines[1]), Double.parseDouble(transfersCurrentLines[3]) / 100);
            }
            transfersCurrentLine = stopTransfersReader.readLine();
        }

        return graph.Dijkstra(startID, endID);

    }

    public static Boolean isValidTime(String time) {
        int temp;
        try {
            String[] HHMMSS = time.split(":");
            if (HHMMSS.length != 3) return false;
            temp = Integer.parseInt(HHMMSS[0].trim());
            if (temp < 0 || temp > 23) {
                return false;
            }
            temp = Integer.parseInt(HHMMSS[1].trim());
            if (temp < 0 || temp > 59) {
                return false;
            }
            temp = Integer.parseInt(HHMMSS[2].trim());
            return temp >= 0 && temp <= 59;
        } catch (Exception e) {
            return false;
        }
    }

    public static Boolean areTimesEqual(String time1, String time2) {
        try {
            String[] HHMMSS1 = time1.split(":");
            String[] HHMMSS2 = time2.split(":");
            if (HHMMSS1.length != HHMMSS2.length || HHMMSS1.length != 3) return false;
            for (int i = 0; i < 3; i++) {
                if (Integer.parseInt(HHMMSS1[i].trim()) != Integer.parseInt(HHMMSS2[i].trim())) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //  /*
//   * Return empty string array if no stops exist with the given arrival time
//   * Otherwise, return a string array of the details of all stops, sorted by their trip id
//   * Look at project specification for details
//   * */
    public static String[] searchForTripsByArrivalTime(String inputString) {
        List<Pair> validTripIdsAndStops = new LinkedList<>();
        String readString = "";
        String[] splitStrings = new String[9];
        File fileObj = new File(PATH_STOP_TIMES);
        try {
            Scanner reader = new Scanner(fileObj);
            String lastTripId = "";
            Boolean lastTripWasValid = false;
            String stopsInLastTrip = "";
            while (reader.hasNextLine()) {
                readString = reader.nextLine();
                splitStrings = readString.split(",");
                if (splitStrings.length > 2 && isValidTime(splitStrings[1])) {
                    if (splitStrings[0].compareTo(lastTripId) != 0) {
                        if (lastTripWasValid) {
                            validTripIdsAndStops.add(new Pair(lastTripId, stopsInLastTrip));
                        }
                        lastTripWasValid = false;
                        stopsInLastTrip = splitStrings[3];
                        lastTripId = splitStrings[0];
                    } else {
                        stopsInLastTrip += " -> " + splitStrings[3];
                    }
                    if (areTimesEqual(inputString, splitStrings[1])) {
                        lastTripWasValid = true;
                    }
                }
            }
            if (lastTripWasValid) {
                validTripIdsAndStops.add(new Pair(lastTripId, stopsInLastTrip));
            }
            if (validTripIdsAndStops.size() == 0) {
                reader.close();
                return new String[0];
            }
            String[] array_ans = new String[validTripIdsAndStops.size()];
            Collections.sort(validTripIdsAndStops);
            int i = 0;
            for (Pair p : validTripIdsAndStops) {
                array_ans[i++] = "Trip Id: " + p.tripId + " with stops : " + p.stops;
            }
            reader.close();
            return array_ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean mode1(Scanner scanner) {

        String start, end, scan;
        scanner.nextLine();
        while (true) {
            try {
                System.out.print("Enter start bus stop: ");
                scan = scanner.nextLine();
                if (scan.equalsIgnoreCase("exit")) return true;
                if (Pattern.matches(".*[a-zA-Z]+.*", scan)) {
                    start = scan.trim();
                    System.out.print("Enter end bus stop: ");
                    scan = scanner.nextLine();
                    if (scan.equalsIgnoreCase("exit")) return true;
                    if (Pattern.matches(".*[a-zA-Z]+.*", scan)) {
                        end = scan.trim();
                        break;
                    } else throw new Exception("Invalid input for end stop");
                } else throw new Exception("Invalid input for start stop");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Please try again");
            }
        }

        String[] result;
        try {
            result = getShortestRoute(start, end);
            for (String line: result) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("No path route exists between these stops");
        }

        return false;
    }

    public static boolean mode2(Scanner scanner) {
        String input;
        scanner.nextLine();

        while (true) {
            System.out.print("Enter the bus stop name: ");
            input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                return true;
            } else if (Pattern.matches(".*[a-zA-Z]+.*", input)) {
                LinkedList<String> results = getStopsList(input.trim());
                if (results == null || results.size() == 0) System.out.println("No results have been found");
                else for (String result : results) System.out.println(result);
                return false;
            } else {
                System.out.println("Please input valid stop name consisting of letters and numbers (letters are essential) ");
            }
        }
    }

    public static boolean mode3(Scanner scanner) {
        String inputString;

        while (true) {
            System.out.print("Enter the arrival time in such format as HH:MM:SS : ");
            inputString = scanner.next();
            if (inputString.equalsIgnoreCase("exit")) {
                return true;
            } else
                if (!isValidTime(inputString)) {
                    String[] result = searchForTripsByArrivalTime(inputString);
                    if (result.length == 0) {
                        System.out.println("No trips exist with this arrival time");
                    } else {
                        for (String s : result) {
                            System.out.println(s);
                        }
                        break;
                    }
                }
                else {
                    System.out.println("Please input a valid time");
                }

        }
        return false;

    }


    public static void main(String[] args) {
        System.out.println("* Select one of the functions below to run");
        System.out.println("- 1. Finding shortest paths between 2 bus stops");
        System.out.println("- 2. Fuzzy or accurate Search for a bus stop");
        System.out.println("- 3. Searching for all trips with a given arrival time");

        Scanner scan = new Scanner(System.in);
        boolean exit = false;

        while (true) {
            System.out.print("Type 1, 2, 3 or exit: ");
            String inputString = scan.next();
            try {
                int modeChosen = Integer.parseInt(inputString);
                switch (modeChosen) {
                    case 1: exit = mode1(scan); break;
                    case 2: exit = mode2(scan); break;
                    case 3: exit = mode3(scan); break;
                    default: System.out.println("Current mode " + modeChosen + " is not supported yet"); break;
                }
                if (exit) break;
            } catch (NumberFormatException e) {
                if (inputString.equalsIgnoreCase("exit")) {
                    break;
                } else {
                    System.out.println("Input must be a digit of 1, 2, 3 or exit");
                }
            }
        }
        System.out.println("Exit");
    }
}

