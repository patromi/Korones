package pl.minespoko.korones;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pl.minespoko.korones.data.Callback;
import pl.minespoko.korones.data.DataManager;
import pl.minespoko.korones.storage.DatabaseHelper;
import pl.minespoko.korones.utils.Utils;

public class StatsActivity extends AppCompatActivity {

    /*Klasa NumberFormat służąca do formatowania liczb z odebranych danych statystycznych*/
    private NumberFormat numberFormat = NumberFormat.getInstance();{
        numberFormat.setGroupingUsed(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final AppCompatAutoCompleteTextView autoCompleteTextView = findViewById(R.id.countryInputComplete);
        List<String> cFromDB = new ArrayList<>();
        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        try (SQLiteDatabase db = databaseHelper.getReadableDatabase()) {
            String query = String.format("SELECT * FROM %s", DatabaseHelper.DB_TAB_countries);
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{});
            if (cursor.moveToFirst()) {
                do {
                    cFromDB.add(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DB_COL_country_PL)));
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.log("error", "Błąd podczas pobierania wartości z bazy (stats: countries)");
        }
        String[] complete = new String[cFromDB.size()];
        for (int i = 0; i<cFromDB.size(); i++){
            complete[i] = cFromDB.get(i);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, complete);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(adapter);

        /*
         * Kiedy zostanie naciśnięty przycisk OK wyświetlona zostanie informacja o tym,
         * że trzeba wybrać kraj z listy
         * */
        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                autoCompleteTextView.clearFocus();
                Snackbar.make(autoCompleteTextView, "Musisz wybrać kraj z listy", Snackbar.LENGTH_LONG).show();
                return true;
            }
        });
        /*
        * Kiedy zostanie wybrany kraj z listy zacznie się ładowanie danych statystycznych
        * */
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
                LinearLayout linearLayout = findViewById(R.id.stat_layoutconetnt);
                linearLayout.setVisibility(View.VISIBLE);
                final String selected = adapter.getItem(position);
                updateTextViev(R.id.stats_text_country_val, selected);
                updateTextViev(R.id.stats_text_allcases_val, "...");
                updateTextViev(R.id.stats_text_newcases_val, "...");
                updateTextViev(R.id.stats_text_alldeads_val, "...");
                updateTextViev(R.id.stats_text_newdeath_val, "...");
                updateTextViev(R.id.stats_text_allrecived_val, "...");
                updateTextViev(R.id.stats_text_activecases_val, "...");
                updateTextViev(R.id.stats_text_critical_val, "...");
                updateTextViev(R.id.stats_text_cases1m_val, "...");
                updateTextViev(R.id.stats_text_death1m_val, "...");
                updateTextViev(R.id.stats_text_alltest_val, "...");
                updateTextViev(R.id.stats_text_test1m_val, "...");
                updateTextViev(R.id.stats_text_population_val, "...");
                updateTextViev(R.id.stats_text_continent_val, "...");
                String allcases = DatabaseHelper.getValue(StatsActivity.this, selected + ";allcases");
                if (allcases != null) {
                    /*
                    * Jeśli mamy jakieś dane w lokalnej bazie to ładujemy je
                    * */
                    loadFromDatabase(selected);
                }
                @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (Objects.requireNonNull(msg.getData().getString("key"))) {
                            case "allcases":
                                updateTextViev(R.id.stats_text_allcases_val, msg.getData().getString("value"));
                                break;
                            case "newcases":
                                updateTextViev(R.id.stats_text_newcases_val, msg.getData().getString("value"));
                                break;
                            case "alldeads":
                                updateTextViev(R.id.stats_text_alldeads_val, msg.getData().getString("value"));
                                break;
                            case "newdeath":
                                updateTextViev(R.id.stats_text_newdeath_val, msg.getData().getString("value"));
                                break;
                            case "allrecived":
                                updateTextViev(R.id.stats_text_allrecived_val, msg.getData().getString("value"));
                                break;
                            case "activecases":
                                updateTextViev(R.id.stats_text_activecases_val, msg.getData().getString("value"));
                                break;
                            case "critical":
                                updateTextViev(R.id.stats_text_critical_val, msg.getData().getString("value"));
                                break;
                            case "cases/1m":
                                updateTextViev(R.id.stats_text_cases1m_val, msg.getData().getString("value"));
                                break;
                            case "death/1m":
                                updateTextViev(R.id.stats_text_death1m_val, msg.getData().getString("value"));
                                break;
                            case "alltest":
                                updateTextViev(R.id.stats_text_alltest_val, msg.getData().getString("value"));
                                break;
                            case "test1/m":
                                updateTextViev(R.id.stats_text_test1m_val, msg.getData().getString("value"));
                                break;
                            case "population":
                                updateTextViev(R.id.stats_text_population_val, msg.getData().getString("value"));
                                break;
                            case "continent":
                                updateTextViev(R.id.stats_text_continent_val, msg.getData().getString("value"));
                                break;
                            default:
                                Utils.log("missing", "Missing iplementation of '" + msg.getData().getString("key") + "'");
                                break;
                        }
                    }
                };
                DataManager.getDataSocketConnection().putCallback("/info", new Callback() {
                    @Override
                    public void dataInputBack(String key, String value) {
                        value = value.trim();
                        String xx = value.split(";", 4)[2];
                        String dbVal = value.split(";", 4)[3];
                        DatabaseHelper.setValue(StatsActivity.this, selected + ";" + xx, dbVal);
                        Message msg = handler.obtainMessage();
                        Bundle bun = new Bundle();
                        bun.putString("key", xx);
                        bun.putString("value", dbVal);
                        msg.setData(bun);
                        handler.sendMessage(msg);
                    }
                });
                DataManager.getDataSocketConnection().putCallback("stateupdate", new Callback() {
                    @Override
                    public void dataInputBack(String key, String value) {
                        if (value.equals("OPEN")) {
                            DataManager.getDataSocketConnection().send("/info;" + DatabaseHelper.getCountryRealName(StatsActivity.this, selected));
                            DataManager.getDataSocketConnection().removeCallback(key, this);
                        }
                    }
                });
            }
        });
    }

    /*
    * Funkcja odpowiedzialna za formatowanie i ustawianie danych wartości statystycznych
    * */
    private void updateTextViev(int id, String text){
        if(text == null || text.trim().isEmpty() || text.equals("null")){
            text = "brak danych";
        }
        try{
            int i = Integer.parseInt(text);
            text = numberFormat.format(i);
        }catch (Exception ignored){ }
        ((TextView) findViewById(id)).setText(text);
    }

    /*
    * Funkcja odpowiedzialna za ładowanie informacji o dany karju z lokalnej bazy danych
    * */
    private void loadFromDatabase(String country){
        updateTextViev(R.id.stats_text_allcases_val,DatabaseHelper.getValue(this,country+";allcases"));
        updateTextViev(R.id.stats_text_newcases_val,DatabaseHelper.getValue(this,country+";newcases"));
        updateTextViev(R.id.stats_text_alldeads_val,DatabaseHelper.getValue(this,country+";alldeads"));
        updateTextViev(R.id.stats_text_newdeath_val,DatabaseHelper.getValue(this,country+";newdeath"));
        updateTextViev(R.id.stats_text_allrecived_val,DatabaseHelper.getValue(this,country+";allrecived"));
        updateTextViev(R.id.stats_text_activecases_val,DatabaseHelper.getValue(this,country+";activecases"));
        updateTextViev(R.id.stats_text_critical_val,DatabaseHelper.getValue(this,country+";critical"));
        updateTextViev(R.id.stats_text_cases1m_val,DatabaseHelper.getValue(this,country+";cases/1m"));
        updateTextViev(R.id.stats_text_death1m_val,DatabaseHelper.getValue(this,country+";death/1m"));
        updateTextViev(R.id.stats_text_alltest_val,DatabaseHelper.getValue(this,country+";alltest"));
        updateTextViev(R.id.stats_text_test1m_val,DatabaseHelper.getValue(this,country+";test1/m"));
        updateTextViev(R.id.stats_text_population_val,DatabaseHelper.getValue(this,country+";population"));
        updateTextViev(R.id.stats_text_continent_val,DatabaseHelper.getValue(this,country+";continent"));
    }

    @Override
    public void onBackPressed() {
        Intent launchActivity = new Intent(this, MainActivity.class);
        startActivity(launchActivity);
        finish();
    }
}
