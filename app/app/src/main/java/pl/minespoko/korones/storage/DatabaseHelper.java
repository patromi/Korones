package pl.minespoko.korones.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import pl.minespoko.korones.utils.Utils;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    private static final String DATABASE_NAME = "db_korones";
    public static final String DB_TAB_korones = "korones";
    public static final String DB_COL_key = "key";
    private static final String DB_COL_value = "value";

    public static final String DB_TAB_countries = "countries";
    private static final String DB_COL_country = "country";
    public static final String DB_COL_country_PL = "countrypl";

    /*Statyczna instancja tej klasy*/
    private static DatabaseHelper databaseHelper;

    /*
    * Funkcja pozwalająca na odwołanie się do instancji tej klasy lub utworzenie jej
    * */
    public static synchronized DatabaseHelper getInstance(Context context){
        if (databaseHelper == null){
            databaseHelper = new DatabaseHelper(context.getApplicationContext());
        }
        return databaseHelper;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
     * Funkcja odpowiedzialna za utworzenie tabel podczas utworzenia bazy danych
     * */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(String.format("CREATE TABLE %s (%s TEXT NOT NULL PRIMARY KEY, %s TEXT)",
                DB_TAB_korones,DB_COL_key,DB_COL_value));
        sqLiteDatabase.execSQL(String.format("CREATE TABLE %s (%s TEXT NOT NULL PRIMARY KEY, %s TEXT)",
                DB_TAB_countries,DB_COL_country,DB_COL_country_PL));
    }

    /*
     * Funkcja odpowiedzialna za dodanie do listy państw danej asocjacji państwa,
     * jako argument "country" podaje się ciąg znaków w postaci "angielska nazwa;polska nazwa"
     * */
    public static synchronized void insertCountry(Context context, String country){
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        try (SQLiteDatabase db = databaseHelper.getWritableDatabase()) {
            db.execSQL(String.format("INSERT OR IGNORE INTO %s (%s,%s) VALUES (?,?);",
                    DB_TAB_countries, DB_COL_country, DB_COL_country_PL),
                    new Object[]{country.split(";")[0], country.split(";")[1]});
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log("error", "Blad podczas zapisywania do bazy (country: " + country + ")");
        }
    }

    /*
     * Funkcja odpowiedzialna za zwrócenie angielskiej nazwy państwa
     * */
    public static synchronized String getCountryRealName(Context context, String country){
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        try (SQLiteDatabase db = databaseHelper.getReadableDatabase()) {
            String query = String.format("SELECT * FROM %s WHERE %s=?",
                    DatabaseHelper.DB_TAB_countries, DB_COL_country_PL);
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{country});
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(DB_COL_country));
            } else {
                return "nic_blad";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.log("error", "Błąd podczas pobierania wartości z bazy (countries - rename)");
        }
        return "nic_2_blad";
    }

    /*
     * Funkcja odpowiedzialna za pobranie ustawionej po kluczu wartości z bazy
     * */
    public static synchronized String getValue(Context context, String key){
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        try (SQLiteDatabase db = databaseHelper.getReadableDatabase()) {
            String query = String.format("SELECT * FROM %s WHERE %s=?",
                    DatabaseHelper.DB_TAB_korones, DatabaseHelper.DB_COL_key);
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{key});
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(DatabaseHelper.DB_COL_value));
            }
        } catch (Exception x) {
            x.printStackTrace();
            Utils.log("error", "Błąd podczas pobierania wartości z bazy (" + key + ")");
        }
        return null;
    }
    /*
     * Funkcja odpowiedzialna za włożenie lub zamianę danej wartości kojarzonej po kluczu
     * */
    public static synchronized void setValue(Context context, String key, String value){
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        try (SQLiteDatabase db = databaseHelper.getWritableDatabase()) {
            db.execSQL(String.format("INSERT OR REPLACE INTO %s (%s,%s) VALUES (?,?);",
                    DB_TAB_korones, DB_COL_key, DB_COL_value),
                    new Object[]{key, value});
        } catch (Exception e) {
            e.printStackTrace();
            Utils.log("error", "Blad podczas zapisywania do bazy (" + key + "," + value + ")");
        }
    }

    /*
     * Funkcja odpowiedzialna za usunięcie tabel w przypadku zmiany wersji bazy
     * np. zmiana w strukturze starych tabel
     * */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DB_TAB_korones);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DB_TAB_countries);
        onCreate(sqLiteDatabase);
    }
}