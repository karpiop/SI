package notepack;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;


public class Element {

    private List<Note> prev;
    private List<Pair<Note, Integer>> counterList = new ArrayList<>();

    public Element(List<Note> prev, Note current) {
        this.prev = new ArrayList<>(prev);
        increment(current);
    }

    /**
     * Increments counter corresponding to the element. If the counter doesn't exist yet, it creates default one.
     *
     * @param current this element's counter will be incremented
     */
    public void increment(Note current) {
        for (Pair<Note, Integer> pair : counterList) {
            if (pair.getKey().compareTo(current) == 0) {
                int newValue = pair.getValue() + 1;
                counterList.add(new Pair<>(pair.getKey(), newValue));
                counterList.remove(pair);
                return;
            }
        }

        counterList.add(new Pair<>(current, 1));
    }

    public List<Note> getPrev() {
        return prev;
    }

    public List<Pair<Note, Integer>> getCounterList() {
        return counterList;
    }

    public int getCounter(int index) {
        return counterList.get(index).getValue();
    }

    public Note get(int index) {
        return counterList.get(index).getKey();
    }


}
