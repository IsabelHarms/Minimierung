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
                minimize(graph);
                repaint();;
            }
        });
    }

    private Graph minimize(Graph graph) {
        //(1)
        int t = 2;
        List<Set<Node>> Q = new ArrayList<>(new HashSet<>());
        Set<Node> Q0 = new HashSet<>();
        Set<Node> Q1 = new HashSet<>(graph.endNodes);
        Set<Node> Q2 = new HashSet<>(graph.getNodes());
        Q2.removeAll(Q1);
        Q.add(Q0);
        Q.add(Q1);
        Q.add(Q2);
        Set<Character> alphabet = graph.getAlphabet();
        //(2)
        while (true) {
            boolean changed = false;
            for (int i = 1; i < t; i++) {
                Set<Node> Qi = Q.get(i);
                for (char a : alphabet) {
                    for (int j = 0; j < t; j++) {
                        Set<Node> Qj = Q.get(j);

                        // Check for non-empty intersection (1.)
                        Set<Node> reachableStates = new HashSet<>();
                        for (Node state : Qi) {
                            Node next = state.getNextState(a);
                            System.out.println(next);
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

            // (3)
            int i = 0;
            for (Set<Node> partition : Q) {
                System.out.println(partition);
            }

            Map<Set<Node>, Node> partitionToNewState = new HashMap<>();
            int stateId = 0;

            // Create new states for each partition
            for (Set<Node> partition : Q) {
                partitionToNewState.put(partition, new Node(stateId*100, stateId* 100, stateId++, 30));
            }

            List<Edge> minimizedTransitions = new ArrayList<>();
/*
            // Generate transitions for the minimized DFA
            for (Set<Node> partition : Q) {
                Node newState = partitionToNewState.get(partition);
                for (char symbol : alphabet) {
                    Node representative = partition.iterator().next(); // Take any state as representative
                    Node target = representative.getNextState(symbol);

                    if (target != null) {
                        // Find the partition containing the target state
                        for (Set<Node> targetPartition : Q) {
                            if (targetPartition.contains(target)) {
                                Node targetNewState = partitionToNewState.get(targetPartition);
                                minimizedTransitions.add(new Edge(newState, targetNewState, Collections.singleton(symbol)));
                                break;
                            }
                        }
                    }
                }
            }*/

            // Create and return the minimized graph
            Graph minimizedGraph = new Graph();
            for (Node state : partitionToNewState.values()) {
                minimizedGraph.addNode(state);
            }
            /*for (Edge e : minimizedTransitions) {
                minimizedGraph.addEdge(e);
            }*/
        this.graph = minimizedGraph;
        return minimizedGraph;

    }
}
