import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Graph {
    Node startNode;
    Node[] endNodes;
    public List<Node> nodes;
    public List<Edge> edges;

    int currentNodeNumber = 0;

    public Graph() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        endNodes = new Node[]{};
    }


    public boolean validate() {
        for (Node node: nodes) {
            if (node.incomingEdges.size()==0 && node.outgoingEdges.size()==0) {
                return false; //not connected
            }
            if (node.outgoingEdges.size()==0 && !node.isEnd) {
                return false; //dead end
            }
            if (node.incomingEdges.size()==0 && !node.isStart) {
                return false; //not reachable
            }
            Set<Character> seenCharacters = new HashSet<>();
            for (Edge incomingEdge: node.incomingEdges) {
                for (char c : incomingEdge.characters) {
                    if (!seenCharacters.add(c)) {
                        return false; // This character is a duplicate, therefore not deterministic
                    }
                }
            }
        }
        return true;
    } //todo check for other invalid characteristics

    public String getGraphState() {
        StringBuilder sb = new StringBuilder();

        if (!this.validate()) {
            return "unfortunately this is not a valid graph :c.";
        }

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
        if (endNodes.length != 0) {
            sb.append("End Nodes: \n");
        }
        for (Node endNode: endNodes) {
            sb.append(endNode.getLabel()).append("\n");
        }

        return sb.toString();
    }

    public String exportGraph(File fileToSave) { //todo move this here
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
            // Write nodes
            for (Node node : nodes) {
                String type = "";
                if (node.isStart) type += "START ";
                if (node.isEnd) type += "END ";
                writer.write(node.number + "," + node.x + "," + node.y + "," + type.trim());
                writer.newLine();
            }

            // Write edges
            for (Edge edge : edges) {
                writer.write("EDGE," + edge.startNode + "," + edge.endNode);
                writer.newLine();
            }

            return("Graph exported successfully!");
        } catch (IOException e) {
            return("Error exporting graph: " + e.getMessage());
        }
    }
}
