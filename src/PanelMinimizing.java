import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                // For now, do nothing
                minimize(graph);
            }
        });
    }

    private Graph minimize(Graph graph) {
        //(1)
        int t = 2;
        List<Set<Node>> Q = new ArrayList<>();
        Set<Node> Q0 = new HashSet<>();
        Set<Node> Q1 = new HashSet<>(graph.endNodes);
        Set<Node> Q2 = new HashSet<>(graph.nodes);
        Q2.removeAll(Q1);
        Q.add(Q0);
        Q.add(Q1);
        Q.add(Q2);
        int j,i;
        Set<Character> alphabet = graph.getAlphabet();
        //(2)
        while (true) {
            boolean changed = false;
            for (i = 1; i<= t; i++) {
                Set<Node> Qi = Q.get(i);
                for (char a: alphabet) {
                    for (j = 1; j<= t; j++) {
                        //1.
                        Set<Node> Qj = Q.get(j);
                        Set<Node> reachableStates = new HashSet<>();

                        for (Node state : Qi) {
                            Node next = state.getNextState(a);
                            if (next != null && Qj.contains(next)) {
                                reachableStates.add(state);
                            }
                        }

                        if (!reachableStates.isEmpty() && reachableStates.size() < Qi.size()) { //todo das stimmt glaub ich nicht
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
        //(3)
        Graph minimalGraph = new Graph();
        return minimalGraph;
    }
}
