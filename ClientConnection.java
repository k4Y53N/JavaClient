import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ClientConnection extends Thread {
    private final LinkedBlockingQueue<JSONObject> inputBuffer = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<JSONObject> outputBuffer = new LinkedBlockingQueue<>();
    private final Socket socket = new Socket();

    boolean isConnected() {
        return this.socket.isConnected();
    }

    void close() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void connect(String ip, int port) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(ip, port);
        this.socket.connect(inetSocketAddress);
    }

    void verify(String passWord) {
        if (!this.isConnected()) {
            return;
        }
        JSONObject login = CommandFactory.LOGIN();
        login.put("PWD", passWord);
        try {
            this.writeMessage(this.socket.getOutputStream(), login);
            JSONObject loginInfo = this.readMessage(this.socket.getInputStream());
            if (!loginInfo.getBoolean("VERIFY")) {
                this.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (!this.isConnected()) {
            return;
        }

        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    reading();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread writeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    writing();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        readThread.start();
        writeThread.start();
        try {
            readThread.join();
            writeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void reading() throws IOException {
        InputStream inputStream = this.socket.getInputStream();
        while (this.isConnected()) {
            try {
                JSONObject message = this.readMessage(inputStream);
                this.inputBuffer.offer(message, 1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                this.close();
            }
        }
    }

    private JSONObject readMessage(InputStream inputStream) throws IOException {
        int headSize = ByteBuffer.wrap(this.readAll(inputStream, 4)).getInt();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(headSize);
        byteArrayOutputStream.writeBytes(this.readAll(inputStream, headSize));
        return new JSONObject(byteArrayOutputStream.toString());
    }

    private byte[] readAll(InputStream inputStream, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int total = 0;
        while (total < bufferSize) {
            int receive = inputStream.read(buffer, total, bufferSize - total);
            if (receive <= 0) throw new IOException("Can't read any byte from inputStream");
            total += receive;
        }
        return buffer;
    }

    private void writing() throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        JSONObject jsonObject;
        while (this.isConnected()) {
            try {
                jsonObject = this.outputBuffer.poll(1000, TimeUnit.MILLISECONDS);
                this.writeMessage(outputStream, jsonObject);
            } catch (NullPointerException ignored) {

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                this.close();
            }
        }
    }

    private void writeMessage(OutputStream outStream, JSONObject jsonObject) throws NullPointerException, IOException {
        if (jsonObject == null) {
            throw new NullPointerException();
        }
        byte[] stringByte = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        byte[] headSize = ByteBuffer.allocate(4).putInt(stringByte.length).array();
        outStream.write(headSize);
        outStream.write(stringByte);
    }

    JSONObject get() {
        JSONObject jsonObject;
        try {
            jsonObject = this.inputBuffer.poll(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return null;
        }
        return jsonObject;
    }

    void put(JSONObject jsonObject) {
        if (!this.isConnected()) {
            return;
        }
        if (jsonObject != null) {
            try {
                this.outputBuffer.offer(jsonObject, 1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ClientConnection clientConnection = new ClientConnection();
        try {
            clientConnection.connect("192.168.0.1", 5050);
            clientConnection.verify("123456");
        } catch (IOException e) {
            e.printStackTrace();
            clientConnection.close();
        }

        if (clientConnection.isConnected()) {
            clientConnection.start();
            try {
                clientConnection.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                clientConnection.close();
            }
        }
    }

}
