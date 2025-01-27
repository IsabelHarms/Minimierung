import java.util.LinkedList;

public class StateList<StateEntry> extends LinkedList<StateEntry> {
    private int i;
    private int j;
    private char a;

    private FromList<StateList<StateEntry>> head;
    public StateList(int i, int j, char a, FromList<StateList<StateEntry>> head) {
        this.i = i;
        this.j = j;
        this.a = a;
        this.head = head;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public int getA() {
        return a;
    }

    public FromList<StateList<StateEntry>> getHead() {
        return head;
    }

    public void setI(int i) {
        this.i = i;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public void setA(char a) {
        this.a = a;
    }

    public void setHead(FromList<StateList<StateEntry>> head) {
        this.head = head;
    }
}
//für ein a übergänge in mindestens zwei andere StateLists
