package Tracker;

import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

public class Tracker {
    float scoreThreshold;
    int generationLimit;
    IDGenerator idGenerator = new IDGenerator();
    List<Box> trackedBoxes = new LinkedList<>();

    public Tracker(float scoreThreshold, int generationLimit) {
        this.scoreThreshold = scoreThreshold;
        this.generationLimit = generationLimit;
    }

    public List<Box> get() {
        return get(0);
    }

    public List<Box> get(int generation) {
        List<Box> boxes = new LinkedList<>();
        for (Box box : this.trackedBoxes) {
            if (box.gen == generation) {
                boxes.add(box);
            }
        }
        return boxes;
    }

    public List<Box> getAll() {
        return this.trackedBoxes;
    }

    public void reset() {
        this.idGenerator.reset();
        this.trackedBoxes.clear();
    }

    public void update(JSONArray boxes) {
        if (this.trackedBoxes.size() == 0) {
            this.initialUpdate(boxes);
            return;
        }
        List<Box> tracked = new LinkedList<>();
        for (Object o : boxes) {
            Box box = new Box((JSONArray) o);
            Box matchBox = this.popMatchBox(box);
            if (matchBox == null) {
                box.id = this.idGenerator.get(box.classId);
                tracked.add(box);
            } else {
                box.id = matchBox.id;
                tracked.add(box);
            }
        }
        this.removeTooOldGeneration();
        this.trackedBoxes.addAll(tracked);
    }

    private void initialUpdate(JSONArray boxes) {
        for (Object o : boxes) {
            JSONArray box = (JSONArray) o;
            this.trackedBoxes.add(new Box(box));
        }
    }

    private Box popMatchBox(Box box) {
        float maxIOUScore = 0;
        Box maxScoreBox = null;
        for (Box existBox : this.trackedBoxes) {
            float IOUScore = this.calcIOUScore(box, existBox);
            if (IOUScore > maxIOUScore) {
                maxIOUScore = IOUScore;
                maxScoreBox = existBox;
            }
        }
        if (maxIOUScore >= this.scoreThreshold) {
            this.trackedBoxes.remove(maxScoreBox);
            return maxScoreBox;
        }
        return null;
    }

    private float calcIOUScore(Box box1, Box box2) {
        if (box1.classId != box2.classId) {
            return 0;
        }
        float xLeft = Math.max(box1.x1, box2.x1);
        float yTop = Math.max(box1.y1, box2.y1);
        float xRight = Math.min(box1.x2, box2.x2);
        float yBottom = Math.min(box1.y2, box2.y2);

        if (xRight < xLeft || yBottom < yTop) {
            return 0;
        }

        float interArea = (xRight - xLeft) * (yBottom - yTop);
        float box1Area = (box1.x2 - box1.x1) * (box1.y2 - box1.y1);
        float box2Area = (box2.x2 - box2.x1) * (box2.y2 - box2.y1);
        return interArea / (box1Area + box2Area - interArea);
    }

    private void removeTooOldGeneration() {
        this.trackedBoxes.removeIf(box -> ++box.gen > this.generationLimit);
    }

    @Override
    public String toString() {
        return "Tracker{" +
                "trackedBoxes=" + trackedBoxes +
                '}';
    }
}