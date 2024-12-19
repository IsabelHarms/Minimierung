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

    private List<Set<Node>> getPartitions() {
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
            for (int i = 1; i <= t; i++) {
                Set<Node> Qi = Q.get(i);
                for (char a : alphabet) {
                    for (int j = 0; j < t; j++) {
                        Set<Node> Qj = Q.get(j);

                        // Check for non-empty intersection (1.)
                        Set<Node> reachableStates = new HashSet<>();
                        for (Node state : Qi) {
                            Node next = state.getNextState(a);
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

    private void minimizeStepByStep(List<Set<Node>> Partitions) {
        for (Set<Node> partition : Partitions) {
            if(partition.size()!= 0) {
                mergeNodes(partition);
                repaint();
            }
        }
    }

    public void mergeNodes(Set<Node> nodes) {
        int x = 0;
        int y = 0;
        StringBuilder labelBuilder = new StringBuilder();
        Node newNode = new Node(x,y,++graph.currentNodeNumber, NODE_RADIUS);
        for(Node node: nodes) {
            if (node.isStart) newNode.isStart = true;
            if (node.isEnd) newNode.isEnd = true;
            labelBuilder.append(node.label);
            //averages of x and y as coordinates
                x += node.x;
                y += node.y;
            //add edge if new, add characters if old
            for (Edge incomingEdge : node.incomingEdges) {
                if (incomingEdge.startNode.connected(newNode) != null) {
                    incomingEdge.startNode.connected(newNode).characters.addAll(incomingEdge.characters); //todo
                }
                else {
                    Edge newEdge = new Edge(incomingEdge.startNode, newNode,incomingEdge.characters, incomingEdge.arrowType);
                    graph.addEdge(newEdge);
                }
            }
            for (Edge outgoingEdge : node.outgoingEdges) {
                if (newNode.connected(outgoingEdge.endNode) != null) {
                    newNode.connected(outgoingEdge.endNode).characters.addAll(outgoingEdge.characters);
                }
                else {
                    Edge newEdge = new Edge(newNode, outgoingEdge.endNode,outgoingEdge.characters, outgoingEdge.arrowType);
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
