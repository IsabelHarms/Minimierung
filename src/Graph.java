import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class Graph {
    static final int NODE_RADIUS = 30;
    State startState;
    Set<State> endStates;
    private Set<State> states;
    private Set<Edge> edges;

    public boolean isValid;

    int currentNodeNumber = 0;

    public Graph() {
        states = new HashSet<>();
        edges = new HashSet<>();
        endStates = new HashSet<>();
        isValid = true;
    }

    public Set<State> getStates() {
        return this.states;
    }
    public void addNode(State state) {
        if (state.isStart) {
            this.startState = state;
        }
        if (state.isEnd) {
            this.endStates.add(state);
        }
        this.states.add(state);
    }

    public void removeNode(State state) {
        if (state.isStart) {
            this.startState = null;
        }
        if (state.isEnd) {
            this.endStates.remove(state);
        }
        this.states.remove(state);
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

    public void removeUnnecessaryStates() {
        Set<State> reachableFromStart = reachableFromStart();
        Set<State> canReachEnd = canReachEnd();
        Set<State> toBeRemoved = new HashSet<>();
        Set<Edge> toBeRemovedEdges = new HashSet<>();
        for (State state : states) {
            if (!reachableFromStart.contains(state) || !canReachEnd.contains(state)) {
                toBeRemovedEdges.addAll(state.incomingEdges);
                toBeRemovedEdges.addAll(state.outgoingEdges);
                toBeRemoved.add(state);
            }
        }
        for (Edge removeEdge : toBeRemovedEdges) {
            removeEdge(removeEdge);

        }
        states.removeAll(toBeRemoved);
    }

    public String validate() {
        Set<State> reachableFromStart = reachableFromStart();
        Set<State> canReachEnd = canReachEnd();
        isValid = false;
        if (states.size()==0) {
            isValid = true;
            return "start building or import your graph.";
        }
        if (states.size()==1) {
            State singleNode = states.iterator().next();
            if (singleNode.isStart && getAlphabet().size() > 0) {
                isValid = true;
                return "valid";
            }
        }

        for (State state: states) {
            if (!state.hasPredecessor() && !state.hasSuccessor()) {
                return "state " + state.getLabel() + " is not connected "; //not connected
            }
            /*if (!state.hasPredecessor() && !state.isStart) {
                return "state " + state.getLabel() + " is not reachable "; //not reachable
            }*/
            Set<Character> seenCharacters = new HashSet<>();
            for (Edge outgoingEdge: state.outgoingEdges) {
                for (char c : outgoingEdge.characters) {
                    if (!seenCharacters.add(c)) {
                        return "multiple options from state " + state.getLabel() + " via character " + c; // This character is a duplicate, therefore not graph deterministic
                    }
                }
            }
            if (!reachableFromStart.contains(state) && !canReachEnd.contains(state)) {
                return state.getLabel() + " is not connected";
            }

            if (reachableFromStart.size() != states.size() || canReachEnd.size() != states.size()) {
                isValid = true;
                return "unnecessary states will be removed"; //todo
            }
        }
        isValid = true;
        return "flawless";
    }

    private Set<State> reachableFromStart() {
        Set<State> reachableNodes = new HashSet<>();
        if (startState == null) return reachableNodes;
        reachableNodes.add(startState);
        startState.nextNodesRecursive(reachableNodes);
        return reachableNodes;
    }

    private Set<State> canReachEnd() {
        Set<State> reachingNodes = new HashSet<>(endStates);
        if (endStates.size() == 0) return reachingNodes;
        for (State endState: endStates) {
            endState.previousNodesRecursive(reachingNodes);
        }
        return reachingNodes;
    }



    public String getGraphState() {
        StringBuilder sb = new StringBuilder();
        sb.append(isValid? "valid":"invalid").append("\n");
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
            // Knoten schreiben
            for (State state : states) {
                String type = (state == startState ? "START " : "") + (endStates.contains(state) ? "END " : "");
                writer.write("NODE," + state.number + "," + state.x + "," + state.y + "," + type.trim());
                writer.newLine();
            }

            // Kanten schreiben
            for (Edge edge : edges) {
                // Zeichen korrekt als Komma-separierte Liste speichern
                String characters = edge.characters.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(""));  // Zeichen ohne zusätzliche Kommas kombinieren

                writer.write("EDGE," + edge.startState.number + "," + edge.endState.number + ","
                        + characters + "," + edge.arrowType);
                writer.newLine();
            }

            System.out.println("Graph erfolgreich exportiert!");
        } catch (IOException e) {
            System.err.println("Fehler beim Exportieren des Graphen: " + e.getMessage());
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

                    // START / END Status setzen
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

                    // Zeichenliste korrekt parsen (alle Zeichen nach dem 3. Komma)
                    Set<Character> characters = new LinkedHashSet<>();
                    for (char c : parts[3].toCharArray()) {
                        if (c != ',') {  // Kommas ignorieren
                            characters.add(c);
                        }
                    }

                    ArrowType arrowType = ArrowType.valueOf(parts[4]);
                    Edge edge = new Edge(stateMap.get(startNumber), stateMap.get(endNumber), characters, arrowType);
                    edges.add(edge);

                    // Verknüpfungen zwischen Knoten aktualisieren
                    stateMap.get(startNumber).outgoingEdges.add(edge);
                    stateMap.get(endNumber).incomingEdges.add(edge);
                }
            }
            System.out.println("Graph erfolgreich importiert!");
        } catch (IOException e) {
            System.err.println("Fehler beim Importieren des Graphen: " + e.getMessage());
        }
    }

}
