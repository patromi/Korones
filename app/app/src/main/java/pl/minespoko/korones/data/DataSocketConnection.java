package pl.minespoko.korones.data;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.minespoko.korones.utils.Utils;

public class DataSocketConnection extends Thread {

    /*Mapa czekaczy*/
    private final Map<String,List<Callback>> callbacks;
    /*Mapa czekaczy, które należy usunąć*/
    private final Map<String,List<Callback>> callbackstoremove;
    private String ip;
    private int port;
    /*Aktualny stan połączenia*/
    private ConnectionState state;
    Socket socket;
    /*Strumień wyjścia - wysyłanie po nim zapytań*/
    private PrintWriter printWriter;
    /*Ostatni czas polecenia "/ping" */
    private long lastSentPing = 0;
    /*Ostatni czas odebrania jakiejkolwiek informacji*/
    private long lastRecivedPacket;
    /*"Basen" wątków służacy do wysyłania zapytań*/
    private ExecutorService threadpool = Executors.newCachedThreadPool();

    @SuppressLint("HandlerLeak")
    DataSocketConnection(String ip, int port){
        this.ip = ip;
        this.port = port;
        this.callbacks = new HashMap<>();
        this.callbackstoremove = new HashMap<>();
        state = ConnectionState.OPENING;
        putCallback("/pong", new Callback() {
            @Override
            public void dataInputBack(String key, String value) {
                try {
                    long time = Long.parseLong(value);
                    time = System.currentTimeMillis() - time;
                    lastSentPing = 0;
                    Utils.log("pingtime", String.valueOf(time));
                }catch (Exception ex){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
    }

    /*
    * Funkcja pozwalająca na dodanie czekacza,
    * jeżeli jego wartość to "stateupdate" to od razu informujemy
    * go o stanie połączenia
    */
    public void putCallback(String key, final Callback callback){
        if(!callbacks.containsKey(key)) callbacks.put(key,new ArrayList<Callback>());
        Objects.requireNonNull(callbacks.get(key)).add(callback);
        if(key.equals("stateupdate")){
            threadpool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        callback.dataInputBack("stateupdate", state.name());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Utils.log("error", "Activate callback error with key [MASTER]: stateupdate");
                    }
                }
            });
        }
    }

    /*
    * Funkcja pozwalająca na usunięcie czekacza
    * */
    public void removeCallback(String key,Callback callback) {
        if (!callbackstoremove.containsKey(key)){
            callbackstoremove.put(key, new ArrayList<Callback>());
        }
        Objects.requireNonNull(callbackstoremove.get(key)).add(callback);
    }

    /*
    * Funkcja pozwalająca na wysyłanie zapytań.
    * Zapytania wysyłane są w oddzielnych wątkach
    */
    public void send(final String cmd) {
        try {
            Utils.log("cmdsend",cmd);
            threadpool.submit(new Runnable() {
                @Override
                public void run() {
                    if (cmd.startsWith("/info;")) {
                        printWriter.write(cmd);
                        printWriter.flush();
                        printWriter.println();
                    } else {
                        printWriter.println(cmd);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log("error","Wystąpił błąd podczas wysyłania zapytania do serwera (XSTS)");
        }
    }

    /*
    * Funkcja, która aktualizuje stan połączenia
    */
    private void stateUpdate(final ConnectionState state){
        this.state = state;
        Utils.log("cstate", state.name());
        threadpool.submit(new Runnable() {
            @Override
            public void run() {
                synchronized (callbacks) {
                    if(callbacks.containsKey("stateupdate")){
                        for(Callback callback : Objects.requireNonNull(callbacks.get("stateupdate"))){
                            try {
                                callback.dataInputBack("stateupdate",state.name());
                            }catch (Exception ex){
                                ex.printStackTrace();
                                Utils.log("error","Activate callback error with key [MASTER]: stateupdate");
                            }
                        }
                    }
                }
            }
        });
    }

    /*
    * Funkcja wywoływana przy uruchomieniu wątku, inicjalizuje połączenie
    * oraz niezbędne do jego działania rzeczy
    * */
    @Override
    public void run() {
        try {
            Utils.log("cinfo","Tworzenie połączenia");
            stateUpdate(ConnectionState.OPENING);
            socket = new Socket(ip, port);
            lastRecivedPacket = System.currentTimeMillis();
            printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),true);
            new ReciveData(socket.getInputStream()).start();
            stateUpdate(ConnectionState.OPEN);
            Utils.log("cinfo","Pętla ping");
            while (socket.isConnected()){
                if(lastSentPing == 0){
                    lastSentPing = System.currentTimeMillis();
                    send("/ping;"+lastSentPing);
                }
                if(System.currentTimeMillis()-lastSentPing > 31000 && System.currentTimeMillis()-lastRecivedPacket > 20000){
                    stateUpdate(ConnectionState.TIMEOUT);
                    break;
                }
                try{
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Utils.log("error","Wystąpił błąd podczas utrzymywania połączenia z serwerem (XCSS)");
                    break;
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.log("error","Wystąpił błąd podczas łączenia z serwerem (XCTS)");
        }
        stateUpdate(ConnectionState.CLOSED);
    }

    /*
    * Prywatna klasa odpowiedzialna za odbieranie danych,
    * pozwala na asynchroniczne odbieranie danych dzięki nowemu wątkowi
    * */
    private class ReciveData extends Thread {

        private InputStream inputStream;

        ReciveData(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        /*Funkcja próbująca odczytywać dane przychodzące i rozsyłać je po czekaczach,
        * również nimi zarządza
        * */
        private void tryRead() throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String ln;
            while ((ln = br.readLine()) != null) {
                lastRecivedPacket = System.currentTimeMillis();
                Utils.log("recive",ln);
                final String[] keys = ln.split(";");
                if(ln.length() < 2) continue;
                try {
                    if (ln.contains("/info;") && !ln.startsWith("/info;")) {
                        synchronized (callbacks) {
                            synchronized (callbackstoremove) {
                                for (Map.Entry<String, List<Callback>> ena : callbackstoremove.entrySet()) {
                                    for (Callback call : ena.getValue()) {
                                        if (callbacks.containsKey(ena.getKey())) {
                                            Objects.requireNonNull(callbacks.get(ena.getKey())).remove(call);
                                        }
                                    }
                                }
                                callbackstoremove.clear();
                                if (callbacks.containsKey(keys[0].split("/info;")[0])) {
                                    List<Callback> callbacks = DataSocketConnection.this.callbacks.get(keys[0].split("/info;")[0]);
                                    assert callbacks != null;
                                    for (Callback callback : callbacks) {
                                        try {
                                            callback.dataInputBack(keys[0].split("/info;")[0], ln.substring((keys[0].split("/info;")[0]).length() + 1));
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            Utils.log("error", "Activate callback error with key: " + keys[0].split("/info;")[0]);
                                        }
                                    }
                                }
                                if (callbacks.containsKey("/info;" + keys[0].split("/info;")[1])) {
                                    List<Callback> callbacks = DataSocketConnection.this.callbacks.get(keys[0].split("/info;")[1]);
                                    assert callbacks != null;
                                    for (Callback callback : callbacks) {
                                        try {
                                            callback.dataInputBack("/info;" + keys[0].split("/info;")[1], ln.substring(("/info;" + keys[0].split("/info;")[1]).length() + 1));
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                            Utils.log("error", "Activate callback error with key: " + "/info;" + keys[0].split("/info;")[1]);
                                        }
                                    }
                                }
                            }
                        }
                        continue;
                    }
                    synchronized (callbacks) {
                        if (callbacks.containsKey(keys[0])) {
                            for (Map.Entry<String, List<Callback>> en : callbackstoremove.entrySet()) {
                                for(Callback call : en.getValue()){
                                    if (callbacks.containsKey(en.getKey())) {
                                        Objects.requireNonNull(callbacks.get(en.getKey())).remove(call);
                                    }
                                }
                            }
                            callbackstoremove.clear();
                            List<Callback> callbacks = DataSocketConnection.this.callbacks.get(keys[0]);
                            try {
                                assert callbacks != null;
                                for (Callback callback : callbacks) {
                                    try {
                                        if(!(ln.length() > (keys[0].length() + 1))) continue;
                                        callback.dataInputBack(keys[0], ln.substring(keys[0].length() + 1));
                                    } catch (Exception ex) {
                                        if (!(ex instanceof ConcurrentModificationException)) {
                                            ex.printStackTrace();
                                            Utils.log("error", "Activate callback error with key: " + keys[0]);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                if (!(ex instanceof ConcurrentModificationException)) {
                                    ex.printStackTrace();
                                    Utils.log("error", "Activate callback error with key2: " + keys[0]);
                                }
                            }
                        }
                    }
                }catch (Exception ignored){

                }
            }
        }

        @Override
        public void run() {
            try {
                tryRead();
            } catch (IOException e) {
                e.printStackTrace();
                Utils.log("error","Wystąpił błąd podczas odczytywania danych");
                try {
                    if(socket.isConnected()) socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                interrupt();
            }
        }
    }

    /*
    * Publiczny enum definiujący stany połaczenia
    * */
    public enum ConnectionState {
        OPEN,
        CLOSED,
        OPENING,
        TIMEOUT
    }

}