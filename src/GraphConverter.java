import java.util.*;

public class GraphConverter {
    int deaSize;
    int alphabetSize;

    List<State> states;

    List<Character> alphabet;

    Graph graph;

    List<Set<State>> Q;

    Set<FromList<StateList<StateEntry>>> fromSet; //K
    StateEntry[][] StateEntryArray; //delta

    LinkedList<State>[][] predecessorArray; //delta^-1

    StateList<StateEntry>[][] gamma;

    public GraphConverter(Graph graph, List<Set<State>> Q) {
        this.deaSize = graph.getStates().size();
        this.alphabetSize = graph.getAlphabet().size();
        this.graph = graph;
        this.states = new ArrayList<>(graph.getStates());
        this.alphabet = new ArrayList<>(graph.getAlphabet());
        this.Q = Q;

        StateEntryArray = new StateEntry[deaSize][alphabetSize]; //delta
        fromSet = new HashSet<>(); // K
        initializeTransitionListAndK(fromSet, StateEntryArray);

        predecessorArray = new LinkedList[alphabetSize][deaSize]; //delta^-1
        fillInvertedDeltaArray();

        this.gamma = new StateList[alphabetSize][deaSize];
    }

    //initialize
    private void initializeTransitionListAndK(Set<FromList<StateList<StateEntry>>> fromSet, StateEntry[][] transitionListEntryArray) {
        for (char a : this.alphabet) {
            FromList<StateList<StateEntry>> fromList = new FromList<>(fromSet, 1, a); //delta'
            fromList.add(addTransitionList(1, a, 2, transitionListEntryArray, fromList));
            fromSet.add(fromList);

            FromList<StateList<StateEntry>> fromList1 = new FromList<>(fromSet, 2, a); //delta'
            fromList1.add(addTransitionList(2, a, 1, transitionListEntryArray, fromList1));
            fromSet.add(fromList1);
        }
    }
    private StateList<StateEntry> addTransitionList(int i, char a, int j, StateEntry[][] transitionListEntryArray, FromList<StateList<StateEntry>> head) {
        StateList<StateEntry> transitionList = new StateList<>(i, j, a, head);
        gamma[a][j] = transitionList;
        Set<State> Qstart = Q.get(i);
        int charIndex = alphabet.indexOf(a);
        for (State startState : Qstart) {
            State nextState = startState.getNextState(a);
            if (Q.get(j).contains(nextState)) {
                StateEntry transitionListEntry = new StateEntry(startState, transitionList);
                transitionList.add(transitionListEntry);                                                                // TransitionEntry
                int stateIndex = states.indexOf(startState);
                transitionListEntryArray[stateIndex][charIndex] = transitionListEntry;                                  //Add to delta array
            }
        }
        return transitionList;
    }

    private void fillInvertedDeltaArray() {
        for (int i = 0; i < alphabetSize; i++) {
            for (int j = 0; j < deaSize; j++) {
                LinkedList<State> previousStates = new LinkedList<>(states.get(j).getPreviousStates(alphabet.get(i)));  //p-List
                this.predecessorArray[i][j] = previousStates;
            }
        }
    }

    public void minimize() {
        while (!fromSet.isEmpty()) {
            // Get the next fromList to process
            FromList<StateList<StateEntry>> fromList = fromSet.iterator().next();
            fromSet.remove(fromList);  // Remove it from the worklist

            // Call the i method to refine the partitions
            refinePartition(fromList, fromList.getI());
        }
    }

    private void refinePartition(FromList<StateList<StateEntry>> fromList, int t) {
        StateList<StateEntry> transitionList1 = fromList.get(0);
        StateList<StateEntry> transitionList2 = fromList.get(1);
        StateList<StateEntry> shorterList = transitionList1.size() <= transitionList2.size() ? transitionList1 : transitionList2;

        createNewFromListAndMoveEntry(fromSet, fromList, t+1,fromList.a, shorterList);

        processStateList(shorterList, t);
    }

    private void processStateList(StateList<StateEntry> stateList, int t) {  //stateList i,a,j
        for (StateEntry stateEntry : stateList) {
            State currentState = stateEntry.state;
            for (char b : graph.getAlphabet()) { //1
                if(b != stateList.getA()) {
                    StateEntry otherStateEntry = StateEntryArray[states.indexOf(currentState)][alphabet.indexOf(b)]; //delta(q,b)
                    if (otherStateEntry == null) continue;
                    //other i,b,k
                    StateList<StateEntry> otherStateList = otherStateEntry.getHead();

                    StateList<StateEntry> lastGeneratedList = gamma[b][otherStateList.getJ()];

                    if (lastGeneratedList == null || lastGeneratedList.getI() != t+1) {
                        lastGeneratedList = createStateList(t+1,otherStateList.getJ(),b, otherStateList.getHead()); //todo
                    }
                    lastGeneratedList.add(otherStateEntry);
                    otherStateEntry.setHead(lastGeneratedList);

                    otherStateList.remove(otherStateEntry);
                    deleteIfEmpty(otherStateList);

                    // Update predecessor array
                    int charIndex = alphabet.indexOf(b);
                    int targetStateIndex = states.indexOf(otherStateEntry.state);
                    LinkedList<State> predecessors = predecessorArray[charIndex][targetStateIndex];
                    if (predecessors == null) {
                        predecessors = new LinkedList<>();
                        predecessorArray[charIndex][targetStateIndex] = predecessors;
                    }
                    predecessors.add(currentState);

                    // Update transition counts
                    StateList<StateEntry> oldList = otherStateEntry.getHead();
                    deleteIfEmpty(oldList);

                    // Update gamma
                    gamma[b][otherStateList.getJ()] = lastGeneratedList;

                    // Add the entry to the new list
                    lastGeneratedList.add(otherStateEntry);
                }
            }
        }

        // Final cleanup
        for (int i = 0; i < alphabetSize; i++) {
            for (int j = 0; j < deaSize; j++) {
                if (gamma[i][j] != null && gamma[i][j].isEmpty()) {
                    gamma[i][j] = null;  // Clear the reference
                }
            }
        }

        fromSet.removeIf(fromList -> fromList.isEmpty());
    }

    private void deleteIfEmpty(StateList<StateEntry> stateList) {
        if (stateList.size() == 0) {
            stateList.getHead().remove(stateList);
            gamma[stateList.getA()][stateList.getJ()] = null;
        }
        //todo eventually delete from K?
    }

    private void createNewFromListAndMoveEntry(Set<FromList<StateList<StateEntry>>> fromSet, FromList<StateList<StateEntry>> oldList, int i, char a, StateList<StateEntry> entry) {
        oldList.remove(entry);
        FromList<StateList<StateEntry>> newList = new FromList<StateList<StateEntry>>(fromSet, i, a);
        fromSet.add(newList);
        newList.add(entry);
        entry.setI(i);
        entry.setHead(newList);
        if(oldList.size() < 2) {
            fromSet.remove(oldList);
        }
    }

    private StateList<StateEntry> createStateList(int i, int j, char a, FromList<StateList<StateEntry>> head) {
        var stateList =  new StateList<StateEntry>(i,j,a, head);
        addGamma(stateList);
        head.add(stateList);
        return stateList;
    }

    private void addGamma(StateList<StateEntry> stateList) {
        gamma[stateList.getA()][stateList.getJ()] = stateList;
    }
}
