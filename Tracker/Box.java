package Tracker;

import org.json.JSONArray;

public class Box {
    float x1, y1, x2, y2;
    int classId;
    int id, gen = 0;

    public Box(JSONArray box) {
        x1 = box.getFloat(0);
        y1 = box.getFloat(1);
        x2 = box.getFloat(2);
        y2 = box.getFloat(3);
        classId = box.getInt(4);
    }

    @Override
    public String toString() {
        return "Box{" +
                "x1=" + x1 +
                ", y1=" + y1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                ", class_id=" + classId +
                ", id=" + id +
                ", gen=" + gen +
                '}';
    }
}
