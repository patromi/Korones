package pl.minespoko.korones;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pl.minespoko.korones.data.Callback;
import pl.minespoko.korones.data.DataManager;
import pl.minespoko.korones.storage.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    /*
    * Funkcja odpowiedzialna za tzw. setup aplikacji.
    * Odpowiednio przypisuje akcje do przycisków w menu głównym.
    * Ładuje dane iwywołuje funkcje odpowiedzialną za utworzenie połączenia z serwerem
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        * Wywołanie funkcji odpowiedzialnych za połączenie
        * */
        DataManager.init();
        DataManager.loadNeeded(this);

        /*
         * Ustawienie, że po kliknięciu przycisku autorów przenisie nas do strony z autorami
         * */
        Button authors = findViewById(R.id.authorsButton);
        authors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchActivity = new Intent(MainActivity.this, AuthorsActivity.class);
                startActivity(launchActivity);
                finish();
            }
        });
        /*
         * Ustawienie, że po kliknięciu statystyk przenisie nas do strony ze statystykami
         * */
        final ImageView buttonStats = findViewById(R.id.main_button_stats);
        buttonStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchActivity = new Intent(MainActivity.this, StatsActivity.class);
                startActivity(launchActivity);
                finish();
            }
        });
        /*
         * Ustawienie, że po kliknięciu kontaktu przenisie nas do strony z wyszukiwaniem kontaktu
         * */
        final ImageView buttonContact = findViewById(R.id.main_button_contact);
        buttonContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchActivity = new Intent(MainActivity.this, ContactActivity.class);
                startActivity(launchActivity);
                finish();
            }
        });
        /*
         * Ustawienie, że po kliknięciu obostrzeń przenisie nas do strony z obostrzeniami
         * */
        final ImageView buttonRestrictions = findViewById(R.id.main_button_restrictions);
        buttonRestrictions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchActivity = new Intent(MainActivity.this, RestrictionsActivity.class);
                startActivity(launchActivity);
                finish();
            }
        });
        /*
         * Ustawienie, że po kliknięciu objawów przenisie nas do strony z objawami
         * */
        final ImageView buttonSymptoms = findViewById(R.id.main_button_symptoms);
        buttonSymptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchActivity = new Intent(MainActivity.this, SymptomsActivity.class);
                startActivity(launchActivity);
                finish();
            }
        });

        /*
         * Wywołanie funkcji dot. załadowania wszystkich przypadków
         * */
        loadAllCasesMain();
    }


    /*
     * Funkcja odpowiedzialna za załadowanie danych statystycznych na temat wszystkich zakażeń
     * */
    private void loadAllCasesMain(){
        final TextView textView = findViewById(R.id.home_text_allcases);
        final String a = getString(R.string.cases_on_world);
        final String cases = DatabaseHelper.getValue(this,"allcases");
        if(cases != null){
            textView.setText(String.format(a, cases));
        }

        /*
         * Handler odpowiednio formatujący i ustawiający tekst
         * */
        @SuppressLint("HandlerLeak") final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                textView.setText(String.format(a,msg.getData().getString("text")));
            }
        };
        /*
         * Po otrzymaniu informacji wysłanie wiadomości do handlera
         * */
        DataManager.getDataSocketConnection().putCallback("/zarazenia", new Callback() {
            @Override
            public void dataInputBack(String key, String value) {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("text",value);
                msg.setData(bundle);
                handler.sendMessage(msg);
                DatabaseHelper.setValue(MainActivity.this,"allcases",value);
            }
        });
        /*
         * Jeżeli połączenie jest otwarte to wysyła zapytanie o objawy
         * */
        DataManager.getDataSocketConnection().putCallback("stateupdate", new Callback() {
            @Override
            public void dataInputBack(String key, String value) {
                if(value.equals("OPEN")){
                    DataManager.getDataSocketConnection().send("/zarazenia");
                    DataManager.getDataSocketConnection().removeCallback(key,this);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
