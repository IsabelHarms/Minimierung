import java.util.*;

public class GraphConverter {
    int deaSize;
    int alphabetSize;

    List<State> states;

    List<Character> alphabet;

    Graph graph;

    List<Set<State>> Q;

    Set<ToList<StateList<StateEntry>>> fromSet; //K
    StateEntry[][] stateEntryArray; //delta

    LinkedList<State>[][] predecessorArray; //delta^-1

    StateList<StateEntry>[][] gammaStateEntry;
    StateList<StateEntry>[][] gammaPredecessors;

    private List<Set<State>> D;

    public GraphConverter(Graph graph, List<Set<State>> Q) {
        this.deaSize = graph.getStates().size();
        this.alphabetSize = graph.getAlphabet().size();
        this.graph = graph;
        this.states = new ArrayList<>(graph.getStates());
        this.alphabet = new ArrayList<>(graph.getAlphabet());
        this.Q = Q;

        this.gammaStateEntry = new StateList[alphabetSize][deaSize];
        this.gammaPredecessors = new StateList[alphabetSize][deaSize];

        this.stateEntryArray = new StateEntry[deaSize][alphabetSize]; //delta
        fromSet = new HashSet<>(); // K
        initializeTransitionListAndK(fromSet, stateEntryArray);

        predecessorArray = new LinkedList[alphabetSize][deaSize]; //delta^-1
        fillInvertedDeltaArray();
        D = new ArrayList<>(Q);
    }

    //initialize
    private void initializeTransitionListAndK(Set<ToList<StateList<StateEntry>>> fromSet, StateEntry[][] stateEntryArray) {
        for (char a : this.alphabet) {
            for (int i = 1; i <= 2; i++) {
                ToList<StateList<StateEntry>> toList = new ToList<>(fromSet, i, a); //delta'
                for (int j = 1; j <= 2; j++) {
                    StateList<StateEntry> statelist = addStateList(i, a, j, stateEntryArray, toList);
                    if (statelist.size() != 0) {
                        toList.add(statelist);
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

    public List<Set<State>> minimize() {
        while (!fromSet.isEmpty()) {
            ToList<StateList<StateEntry>> toList = fromSet.iterator().next();
            fromSet.remove(toList);
            if(toList.size() >=2 ) {
                refinePartition(toList, toList.getI());
            }
            else {
                break; //todo, das sollte eigentlich nicht passieren
            }

        }
        Set<StateList<StateEntry>> lists = new HashSet<>();
        for (int i = 0; i < alphabetSize; i++) {
            for (int j = 0; j < deaSize; j++) {
                if (this.stateEntryArray[j][i] != null) {
                    StateEntry stateEntry = this.stateEntryArray[j][i];
                    StateList<StateEntry> stateList = stateEntry.getHead();
                    lists.add(stateList);
                    System.out.println("State:" + stateEntry.state.label + ", List: i: " + stateList.getI() + ", j: " + stateList.getJ() + ", a: " + stateList.getA());
                }
            }
        }
        return Q;
    }

    private void refinePartition(ToList<StateList<StateEntry>> fromList, int t) {
        StateList<StateEntry> transitionList1 = fromList.get(0);
        StateList<StateEntry> transitionList2 = fromList.get(1);
        StateList<StateEntry> shorterList = transitionList1.size() <= transitionList2.size() ? transitionList1 : transitionList2;

        createNewToListAndMoveEntry(fromSet, fromList, t + 1, fromList.a, shorterList);

        processStateList(shorterList, t); // (ii)
    }

    private void processStateList(StateList<StateEntry> stateList, int t) {
        for (StateEntry stateEntry : stateList) {
            State currentState = stateEntry.state;
            for (char b : alphabet) {
                int stateIndex = states.indexOf(currentState);
                int charIndex = alphabet.indexOf(b);
                if (stateIndex == -1 || charIndex == -1) continue; //todo this shouldnt happen

                if (b != stateList.getA()) { //(1)
                    updateStateEntryArray(charIndex, stateIndex, t, b);
                }
                //2
                updatePredecessorArray(charIndex, stateIndex, t, b);
            }
        }
        fromSet.removeIf(toList -> toList.size() < 2); //todo maybe isEmpty
    }

    private void updateStateEntryArray(int charIndex, int stateIndex, int t, char b) {
        StateEntry otherStateEntry = stateEntryArray[stateIndex][charIndex];
        if (otherStateEntry == null) return;

        //L(i,b,k)
        StateList<StateEntry> otherStateList = otherStateEntry.getHead();

        StateList<StateEntry> lastGeneratedList = getOrCreateLastGeneratedList(charIndex, otherStateList.getJ(), t, b);

        lastGeneratedList.moveEntryToList(otherStateEntry);
    }

    private StateList<StateEntry> getOrCreateLastGeneratedList(int i, int j, int t, char b) {
        StateList<StateEntry> lastGeneratedList = gammaStateEntry[i][j];

        if (lastGeneratedList == null || lastGeneratedList.getI() != t + 1) {
            ToList<StateList<StateEntry>> newToList = new ToList<StateList<StateEntry>>(fromSet,t+1,b);
            lastGeneratedList = createStateList(j, newToList);
            //modify gamma
            gammaStateEntry[i][j] = lastGeneratedList;
        }

        return lastGeneratedList;
    }

    private StateList<StateEntry> getOrCreateLastGeneratedList2(int charIndex, int i, int j, int t, char b) {
        StateList<StateEntry> lastGeneratedList = gammaPredecessors[charIndex][j];

        if (lastGeneratedList == null || lastGeneratedList.getJ() != t + 1) {
            ToList<StateList<StateEntry>> newToList = new ToList<StateList<StateEntry>>(fromSet,t+1,b);
            lastGeneratedList = createStateList(t + 1, newToList);
            //modify gamma
            gammaPredecessors[charIndex][j] = lastGeneratedList;
        }

        return lastGeneratedList;
    }

    private void updatePredecessorArray(int charIndex, int stateIndex, int t, char b) { //(2)
        for (State p: predecessorArray[charIndex][stateIndex]) {
            StateEntry otherStateEntry = stateEntryArray[states.indexOf(p)][charIndex];
            if (otherStateEntry == null) continue;

            //L(k,b,i)
            StateList<StateEntry> otherStateList = otherStateEntry.getHead();
            StateList<StateEntry> lastGeneratedList = getOrCreateLastGeneratedList2(charIndex, otherStateList.getI(), otherStateList.getJ(), t, b);

            lastGeneratedList.moveEntryToList(otherStateEntry);
        }
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

    private void moveEntry() {

    }

    private StateList<StateEntry> createStateList(int j, ToList<StateList<StateEntry>> head) {
        var stateList =  new StateList<StateEntry>(j, head);
        addGamma(stateList);
        head.add(stateList);
        return stateList;
    }

    private void addGamma(StateList<StateEntry> stateList) {
        gammaStateEntry[alphabet.indexOf(stateList.getA())][stateList.getJ()] = stateList;
    }
}
