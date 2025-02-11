import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

class PanelMinimizing extends Panel {

    static Color[] partitionColors = {Color.YELLOW, Color.CYAN, Color.PINK, Color.GREEN, Color.LIGHT_GRAY, Color.getColor("#fff8dc")};

    GraphConverter graphConverter;
    List<Set<StateList<StateEntry>>> D;

    int t;
    public PanelMinimizing(Graph graph) {
        super(graph);
        t = 0;
        JButton backButton = new JButton("← Back");
        JButton nextButton = new JButton("Next →");

        JButton finalizeButton = new JButton("Merge Nodes");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(finalizeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphConverter != null && t > 0) {
                    t--;
                    visualizeStep();
                }
                repaint();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphConverter == null) {
                    getPartitions();
                    visualizeStep();
                } else if (t >= D.size()-1){
                    //merge nodes
                } else {
                    t++;
                    visualizeStep();
                }
                repaint();
            }
        });
        finalizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graphConverter != null) {
                    for (Set<State> partition : transformSetOfListsToPartitions(D.get(t))) {
                        if (partition.size() != 0) {
                            mergeNodes(partition);
                        }
                    }
                }
                repaint();
            }
        });
    }

    private void getPartitions() {
        //(1)
        List<Set<State>> Q = new ArrayList<>(new HashSet<>());
        Set<State> Q0 = new HashSet<>();
        Set<State> Q1 = new HashSet<>(graph.endStates);
        Set<State> Q2 = new HashSet<>(graph.getStates());
        Q2.removeAll(Q1);
        Q.add(Q0);
        Q.add(Q1);
        Q.add(Q2);
        Set<Character> alphabet = graph.getAlphabet();
        //(2)
        getPartitionsNLOGN(Q);
    }

    private void getPartitionsNLOGN(List<Set<State>> Q) {
        graphConverter = new GraphConverter(graph, Q);
        D = graphConverter.minimize();
    }

    private void visualizeStep() {
        List<Set<State>> partitions = transformSetOfListsToPartitions(D.get(t));
        printPartitions(partitions);
        visualizePartitions(partitions);
        graphStateTextArea.setText("t = " + t  + ": \n" + getListsText(D.get(t)));
    }
    private List<Set<State>> transformSetOfListsToPartitions(Set<StateList<StateEntry>> DT) {
        List<Set<State>> partitions = new ArrayList<>();
        Set<State> partition0 = new HashSet<>();
        partitions.add(partition0);
        for (StateList<StateEntry> list : DT) {
            for (StateEntry stateEntry : list) {
                System.out.print(stateEntry.state.getLabel());
                if (partitions.size() >= list.getI()+1) { //Qi exists
                    System.out.println("1");
                    Set<State> partition = partitions.get(list.getI());
                    partition.add(stateEntry.state);
                } else { //No Qi yet
                    System.out.println("2");
                    Set<State> partition = new HashSet<>();
                    partition.add(stateEntry.state);
                    while (partitions.size() <= list.getI()) {
                        partitions.add(new HashSet<>());
                    }
                    partitions.add(list.getI(), partition);
                }
                stateEntry.state.setPartition(list.getI());
            }
        }
        return partitions;
    }

    private String getListsText(Set<StateList<StateEntry>> DT) {
        StringBuilder listsText = new StringBuilder();
        for (StateList<StateEntry> stateList : DT) {
            listsText.append("List: i: ").append(stateList.getI()).append(", a: ").append(stateList.getA()).append(", j: ").append(stateList.getJ()).append("\n");
            for (StateEntry stateEntry: stateList) {
                listsText.append(stateEntry.state.getLabel()).append(" ");
            }
            listsText.append("\n");
        }
        return listsText.toString();
    }

    private void visualizePartitions(List<Set<State>> Q) {
        for (int i = 0; i < Q.size(); i++) {
            Color color = Color.WHITE;
            if (i < partitionColors.length) {
                color = partitionColors[i];
            }
            for (State state : Q.get(i)) {
                state.setPartition(i);
                state.setColor(color);
            }
        }
        repaint();
    }

    private void printPartitions(List<Set<State>> Q) {
        for (int i = 0; i < Q.size(); i++) {
            System.out.print(i + ": ");
            for (State state : Q.get(i)) {
                System.out.print(state.getLabel());
            }
            System.out.print("\n");
        }
    }
    public List<Set<State>> getPartitionsHopcroft(List<Set<State>> Q, Set<Character> alphabet) {
        // The worklist contains partitions to be split
        Queue<Set<State>> worklist = new LinkedList<>(Q);

        while (!worklist.isEmpty()) {
            // Select and remove a partition from the worklist
            Set<State> A = worklist.poll();

            for (char c : alphabet) {
                // Precompute states that transition to `A` on `c`
                Set<State> P = computeTransitionSet(A, c, Q);

                // Split each partition in Q using P
                Q = splitPartitions(Q, P, worklist);
            }
        }
        return Q;
    }

    private Set<State> computeTransitionSet(Set<State> A, char c, List<Set<State>> Q) {
        Set<State> P = new HashSet<>();
        for (State state : A) {
            P.addAll(state.getPreviousStates(c));
        }
        return P;
    }

    private List<Set<State>> splitPartitions(List<Set<State>> Q, Set<State> P, Queue<Set<State>> worklist) {
        List<Set<State>> newPartitions = new ArrayList<>();

        for (Set<State> partition : Q) {
            Set<State> intersect = new HashSet<>(partition);
            intersect.retainAll(P);

            Set<State> difference = new HashSet<>(partition);
            difference.removeAll(P);

            if (!intersect.isEmpty() && !difference.isEmpty()) {
                newPartitions.add(intersect);
                newPartitions.add(difference);

                // Add the smaller subset to the worklist
                if (worklist.contains(partition)) {
                    worklist.remove(partition);
                    worklist.add(intersect);
                    worklist.add(difference);
                } else {
                    worklist.add(intersect.size() < difference.size() ? intersect : difference);
                }
            } else {
                newPartitions.add(partition);
            }
        }
        return newPartitions;
    }

    private List<Set<State>> getPartitionsLog(int t, List<Set<State>> Q, Set<Character> alphabet) {
        //1.
        for (int j1 = 0; j1 <= t; j1++) {
            for (int j2 = 0; j2 <= t; j2++) {
                if (j1 != j2) { //choose j1 and j2
                    for (int i = 0; i <= t; i++) { //todo <= ?
                        for (char a : alphabet) {
                            Set<State> nextStates = graph.getNextStates(Q.get(i), a);
                            Set<State> intersection1 = new HashSet<>(nextStates);
                            Set<State> intersection2 = new HashSet<>(nextStates);
                            intersection1.retainAll(Q.get(j1));
                            intersection2.retainAll(Q.get(j2));
                            if (intersection1.size() != 0 && intersection2.size() != 0) {
                                //2.
                                if(intersection1.size() <= intersection2.size()) {
                                    Q.add(intersection1);
                                }
                                else {
                                    Q.add(intersection2);
                                }
                                Q.get(i).removeAll(Q.get(++t));
                            }
                        }
                    }
                }
            }
        }
        return Q;
    }


    private List<Set<State>> getPartitionsQuadratic(int t, List<Set<State>> Q, Set<Character> alphabet) {
        while (true) {
            boolean changed = false;
            for (int i = 1; i <= t; i++) {
                Set<State> Qi = Q.get(i);
                for (char a : alphabet) {
                    for (int j = 0; j < t; j++) {
                        Set<State> Qj = Q.get(j);

                        // Check for non-empty intersection (1.)
                        Set<State> reachableStates = new HashSet<>();
                        for (State state : Qi) {
                            State next = state.getNextState(a);
                            if (next != null && Qj.contains(next)) {
                                reachableStates.add(state);
                            }
                        }
                        if (!reachableStates.isEmpty() && reachableStates.size() < Qi.size()) {
                            // Refine the partition (2.)

                            Q.add(reachableStates);
                            Qi.removeAll(reachableStates);
                            t++;
                            changed = true;
                            break;
                        }
                    }
                }
            }
            if (!changed) break;
        }
        return Q;
    }

    private void minimizeStepByStep(List<Set<State>> Partitions) {
        for (Set<State> partition : Partitions) {
            if(partition.size()!= 0) {
                mergeNodes(partition);
                repaint();
            }
        }
    }

    public void mergeNodes(Set<State> nodes) {
        int x = 0;
        int y = 0;
        StringBuilder labelBuilder = new StringBuilder();
        State newNode = new State(x,y,++graph.currentNodeNumber, NODE_RADIUS);
        for(State node: nodes) {
            if (node.isStart) newNode.isStart = true;
            if (node.isEnd) newNode.isEnd = true;
            labelBuilder.append(node.label);
            //averages of x and y as coordinates
                x += node.x;
                y += node.y;
            //add edge if new, add characters if old
            for (Edge incomingEdge : node.incomingEdges) {
                if (nodes.contains(incomingEdge.startState)) { //edge within partition
                    Edge selfEdge = newNode.connected(newNode);
                    if (selfEdge == null) {
                        Edge newEdge = new Edge(newNode, newNode, incomingEdge.characters, ArrowType.SELF);
                        graph.addEdge(newEdge);
                    }
                    else {
                        selfEdge.characters.addAll(incomingEdge.characters);
                    }
                }
                if (incomingEdge.startState.connected(newNode) != null) {
                    incomingEdge.startState.connected(newNode).characters.addAll(incomingEdge.characters); // merge characters
                    incomingEdge.startState.connected(newNode).setCharactersAndLabel(
                            incomingEdge.startState.connected(newNode).characters); // update label
                } else {
                    Edge newEdge = new Edge(incomingEdge.startState, newNode, incomingEdge.characters, incomingEdge.arrowType);
                    graph.addEdge(newEdge);
                }
            }
            for (Edge outgoingEdge : node.outgoingEdges) {
                if (newNode.connected(outgoingEdge.endState) != null) {
                    newNode.connected(outgoingEdge.endState).characters.addAll(outgoingEdge.characters); // merge characters
                    newNode.connected(outgoingEdge.endState).setCharactersAndLabel(
                            newNode.connected(outgoingEdge.endState).characters); // update label
                } else {
                    Edge newEdge = new Edge(newNode, outgoingEdge.endState, outgoingEdge.characters, outgoingEdge.arrowType);
                    graph.addEdge(newEdge);
                }
            }
            for (Edge edge: new ArrayList<>(node.incomingEdges)) {
                graph.removeEdge(edge);
            }
            for (Edge edge: new ArrayList<>(node.outgoingEdges)) {
                graph.removeEdge(edge);
            }
            graph.removeNode(node);
        }
        //set node to average position of nodes in partition
        newNode.x = x/ nodes.size();
        newNode.y = y/ nodes.size();
        newNode.setLabel(labelBuilder.toString());
        graph.addNode(newNode);
    }
}
