import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

class PanelMinimizing extends Panel {

    static Color[] partitionColors = {
            Color.WHITE,
            Color.YELLOW,
            Color.CYAN,
            Color.PINK,
            Color.GREEN,
            Color.LIGHT_GRAY,
            new Color(255, 165, 0),  // Orange
            new Color(173, 216, 230), // Hellblau
            new Color(144, 238, 144), // Hellgrün
            new Color(255, 182, 193), // Hellrosa
            new Color(240, 230, 140)  // Khaki
    };

    GraphConverter graphConverter;
    List<DT> D;

    int t;
    public PanelMinimizing(Graph graph) {
        super(graph);
        t = 0;
        JButton backButton = new JButton("← Back");
        JButton nextButton = new JButton("Next →");
        JButton finalizeButton = new JButton("Merge Nodes");
        backButton.setFont(new Font("Arial", Font.BOLD, 20)); // Größere Schrift
        backButton.setPreferredSize(new Dimension(150, 50)); // Größerer Button
        nextButton.setFont(new Font("Arial", Font.BOLD, 20)); // Größere Schrift
        nextButton.setPreferredSize(new Dimension(150, 50)); // Größerer Button
        finalizeButton.setFont(new Font("Arial", Font.BOLD, 20)); // Größere Schrift
        finalizeButton.setPreferredSize(new Dimension(200, 50)); // Größerer Button

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
                    for (Set<State> partition : transformSetOfListsToPartitions(D.get(t).lists)) {
                        if (partition.size() != 0) {
                            if(partition.size() == 1 && partition.iterator().next().isDefault) {
                                System.out.println("default state skipped");
                            } else {
                                mergeNodes(partition);
                            }
                        }
                    }
                    graph.removeNode(graph.defaultState);
                    graph.getEdges().removeAll(graph.defaultState.incomingEdges);

                    graph.defaultState = null;
                }
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(finalizeButton);
                if (frame != null) {
                    frame.setContentPane(new PanelGraph(graph));
                    frame.revalidate();
                    frame.repaint();
                }
            }

        });
        addDefaultState();
        graph.initializeStateIndices();
        repaint();
    }

    public void addDefaultState() {
        State defaultState = new State(100,100, graph.currentNodeNumber++, 30);
        graph.addNode(defaultState);
        defaultState.isDefault = true;
        graph.defaultState = defaultState;
        graph.addEdge(new Edge(defaultState, defaultState, graph.getAlphabet(), ArrowType.SELF));
        for (State state: graph.getStates()) {
            //collect characters
            Set<Character> unusedCharacters = new HashSet<>(graph.getAlphabet());
            for (Edge edge : state.outgoingEdges) {
                unusedCharacters.removeAll(edge.characters);
            }
            if (unusedCharacters.size() != 0) { //state needs defaultEdge
                graph.addEdge(new Edge(state, defaultState, unusedCharacters, ArrowType.STRAIGHT));
            }
        }
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
        List<Set<State>> partitions = transformSetOfListsToPartitions(D.get(t).lists);
        printPartitions(partitions);
        visualizePartitions(partitions);
        updateGraphStateTextArea(partitionColors, t);
    }
    private List<Set<State>> transformSetOfListsToPartitions(Set<StateList<StateEntry>> DT) {
        List<Set<State>> partitions = new ArrayList<>();
        Set<State> partition0 = new HashSet<>();
        partitions.add(partition0);
        for (StateList<StateEntry> list : DT) {
            for (StateEntry stateEntry : list) {
                if (partitions.size() >= list.getI()+1) { //Qi exists
                    Set<State> partition = partitions.get(list.getI());
                    partition.add(stateEntry.state);
                } else { //No Qi yet
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
        for (State state : graph.getStates()) { //collect states that do not appear in an L-list. non end states have been cleared previously
            if (state.isEnd && state.outgoingEdges.size() == 0) {
                partitions.get(1).add(state);
            }
        }
        return partitions;
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
    public void updateGraphStateTextArea(Color[] partitionColors, int t) {
        graphStateTextArea.setText("");
        StyledDocument doc = graphStateTextArea.getStyledDocument();

        try {
            SimpleAttributeSet headerStyle = new SimpleAttributeSet();
            StyleConstants.setBold(headerStyle, true);
            doc.insertString(doc.getLength(), "t = " + (t + 2) + "\n\n", headerStyle);

            doc.insertString(doc.getLength(), D.get(t).getKText() + "\n\n", null);

            D.get(t).lists.stream()
                    .sorted(Comparator.comparingInt(StateList::getI)) // Sortierung nach `getI()`
                    .forEach(stateList -> {
                        try {
                            int i = stateList.getI();
                            int a = stateList.getA();
                            int j = stateList.getJ();

                            SimpleAttributeSet colorI = new SimpleAttributeSet();
                            StyleConstants.setBackground(colorI, partitionColors[i]);
                            StyleConstants.setForeground(colorI, Color.BLACK);
                            StyleConstants.setBold(colorI, true);
                            doc.insertString(doc.getLength(), " " + i + " ", colorI);

                            doc.insertString(doc.getLength(), " → " + a + " → ", null);

                            SimpleAttributeSet colorJ = new SimpleAttributeSet();
                            StyleConstants.setBackground(colorJ, partitionColors[j]);
                            StyleConstants.setForeground(colorJ, Color.BLACK);
                            StyleConstants.setBold(colorJ, true);
                            doc.insertString(doc.getLength(), " " + j + "  ", colorJ);

                            doc.insertString(doc.getLength(), "[", null);
                            for (StateEntry stateEntry : stateList) {
                                doc.insertString(doc.getLength(), stateEntry.state.getLabel() + " ", null);
                            }
                            doc.insertString(doc.getLength(), "]\n", null);

                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    });

            doc.insertString(doc.getLength(), "\n" + D.get(t).getGammaText(), null);

        } catch (BadLocationException e) {
            e.printStackTrace();
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