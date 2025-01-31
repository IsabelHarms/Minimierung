import java.util.LinkedList;
import java.util.Set;

public class ToList<StateList> extends LinkedList<StateList> {
    int i;
    char a;
    //Verweis auf K Eintrag
   Set<ToList<StateList>> FromSet;
    public ToList(Set<ToList<StateList>> FromSet, int i, char a) {
        this.FromSet = FromSet;
        this.i = i;
        this.a = a;
    }

    public int getI() {
       return i;
    }
    public char getA() {
        return a;
    }
}
