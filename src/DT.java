import java.util.*;

public class DT {
    int t;
    Set<StateList<StateEntry>> lists;
    LinkedList<ToList<StateList<StateEntry>>> K;
    StateEntry[][] stateEntryArray; //delta

    LinkedList<State>[][] predecessorArray; //delta^-1 //todo wie kann man das sinnvoll darstellen?

    StateList<StateEntry>[][] gammaStateEntry;
    StateList<StateEntry>[][] gammaPredecessors;
    List<Character> alphabet;

    DT(int t, List<Character> alphabet, int alphabetSize, int deaSize, LinkedList<ToList<StateList<StateEntry>>> K, StateEntry[][] stateEntryArray, StateList<StateEntry>[][] gammaStateEntry, StateList<StateEntry>[][] gammaPredecessors) {
        this.t = t;
        this.alphabet = alphabet;
        this.K = (LinkedList<ToList<StateList<StateEntry>>>) K.clone();
        this.stateEntryArray = stateEntryArray;
        this.gammaPredecessors = copyGammaArray(gammaPredecessors);
        this.gammaStateEntry = copyGammaArray(gammaStateEntry);
        this.lists = getLists(alphabetSize, deaSize);
    }

    private StateList<StateEntry>[][] copyGammaArray(StateList<StateEntry>[][] original) {
        if (original == null) return null;

        int rows = original.length;
        int cols = original[0].length;
        StateList<StateEntry>[][] copy = new StateList[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (original[i][j] != null) {
                    // Neue Instanz erstellen mit den gleichen Elementen (flache Kopie)
                    copy[i][j] = new StateList<>(original[i][j].getJ(), original[i][j].getHead());

                    // Elemente aus der Original-Liste in die neue Liste übertragen
                    for (StateEntry entry : original[i][j]) {
                        copy[i][j].add(entry);  // Kein tiefes Kopieren von `StateEntry`
                    }
                }
            }
        }
        return copy;
    }


    private Set<StateList<StateEntry>> getLists(int alphabetSize, int deaSize) {
        Set<StateList<StateEntry>> lists = new HashSet<>();
        for (int i = 0; i < alphabetSize; i++) {
            for (int j = 0; j < deaSize; j++) {
                if (this.stateEntryArray[j][i] != null) {
                    StateEntry stateEntry = this.stateEntryArray[j][i];
                    StateList<StateEntry> stateList = stateEntry.getHead();
                    lists.add((StateList<StateEntry>) stateList.clone());
                }
            }
        }
        return lists;
    }

    public String getKText() { //todo statelist ist nicht copy, enthält deshalb nur die Endeinträge
        StringBuilder sb = new StringBuilder();
        sb.append("K:\n");
        for (ToList<StateList<StateEntry>> toList : K) {
            sb.append("delta'(").append(toList.getI()).append(",").append(alphabet.get(toList.getA())).append(")\n");
        }
        return sb.toString();
    }

    public String getGammaText() {
        StringBuilder sb = new StringBuilder();

        sb.append("Gamma State Entry:\n");
        for (int i = 0; i < gammaStateEntry.length; i++) {
            for (int j = 0; j < gammaStateEntry[i].length; j++) {
                if (gammaStateEntry[i][j] != null) {
                    sb.append("[").append(alphabet.get(i)).append(", ").append(j).append("] -> ").append(getListText(gammaStateEntry[i][j]));
                }
            }
        }

        sb.append("\nGamma Predecessors:\n");
        for (int i = 0; i < gammaPredecessors.length; i++) {
            for (int j = 0; j < gammaPredecessors[i].length; j++) {
                if (gammaPredecessors[i][j] != null) {
                    sb.append("[").append(i).append(", ").append(alphabet.get(j)).append("] -> ").append(getListText(gammaPredecessors[i][j]));
                }
            }
        }

        return sb.toString();
    }

    private String getListText(StateList<StateEntry> list) {
        return "L(" + list.getI() + "," + alphabet.get(list.getA()) + "," + list.getJ() + ")\n";
    }

    public String getListsText() { //todo sort by a and i
        StringBuilder listsText = new StringBuilder();
        listsText.append("Lists: \n");
        for (StateList<StateEntry> stateList : lists) {
            listsText.append(getListText(stateList));
            for (StateEntry stateEntry: stateList) {
                listsText.append(stateEntry.state.getLabel()).append(" ");
            }
            listsText.append("\n");
        }
        return listsText.toString();
    }

}
