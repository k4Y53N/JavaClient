import java.util.HashMap;
import org.json.JSONObject;
class CommandFactory {
    static HashMap<String, Object> Command() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("CMD", "");
        return hashMap;
    }

    static JSONObject LOGIN() {
        HashMap<String, Object> hashMap = Command();
        hashMap.put("CMD", "LOGIN");
        hashMap.put("PWD", "");
        return new JSONObject(hashMap);
    }

    public static void main(String[] args) {
        System.out.println(CommandFactory.LOGIN());
        JSONObject jo = new JSONObject(CommandFactory.LOGIN());
        System.out.println(jo);
    }
}
