package Tracker;

import java.util.*;

public class IDGenerator {
    Map<Integer, Integer> IDMap;

    IDGenerator() {
        this.IDMap = new HashMap<>();
    }

    void reset() {
        this.IDMap.clear();
    }

    int get(int classID){
        int id = this.IDMap.getOrDefault(classID, 0) + 1;
        this.IDMap.put(classID, id);
        return id;
    }


}
