import java.util.*;

import org.json.JSONObject;

public class Testing {
    public static void main(String[] args) {
        JSONObject jo = new JSONObject("{ \"abc\" : \"def\" }");
        System.out.println(jo.toString());
    }
}
