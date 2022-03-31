import Tracker.*;
import org.json.JSONArray;

public class TrackerDemo {
    public static void main(String[] args) {
        int[][] boxes = {{0, 0, 50, 50, 0}, {50, 50, 100, 100, 0}};
        JSONArray jsonArray = new JSONArray(boxes);
        Tracker tracker = new Tracker(0.5f, 5);
        tracker.update(jsonArray);
        for (int i = 1; i <= 4; i++) {
            tracker.update(new JSONArray());
        }
        int[][] newBoxes = {{5, 5, 55, 55, 0}};
        JSONArray newJsonArray = new JSONArray(newBoxes);
        tracker.update(newJsonArray);
        System.out.println(tracker);
        System.out.println(tracker.get(5)); //get generation 5
        System.out.println(tracker.get(4)); // get 4 ...
        System.out.println(tracker.get(3));
        System.out.println(tracker.get(2));
        System.out.println(tracker.get(1));
        System.out.println(tracker.get()); // = tracker.get(0)
    }
}
