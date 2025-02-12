import java.util.LinkedList;
import java.util.Set;

public class ToList<StateList> extends LinkedList<StateList> {
    int i;
    int a;
    //Verweis auf K Eintrag
   Set<ToList<StateList>> FromSet;
    public ToList(Set<ToList<StateList>> FromSet, int i, int a) {
        System.out.println("toList generated: " + i + ", " + a);
        this.FromSet = FromSet;
        this.i = i;
        this.a = a;
    }

    public int getI() {
       return i;
    }
    public int getA() {
        return a;
    }
}
