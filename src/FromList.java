import java.util.LinkedList;
import java.util.Set;

public class FromList<StateList> extends LinkedList<StateList> {
    int i;
    char a;
    //Verweis auf K Eintrag
   Set<FromList<StateList>> FromSet;
    public FromList(Set<FromList<StateList>> FromSet, int i, char a) {
        this.FromSet = FromSet;
        this.i = i;
        this.a = a;
    }

    public int getI() {
       return i;
    }
}
