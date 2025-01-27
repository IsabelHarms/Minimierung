import java.util.HashSet;
import java.util.Set;

class Edge {
    State startState, endState;
    Set<Character> characters;
    String label;

    ArrowType arrowType;

    public Edge(State startState, State endState, Set<Character> characters, ArrowType arrowType) {
        this.startState = startState;
        this.endState = endState;
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
