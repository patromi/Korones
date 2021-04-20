package pl.minespoko.korones.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import pl.minespoko.korones.storage.DatabaseHelper;
import pl.minespoko.korones.utils.Utils;

public class DataManager {

    private static DataSocketConnection dataSocketConnection;

    /*
    * Ładowanie wymaganych danych do działania aplikacji
    * tj. nazyw państw - polskie i angielskie, polskie wyświetlane użytkownikowi,
    * a angielskie używane do komunikacji z serwerem.
    * Lista państw przechowywana jest w lokalnej bazie danych SQLite w osobnej tabeli
    * */
    public static void loadNeeded(final Context context){
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        try (SQLiteDatabase db = databaseHelper.getReadableDatabase()) {
            String query = String.format("SELECT COUNT(*) FROM %s", DatabaseHelper.DB_TAB_countries);
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{});
            if (cursor.moveToFirst()) {
                /*
                 * Jeżeli mamy już załadowaną listę państw to nie należy robić tego ponownie,
                 * funkcja zakończy swoje działanie jeśli lista ma 200 lub więcej wartości
                 * */
                if (cursor.getInt(cursor.getColumnIndex("COUNT(*)")) >= 200) {
                    return;
                }
            }
            /*
             * Dodajemy callback (czekacz) wrażliwy na informację zwrotną zaczynającą się od "/kraj"
             * Jeśli coś otrzymamy (tutaj nazwę państwa) to włoży ją do bazy danych
             * */
            DataManager.getDataSocketConnection().putCallback("/kraj", new Callback() {
                @Override
                public void dataInputBack(String key, String value) {
                    DatabaseHelper.insertCountry(context, value);
                }
            });
            /*
             * Callback (czekacz) ustawiony na "stateupdate" pozwala sprawdzić stan połączenia,
             * jeżeli jest w stanie "OPEN" (otwarte) to możemy wysłać zapytanie "/kraj", aby
             * poprosić o listę państw
             * */
            DataManager.getDataSocketConnection().putCallback("stateupdate", new Callback() {
                @Override
                public void dataInputBack(String key, String value) {
                    if (value.equals("OPEN")) {
                        DataManager.getDataSocketConnection().send("/kraj");
                        DataManager.getDataSocketConnection().removeCallback(key, this);
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.log("error", "Błąd podczas pobierania wartości z bazy (loader: countries)");
        }
    }

    /*
    * Funkcja init() jest odpowiedzialna za utworzenie połączenia z serwerem
    * i ponowne jego otwarcie w przypadku kiedy zostanie uracone
    * */
    public static void init() {
        if (isConnected()) {
            return;
        }
        /*
        * Tworzenie instancji klasy odpowiedzialnej za połączenie, wysyłanie oraz nasłuchiwanie informacji
        * */
        String SERVER_IP = "34.107.106.99";
        final DataSocketConnection d = new DataSocketConnection(SERVER_IP, 20001);
        /*
         * Callback (czekacz) ustawiony na "stateupdate" pozwala sprawdzić stan połączenia,
         * jeżeli jest w stanie "CLOSED" (zamknięte) to czekamy 10 sekund i ponownie otwieramy połączenie
         * */
        d.putCallback("stateupdate", new Callback() {
            @Override
            public void dataInputBack(String key, String value) {
                if (value.equals("CLOSED")) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dataSocketConnection = null;
                    DataManager.init();
                    d.interrupt();
                }else if(value.equals("OPEN")){
                    dataSocketConnection = d;
                }
            }
        });
        dataSocketConnection = d;
        /*
        * Uruchomienie wątku z wcześniejszymi definicjami.
        * Rozpoczyna proces łączenia - wywołanie funkcji run() w nowym wątku
        * */
        d.start();
    }

    public static DataSocketConnection getDataSocketConnection() {
        return dataSocketConnection;
    }

    private static boolean isConnected() {
        return dataSocketConnection != null && dataSocketConnection.socket.isConnected();
    }

}
