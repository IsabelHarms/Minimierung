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

    private List<Set<StateList<StateEntry>>> D;

    //todo index state attribute, charIndex

    public GraphConverter(Graph graph, List<Set<State>> Q) {
        this.deaSize = graph.getStates().size();
        this.alphabetSize = graph.getAlphabet().size();
        this.graph = graph;
        this.states = new ArrayList<>(graph.getStates());
        this.alphabet = new ArrayList<>(graph.getAlphabet());
        this.Q = Q;

        this.gammaStateEntry = new StateList[alphabetSize][deaSize];
        this.gammaPredecessors = new StateList[deaSize][alphabetSize];

        this.stateEntryArray = new StateEntry[deaSize][alphabetSize]; //delta
        toSet = new HashSet<>(); // K
        initializeTransitionListAndK(toSet, stateEntryArray);

        predecessorArray = new LinkedList[alphabetSize][deaSize]; //delta^-1
        fillInvertedDeltaArray();
        D = new ArrayList<>();
        D.add(getDT());
        printLists();
        printK();
        System.out.println("initialisation finished \n");
    }

    private void printK() {
        for (ToList<StateList<StateEntry>> toList: toSet) {
            System.out.println("toList:" + toList.getI() + ", "  + toList.getA());
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
        for (int a = 0; a < alphabetSize; a++) {
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
    private StateList<StateEntry> addStateList(int i, int a, int j, StateEntry[][] transitionListEntryArray, ToList<StateList<StateEntry>> head) {
        StateList<StateEntry> transitionList = new StateList<>(j, head);
        gammaStateEntry[a][j] = transitionList;
        gammaPredecessors[i][a] = transitionList;
        Set<State> Qstart = Q.get(i);
        for (State startState : Qstart) {
            State nextState = startState.getNextState(this.alphabet.get(a));
            if (Q.get(j).contains(nextState)) {
                StateEntry transitionListEntry = new StateEntry(startState, transitionList);
                transitionList.add(transitionListEntry);                                                                // TransitionEntry
                transitionListEntryArray[startState.getIndex()][a] = transitionListEntry;                                  //Add to delta array
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

    public List<Set<StateList<StateEntry>>> minimize() {
        int t = 2;
        while (!toSet.isEmpty()) {
            ToList<StateList<StateEntry>> toList = toSet.iterator().next();
            toSet.remove(toList);
            if(toList.size() >=2 ) {
                refinePartition(toList, t);
                System.out.println("t: " + toList.getI()); //todo???
                printLists();
                printK();
                System.out.println("\n");
            }
            D.add(getDT());
            t++;
        }
        return D;
    }

    private void printLists() {
        Set<StateList<StateEntry>> lists = new HashSet<>();
        for (int i = 0; i < alphabetSize; i++) {
            for (int j = 0; j < deaSize; j++) {
                if (this.stateEntryArray[j][i] != null) {
                    StateEntry stateEntry = this.stateEntryArray[j][i];
                    StateList<StateEntry> stateList = stateEntry.getHead();
                    lists.add(stateList);
                    System.out.println("State:" + stateEntry.state.label + ", List: i: " + stateList.getI() + ", j: " + stateList.getJ() + ", a: " + stateList.getA() + " head: " + stateList.getHead().getI() + stateList.getHead().getA() + stateList.getHead());
                }
            }
        }
    }

    private Set<StateList<StateEntry>> getDT() {
        Set<StateList<StateEntry>> lists = new HashSet<>();
        for (int i = 0; i < alphabetSize; i++) {
            for (int j = 0; j < deaSize; j++) {
                if (this.stateEntryArray[j][i] != null) {
                    StateEntry stateEntry = this.stateEntryArray[j][i];
                    StateList<StateEntry> stateList = stateEntry.getHead();
                    lists.add((StateList<StateEntry>) stateList.clone()); //todo objekt wird noch verändert.
                }
            }
        }
        return lists;
    }

    private void printGammaPredecessor() {
        for (int i = 0; i < alphabetSize; i++) {
            for (int j = 0; j < deaSize; j++) {
                if (gammaPredecessors[j][i] != null) {
                    System.out.println("[" + j + i + "]: " + gammaPredecessors[j][i].getI() + gammaPredecessors[j][i].getJ() + gammaPredecessors[j][i].getA());
                }
            }
        }
    }

    private void printGammaStateEntry() {
        for (int i = 0; i < alphabetSize; i++) {
            for (int j = 0; j < deaSize; j++) {
                if (gammaStateEntry[i][j] != null) {
                    System.out.println("[" + i + j + "]: " + gammaStateEntry[i][j].getI() + gammaStateEntry[i][j].getJ() + gammaStateEntry[i][j].getA());
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
            for (int b = 0; b < alphabetSize; b++) {
                //int i = b-'a';
                int stateIndex = currentState.getIndex();
                if (stateIndex >= deaSize || b >= alphabetSize) return; //todo this shouldnt happen

                if (b != stateList.getA()) { //(1)
                    updateStateEntryArray(b, stateIndex, t);
                }
                //2
                updatePredecessorArray(b, stateIndex, t);
            }
        }
        toSet.removeIf(toList -> toList.size() < 2);
    }

    private void updateStateEntryArray(int b, int stateIndex, int t) {
        StateEntry otherStateEntry = stateEntryArray[stateIndex][b];
        if (otherStateEntry == null) return;

        //L(i,b,k)
        StateList<StateEntry> otherStateList = otherStateEntry.getHead();
        StateList<StateEntry> lastGeneratedList = getOrCreateLastGeneratedStateList(t, b, otherStateList.getJ());

        lastGeneratedList.moveEntryToList(otherStateEntry);
    }

    private void updatePredecessorArray(int charIndex, int stateIndex, int t) { //(2)
        for (State p: predecessorArray[charIndex][stateIndex]) {
            StateEntry otherStateEntry = stateEntryArray[p.getIndex()][charIndex];
            if (otherStateEntry == null) continue;

            //L(k,b,i)
            StateList<StateEntry> otherStateList = otherStateEntry.getHead();
            StateList<StateEntry> lastGeneratedList = getOrCreateLastGeneratedPredecessorList(charIndex, otherStateList.getI(), t); //todo getJ?

            lastGeneratedList.moveEntryToList(otherStateEntry);
        }
    }

    private StateList<StateEntry> getOrCreateLastGeneratedStateList(int t, int b, int k) {
        //L(t+1,b,k)?
        StateList<StateEntry> lastGeneratedList = gammaStateEntry[b][k];

        printGammaStateEntry();
        System.out.println("charIndex: " + b + ", k: " + k);
        if (lastGeneratedList == null || lastGeneratedList.getI() != t + 1) {
            System.out.println("decided against last generated statelist");
            ToList<StateList<StateEntry>> newToList = new ToList<StateList<StateEntry>>(toSet,t+1,b);
            toSet.add(newToList);
            lastGeneratedList = createStateList(k, newToList);
            //modify gamma
            gammaStateEntry[b][k] = lastGeneratedList;
        }
        return lastGeneratedList;
    }


    private StateList<StateEntry> getOrCreateLastGeneratedPredecessorList(int b, int k, int t) {
        StateList<StateEntry> lastGeneratedList = gammaPredecessors[k][b];
//L(k,b,t+1)
        printGammaPredecessor();
        System.out.println("k: " + k + ", charIndex: " + b);
        if (lastGeneratedList == null || lastGeneratedList.getJ() != t + 1) {
            ToList<StateList<StateEntry>> newToList;
            if (lastGeneratedList != null) {
                newToList = lastGeneratedList.getHead();
            }
            else {
                System.out.println("decided against last generated predecessor list"); //todo doesnt work for 4,0 bc gamma was updated for stateentry not predecessor
                newToList = new ToList<StateList<StateEntry>>(toSet,k,b);
            }
            toSet.add(newToList);
            lastGeneratedList = createStateList(t + 1, newToList);
            //modify gamma
            gammaPredecessors[k][b] = lastGeneratedList;
        }
        return lastGeneratedList;
    }

    private void createNewToListAndMoveEntry(Set<ToList<StateList<StateEntry>>> fromSet, ToList<StateList<StateEntry>> oldList, int i, int a, StateList<StateEntry> entry) {
        oldList.remove(entry);
        System.out.println("here1");
        ToList<StateList<StateEntry>> newList = new ToList<StateList<StateEntry>>(fromSet, i, a);
        fromSet.add(newList);
        newList.add(entry);
        entry.setHead(newList);
        if(oldList.size() < 2) {
            fromSet.remove(oldList);
        }
    }

    private StateList<StateEntry> createStateList(int j, ToList<StateList<StateEntry>> head) {
        System.out.println("StateListCreated:" + j);
        var stateList =  new StateList<StateEntry>(j, head);
        head.add(stateList);
        return stateList;
    }
}
