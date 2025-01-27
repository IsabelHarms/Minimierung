public class StateEntry {
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
