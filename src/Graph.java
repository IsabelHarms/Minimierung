import java.io.*;
import java.util.*;

class Graph {
    Node startNode;
    Set<Node> endNodes;
    private Set<Node> nodes;
    public Set<Edge> edges;

    int currentNodeNumber = 0;

    public Graph() {
        nodes = new HashSet<>();
        edges = new HashSet<>();
        endNodes = new HashSet<>();
    }

    public Set<Node> getNodes() {
        return this.nodes;
    }
    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void removeNode(Node node) {
        this.nodes.remove(node);
    }

    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }

    public Set<Character> getAlphabet() {
        Set<Character> alphabet = new LinkedHashSet<>();
        for (Edge edge : edges) {
            alphabet.addAll(edge.characters);
        }
        return alphabet;
    }

    public String validate() {
        for (Node node: nodes) {
            if (node.incomingEdges.size()==0 && node.outgoingEdges.size()==0) {
                return "node " + node.getLabel() + " is not connected "; //not connected
            }
            if (node.outgoingEdges.size()==0 && !node.isEnd) {
                return "node " + node.getLabel() + " is a dead end "; //dead end
            }
            if (node.incomingEdges.size()==0 && !node.isStart) {
                return "node " + node.getLabel() + " is not reachable "; //not reachable
            }
            Set<Character> seenCharacters = new HashSet<>();
            for (Edge outgoingEdge: node.outgoingEdges) {
                for (char c : outgoingEdge.characters) {
                    if (!seenCharacters.add(c)) {
                        return "multiple options from node " + node.getLabel() + " via character " + c; // This character is a duplicate, therefore not graph deterministic
                    }
                }
            }
        }
        return "valid";
    } //todo check for other invalid characteristics eg. z0-z1 z2-z3 and make 1 state possible

    public String getGraphState() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.validate()).append("\n");

        sb.append("Nodes: \n");
        for (Node node : nodes) {
            sb.append("  ").append(node.getLabel()).append(" at (").append(node.x).append(", ").append(node.y).append(")\n");
        }

        sb.append("\nEdges: \n");
        for (Edge edge : edges) {
            sb.append("  ").append(edge.startNode.getLabel()).append(" -> ").append(edge.endNode.getLabel())
                    .append(" with label: ")
                    .append(String.join(",", edge.characters.stream()
                            .map(String::valueOf)
                            .sorted()
                            .toArray(String[]::new)))
                    .append("\n");
        }

        sb.append("\nAlphabet: \n");
        Set<Character> alphabet = new LinkedHashSet<>();
        for (Edge edge : edges) {
            alphabet.addAll(edge.characters);
        }
        sb.append("  ").append(String.join(",", alphabet.toString())).append("\n");

        if (startNode != null) {
            sb.append("Start Node: ").append(startNode.getLabel()).append("\n");
        }
        if (endNodes.size() != 0) {
            sb.append("End Nodes: \n");
        }
        for (Node endNode: endNodes) {
            sb.append(endNode.getLabel()).append("\n");
        }

        return sb.toString();
    }

    public void exportGraph(File filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write nodes
            for (Node node : nodes) {
                String type = (node == startNode ? "START " : "") + (endNodes.contains(node) ? "END " : "");
                writer.write("NODE," + node.number + "," + node.x + "," + node.y + "," + type.trim());
                writer.newLine();
            }

            // Write edges
            for (Edge edge : edges) {
                String characters = String.join(",", edge.characters.stream().map(String::valueOf).toList());
                writer.write("EDGE," + edge.startNode.number + "," + edge.endNode.number + "," + characters);
                writer.newLine();
            }

            System.out.println("Graph exported successfully!");
        } catch (IOException e) {
            System.err.println("Error exporting graph: " + e.getMessage());
        }
    }

    public void importGraph(File filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            nodes.clear();
            edges.clear();
            endNodes.clear();
            startNode = null;

            String line;
            Map<Integer, Node> nodeMap = new HashMap<>();
            this.currentNodeNumber = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals("NODE")) {
                    int number = Integer.parseInt(parts[1]);
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);
                    Node node = new Node(x, y, number);
                    this.currentNodeNumber++;

                    // Determine node type
                    if (parts.length > 4) {
                        if (parts[4].contains("START")) {
                            startNode = node;
                            node.isStart = true;
                        }
                        if (parts[4].contains("END")) {
                            endNodes.add(node);
                            node.isEnd = true;
                        }
                    }
                    nodes.add(node);
                    nodeMap.put(number, node);
                } else if (parts[0].equals("EDGE")) {
                    int startNumber = Integer.parseInt(parts[1]);
                    int endNumber = Integer.parseInt(parts[2]);
                    Set<Character> characters = new LinkedHashSet<>();
                    for (String c : parts[3].split(",")) {
                        characters.add(c.charAt(0));
                    }
                    Edge edge = new Edge(nodeMap.get(startNumber), nodeMap.get(endNumber), characters);
                    edges.add(edge);

                    // Update node references
                    nodeMap.get(startNumber).outgoingEdges.add(edge);
                    nodeMap.get(endNumber).incomingEdges.add(edge);
                }
            }

            System.out.println("Graph imported successfully!");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error importing graph: " + e.getMessage());
        }
    }
}
