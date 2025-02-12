import java.util.LinkedList;

public class StateList<T extends StateEntry> extends LinkedList<StateEntry> {
    private int j;
    private ToList<StateList<StateEntry>> head;
    public StateList(int j, ToList<StateList<StateEntry>> head) {
        this.j = j;
        this.head = head;
    }

    public int getI() {
        return head.getI();
    }

    public int getJ() {
        return j;
    }

    public int getA() {
        return head.a;
    }

    public ToList<StateList<StateEntry>> getHead() {
        return head;
    }

    public void setJ(int j) {
        this.j = j;
    }


    public void setHead(ToList<StateList<StateEntry>> head) {
        this.head = head;
    }

    public void moveEntryToList(StateEntry entry) {
        entry.getHead().removeEntry(entry);
        this.add(entry);
        entry.setHead((StateList<StateEntry>) this);
    }

    public void removeEntry(StateEntry entry) {
        this.remove(entry);
        deleteIfEmpty();
    }

    private void deleteIfEmpty() {
        if (this.size() == 0) {
            this.getHead().remove(this);
            this.getHead().FromSet.remove(this.getHead());
        }
    }
}
class StateEntry {
    State state;
    StateList<StateEntry> head;
    public StateEntry(State state, StateList<StateEntry> head) {
        this.state = state;
        this.head = head;
    }

    public void setHead(StateList<StateEntry> head) {
        this.head = head;
    }

    public StateList<StateEntry> getHead() {
        return head;
    }
}
