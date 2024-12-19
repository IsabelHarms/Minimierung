import java.util.HashSet;
import java.util.Set;

class Edge {
    Node startNode, endNode;
    Set<Character> characters;
    String label;

    ArrowType arrowType;

    public Edge(Node startNode, Node endNode, Set<Character> characters, ArrowType arrowType) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.arrowType = arrowType;
        this.setCharactersAndLabel(characters); // Handles defensive copy and label setup
    }

    public String getLabel() {
        return label;
    }

    public void setCharactersAndLabel(Set<Character> characters) {
        // Create a mutable copy to avoid issues with immutable input sets
        this.characters = new HashSet<>(characters);
        this.label = this.characters.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}
