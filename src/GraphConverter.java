import java.util.*;

public class GraphConverter {
    int deaSize;
    int alphabetSize;

    List<State> states;

    List<Character> alphabet;

    Graph graph;

    List<Set<State>> Q;

    Set<ToList<StateList<StateEntry>>> toSet; //K
    StateEntry[][] stateEntryArray; //delta

    LinkedList<State>[][] predecessorArray; //delta^-1

    StateList<StateEntry>[][] gammaStateEntry;
    StateList<StateEntry>[][] gammaPredecessors;

    private List<Set<State>> D;

    //todo index state attribute, charIndex

    public GraphConverter(Graph graph, List<Set<State>> Q) {
        graph.initializeStateIndices();
        this.deaSize = graph.getStates().size();
        this.alphabetSize = graph.getAlphabet().size();
        this.graph = graph;
        this.states = new ArrayList<>(graph.getStates());
        this.alphabet = new ArrayList<>(graph.getAlphabet());
        this.Q = Q;

        this.gammaStateEntry = new StateList[alphabetSize][deaSize];
        this.gammaPredecessors = new StateList[alphabetSize][deaSize];

        this.stateEntryArray = new StateEntry[deaSize][alphabetSize]; //delta
        toSet = new HashSet<>(); // K
        initializeTransitionListAndK(toSet, stateEntryArray);

        predecessorArray = new LinkedList[alphabetSize][deaSize]; //delta^-1
        fillInvertedDeltaArray();
        D = new ArrayList<>(Q);
        printLists();
        printK();
        System.out.println("initialisation finished \n");
    }

    private void printK() {
        for (ToList<StateList<StateEntry>> toList: toSet) {
            for (StateList<StateEntry> stateList: toList) {
                System.out.println(stateList.getI() + " " + stateList.getJ() + " " + stateList.getA());
                for (StateEntry stateEntry : stateList) {
                    System.out.println(stateEntry.state.getLabel());
                }
            }
        }
    }

    //initialize
    private void initializeTransitionListAndK(Set<ToList<StateList<StateEntry>>> fromSet, StateEntry[][] stateEntryArray) {
        for (char a : this.alphabet) {
            for (int i = 1; i <= 2; i++) {
                ToList<StateList<StateEntry>> toList = new ToList<>(fromSet, i, a); //delta'
                for (int j = 1; j <= 2; j++) {
                    StateList<StateEntry> stateList = addStateList(i, a, j, stateEntryArray, toList);
                    if (stateList.size() != 0) {
                        toList.add(stateList);
                    }
                }
                if(toList.size() >= 2) {
                    fromSet.add(toList);
                }
            }
        }
    }
    private StateList<StateEntry> addStateList(int i, char a, int j, StateEntry[][] transitionListEntryArray, ToList<StateList<StateEntry>> head) {
        StateList<StateEntry> transitionList = new StateList<>(j, head);
        gammaStateEntry[alphabet.indexOf(a)][j] = transitionList;
        Set<State> Qstart = Q.get(i);
        int charIndex = alphabet.indexOf(a);
        for (State startState : Qstart) {
            State nextState = startState.getNextState(a);
            if (Q.get(j).contains(nextState)) {
                StateEntry transitionListEntry = new StateEntry(startState, transitionList);
                transitionList.add(transitionListEntry);                                                                // TransitionEntry
                transitionListEntryArray[startState.getIndex()][charIndex] = transitionListEntry;                                  //Add to delta array
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

    public List<Set<State>> minimizeStep() {
            ToList<StateList<StateEntry>> toList = toSet.iterator().next();
            toSet.remove(toList);
            if(toList.size() >=2 ) {
                refinePartition(toList, toList.getI());
                printLists();
                printK();
                System.out.println("\n");
            }
            return Q;
    }

    private void printLists() {
        Set<StateList<StateEntry>> lists = new HashSet<>();
        for (int i = 0; i < alphabetSize; i++) {
            for (int j = 0; j < deaSize; j++) {
                if (this.stateEntryArray[j][i] != null) {
                    StateEntry stateEntry = this.stateEntryArray[j][i];
                    StateList<StateEntry> stateList = stateEntry.getHead();
                    lists.add(stateList);
                    System.out.println("State:" + stateEntry.state.label + ", List: i: " + stateList.getI() + ", j: " + stateList.getJ() + ", a: " + stateList.getA() + " head: " + stateList.getHead().getI() + stateList.getHead().getA());
                }
            }
        }
    }

    private void refinePartition(ToList<StateList<StateEntry>> fromList, int t) {
        StateList<StateEntry> transitionList1 = fromList.get(0);
        StateList<StateEntry> transitionList2 = fromList.get(1);
        StateList<StateEntry> shorterList = transitionList1.size() <= transitionList2.size() ? transitionList1 : transitionList2;

        createNewToListAndMoveEntry(toSet, fromList, t + 1, fromList.a, shorterList);

        processStateList(shorterList, t); // (ii)
    }

    private void processStateList(StateList<StateEntry> stateList, int t) {
        for (StateEntry stateEntry : stateList) {
            State currentState = stateEntry.state;
            for (char b : alphabet) {
                //int i = b-'a';
                int stateIndex = currentState.getIndex();
                int charIndex = alphabet.indexOf(b);
                if (stateIndex == -1 || charIndex == -1) continue; //todo this shouldnt happen

                if (b != stateList.getA()) { //(1)
                    updateStateEntryArray(charIndex, stateIndex, t, b);
                }
                //2
                updatePredecessorArray(charIndex, stateIndex, t, b);
            }
        }
        toSet.removeIf(toList -> toList.size() < 2);
    }

    private void updateStateEntryArray(int charIndex, int stateIndex, int t, char b) {
        StateEntry otherStateEntry = stateEntryArray[stateIndex][charIndex];
        if (otherStateEntry == null) return;

        //L(i,b,k)
        StateList<StateEntry> otherStateList = otherStateEntry.getHead();

        StateList<StateEntry> lastGeneratedList = getOrCreateLastGeneratedStateList(charIndex, otherStateList.getJ(), t, b);

        lastGeneratedList.moveEntryToList(otherStateEntry);
    }

    private void updatePredecessorArray(int charIndex, int stateIndex, int t, char b) { //(2)
        for (State p: predecessorArray[charIndex][stateIndex]) {
            StateEntry otherStateEntry = stateEntryArray[p.getIndex()][charIndex];
            if (otherStateEntry == null) continue;

            //L(k,b,i)
            StateList<StateEntry> otherStateList = otherStateEntry.getHead();
            StateList<StateEntry> lastGeneratedList = getOrCreateLastGeneratedPredecessorList(charIndex, otherStateList.getI(), t, b); //todo getJ?

            lastGeneratedList.moveEntryToList(otherStateEntry);
        }
    }

    private StateList<StateEntry> getOrCreateLastGeneratedStateList(int t, int charIndex, int k, char b) {
        StateList<StateEntry> lastGeneratedList = gammaStateEntry[charIndex][k];

        if (lastGeneratedList == null || lastGeneratedList.getI() != t + 1) {
            ToList<StateList<StateEntry>> newToList = new ToList<StateList<StateEntry>>(toSet,t+1,b);
            toSet.add(newToList);
            System.out.println("here1");
            lastGeneratedList = createStateList(k, newToList, charIndex);
            //modify gamma
            gammaStateEntry[charIndex][k] = lastGeneratedList;
        }
        return lastGeneratedList;
    }


    private StateList<StateEntry> getOrCreateLastGeneratedPredecessorList(int charIndex, int k, int t, char b) {
        StateList<StateEntry> lastGeneratedList = gammaPredecessors[charIndex][k];

        if (lastGeneratedList == null || lastGeneratedList.getJ() != t + 1) {
            ToList<StateList<StateEntry>> newToList = new ToList<StateList<StateEntry>>(toSet,k,b);
            toSet.add(newToList);
            System.out.println("here2");
            lastGeneratedList = createStateList(t + 1, newToList, charIndex);
            //modify gamma
            gammaPredecessors[charIndex][k] = lastGeneratedList;
        }
        return lastGeneratedList;
    }

    private void createNewToListAndMoveEntry(Set<ToList<StateList<StateEntry>>> fromSet, ToList<StateList<StateEntry>> oldList, int i, char a, StateList<StateEntry> entry) {
        oldList.remove(entry);
        ToList<StateList<StateEntry>> newList = new ToList<StateList<StateEntry>>(fromSet, i, a);
        fromSet.add(newList);
        newList.add(entry);
        entry.setHead(newList);
        if(oldList.size() < 2) {
            fromSet.remove(oldList);
        }
    }

    private StateList<StateEntry> createStateList(int j, ToList<StateList<StateEntry>> head, int charIndex) {
        System.out.println("StateListCreated:" + j);
        var stateList =  new StateList<StateEntry>(j, head);
        head.add(stateList);
        return stateList;
    }
}
