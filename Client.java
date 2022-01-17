import java.io.IOException;

import org.json.JSONObject;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Client extends Thread {
    private final ClientConnection clientConnection = new ClientConnection();
    private final LinkedBlockingQueue<JSONObject> frameBuffer = new LinkedBlockingQueue<>();
    JSONObject configs;

    void connectAndVerify(String ip, int port, String passWord) {
        try {
            this.clientConnection.connect(ip, port);
            this.clientConnection.verify(passWord);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void connect(String ip, int port) {
        try {
            this.clientConnection.connect(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean isConnected() {
        return this.clientConnection.isConnected();
    }


    @Override
    public void run() {
        //event loop
        JSONObject jsonObject;
        while (this.isConnected()) {
            jsonObject = this.clientConnection.get();
            this.event(jsonObject);
        }
    }

    private void event(JSONObject jsonObject) {
        String CMD = (String) jsonObject.get("CMD");
        if (CMD.equals("FRAME")) {
            this.frameEvent(jsonObject);
        } else if (CMD.equals("CONFIGS")) {
            this.configsEvent(jsonObject);
        }
    }

    private void frameEvent(JSONObject jsonObject) {
        try {
            this.frameBuffer.offer(jsonObject, 1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void configsEvent(JSONObject jsonObject) {
        this.configs = jsonObject;
    }

    void getFrame(JSONObject jsonObject) {
        try {
            this.frameBuffer.poll(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void putCommand(JSONObject jsonObject) {
        this.clientConnection.put(jsonObject);
    }


    JSONObject getConfigs() {
        return this.configs;
    }
}
