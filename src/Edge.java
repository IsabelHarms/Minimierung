import java.util.Set;

class Edge {
    Node startNode, endNode;
    Set<Character> characters;

    String label;

    boolean curved;

    public Edge(Node startNode, Node endNode, Set<Character> characters) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.characters = characters;
        this.setCharactersAndLabel(characters);
        this.curved = false; // By default, edge is not curved
    }

    public String getLabel() {
        return label;
    }

    public void setCharactersAndLabel(Set<Character> characters) {
        this.characters = characters;
        this.label = characters.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
    }
    
}
