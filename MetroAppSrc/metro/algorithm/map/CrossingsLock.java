package metro.algorithm.map;

import java.util.Arrays;

public class CrossingsLock {
    Crossing[] crossings;
    boolean[] isLocked;


    public CrossingsLock(Coordinates[] crossings) {
        this.crossings = new Crossing[crossings.length];
        for (int i = 0; i < crossings.length; i++)
            this.crossings[i] = new Crossing(crossings[i]);
        isLocked = new boolean[this.crossings.length];
        Arrays.fill(isLocked, false);
    }

    public void lockCrossing(Coordinates crossing) {
        int i = indexOf(crossing);
        crossings[i].lockCrossing();
        isLocked[i] = true;
    }

    public void unlockCrossing(Coordinates crossing) {
        int i = indexOf(crossing);
        crossings[i].unlockCrossing();
        isLocked[i] = false;
    }

    public boolean isLocked(Coordinates crossing) {
        return isLocked[indexOf(crossing)];
    }

    private int indexOf(Coordinates crossing) {
        Crossing newCr = new Crossing(crossing);
        for (int i = 0; i < crossings.length; i++) {
            if (crossings[i].equals(newCr))
                return i;
        }
        return -1;
    }
}
