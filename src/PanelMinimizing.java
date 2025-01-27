import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

class PanelMinimizing extends Panel {
    public PanelMinimizing(Graph graph) {
        super(graph);
        JButton backButton = new JButton("← Back");
        JButton nextButton = new JButton("Next →");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);

        add(buttonPanel, BorderLayout.SOUTH);



        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // For now, do nothing
                System.out.println("Back button clicked");
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                minimizeStepByStep(getPartitions()); //todo only one partition at a time, then repaint
                repaint();;
            }
        });
    }

    private List<Set<State>> getPartitions() {
        //(1)
        int t = 2;
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
        //GraphConverter graphConverter = new GraphConverter(graph, Q);
        return getPartitionsLog(t,Q,alphabet);
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
                if (nodes.contains(incomingEdge.startState)) { //edge within partition todo next
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
