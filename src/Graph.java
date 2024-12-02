import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class Graph {
    Node startNode;
    List<Node> endNodes;
    public List<Node> nodes;
    public List<Edge> edges;

    int currentNodeNumber = 0;

    public Graph() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        endNodes = new ArrayList<>();
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
