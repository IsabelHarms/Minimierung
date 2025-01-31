import java.io.*;
import java.util.*;

class Graph {
    static final int NODE_RADIUS = 30;
    State startState;
    Set<State> endStates;
    private Set<State> states;
    private Set<Edge> edges;

    int currentNodeNumber = 0;

    public Graph() {
        states = new HashSet<>();
        edges = new HashSet<>();
        endStates = new HashSet<>();
    }

    public Set<State> getStates() {
        return this.states;
    }
    public void addNode(State node) {
        this.states.add(node);
    }

    public void removeNode(State node) {
        this.states.remove(node);
    }

    public Set<Edge> getEdges() {
        return this.edges;
    }
    public void addEdge(Edge edge) {
        edge.startState.outgoingEdges.add(edge);
        edge.endState.incomingEdges.add(edge);
        this.edges.add(edge);
    }
    public void removeEdge(Edge edge) {
        edge.startState.outgoingEdges.remove(edge);
        edge.endState.incomingEdges.remove(edge);
        this.edges.remove(edge);
    }

    public Set<Character> getAlphabet() {
        Set<Character> alphabet = new LinkedHashSet<>();
        for (Edge edge : edges) {
            alphabet.addAll(edge.characters);
        }
        return alphabet;
    }

    public void initializeStateIndices() {
        int i = 0;
        for (State state : getStates()) {
            state.setIndex(i);
            i++;
        }
    }

    public Set<State> getNextStates(Set<State> nodes, char a) {
        Set<State> nextNodes = new HashSet<>();
        for(State node : nodes) {
            nextNodes.add(node.getNextState(a));
        }
        return nextNodes;
    }

    public String validate() {
        if (states.size()==0) {
            return "start building or import your graph.";
        }
        if (states.size()==1) {
            State singleNode = states.iterator().next();
            if (singleNode.isStart && singleNode.isEnd && getAlphabet().size() > 0) return "valid";
        }

        for (State state: states) {
            if (!state.hasPredecessor() && !state.hasSuccessor()) {
                return "state " + state.getLabel() + " is not connected "; //not connected
            }
            if (state.outgoingEdges.size()==0 && !state.isEnd) {
                return "state " + state.getLabel() + " is a dead end "; //dead end
            }
            if (!state.hasPredecessor() && !state.isStart) {
                return "state " + state.getLabel() + " is not reachable "; //not reachable
            }
            Set<Character> seenCharacters = new HashSet<>();
            for (Edge outgoingEdge: state.outgoingEdges) {
                for (char c : outgoingEdge.characters) {
                    if (!seenCharacters.add(c)) {
                        return "multiple options from state " + state.getLabel() + " via character " + c; // This character is a duplicate, therefore not graph deterministic
                    }
                }
            }
            if (!isConnected()) return "graph is not connected";
        }
        return "valid";
    }

    private boolean isConnected() {
        Set<State> reachableNodes = new HashSet<>();
        reachableNodes.add(startState);
        startState.nextNodesRecursive(reachableNodes);
        return reachableNodes.size() == states.size();
    }



    public String getGraphState() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.validate()).append("\n");

        sb.append("States: \n");
        for (State node : states) {
            sb.append("  ").append(node.getLabel()).append(" at (").append(node.x).append(", ").append(node.y).append(")\n");
        }

        sb.append("\nEdges: \n");
        for (Edge edge : edges) {
            sb.append("  ").append(edge.startState.getLabel()).append(" -> ").append(edge.endState.getLabel())
                    .append(" with label: ")
                    .append(String.join(",", edge.characters.stream()
                            .map(String::valueOf)
                            .sorted()
                            .toArray(String[]::new)))
                    .append("\n");
        }

        sb.append("\nAlphabet: \n");
        sb.append("  ").append(String.join(",", getAlphabet().toString())).append("\n");

        if (startState != null) {
            sb.append("Start State: ").append(startState.getLabel()).append("\n");
        }
        if (endStates.size() != 0) {
            sb.append("End States: \n");
        }
        for (State endState: endStates) {
            sb.append(endState.getLabel()).append("\n");
        }

        return sb.toString();
    }

    public void exportGraph(File filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write nodes
            for (State state : states) {
                String type = (state == startState ? "START " : "") + (endStates.contains(state) ? "END " : "");
                writer.write("NODE," + state.number + "," + state.x + "," + state.y + "," + type.trim());
                writer.newLine();
            }

            // Write edges
            for (Edge edge : edges) {
                String characters = String.join(",", edge.characters.stream().map(String::valueOf).toList());
                writer.write("EDGE," + edge.startState.number + "," + edge.endState.number + ","
                        + characters + "," + edge.arrowType);
                writer.newLine();
            }

            System.out.println("Graph exported successfully!");
        } catch (IOException e) {
            System.err.println("Error exporting graph: " + e.getMessage());
        }
    }

    public void importGraph(File filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            states.clear();
            edges.clear();
            endStates.clear();
            startState = null;

            String line;
            Map<Integer, State> stateMap = new HashMap<>();
            this.currentNodeNumber = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals("NODE")) {
                    int number = Integer.parseInt(parts[1]);
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);
                    State state = new State(x, y, number, NODE_RADIUS);
                    this.currentNodeNumber++;

                    // Determine state type
                    if (parts.length > 4) {
                        if (parts[4].contains("START")) {
                            startState = state;
                            state.isStart = true;
                        }
                        if (parts[4].contains("END")) {
                            endStates.add(state);
                            state.isEnd = true;
                        }
                    }
                    states.add(state);
                    stateMap.put(number, state);
                } else if (parts[0].equals("EDGE")) {
                    int startNumber = Integer.parseInt(parts[1]);
                    int endNumber = Integer.parseInt(parts[2]);
                    Set<Character> characters = new LinkedHashSet<>();
                    for (String c : parts[3].split(",")) {
                        characters.add(c.charAt(0));
                    }
                    ArrowType arrowType = ArrowType.valueOf(parts[4]);
                    Edge edge = new Edge(stateMap.get(startNumber), stateMap.get(endNumber), characters, arrowType);
                    edges.add(edge);

                    // Update node references
                    stateMap.get(startNumber).outgoingEdges.add(edge);
                    stateMap.get(endNumber).incomingEdges.add(edge);
                }
            }

            System.out.println("Graph imported successfully!");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error importing graph: " + e.getMessage());
        }
    }
}
