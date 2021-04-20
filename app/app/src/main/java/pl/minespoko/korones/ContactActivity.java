package pl.minespoko.korones;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import pl.minespoko.korones.data.Callback;
import pl.minespoko.korones.data.DataManager;
import pl.minespoko.korones.storage.DatabaseHelper;
import pl.minespoko.korones.utils.Utils;

public class ContactActivity extends AppCompatActivity {

    private WebView mWebView;
    private AppCompatAutoCompleteTextView autoCompleteTextView;
    private ConstraintLayout assets;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        setupInfoText();
        assets = findViewById(R.id.contactAssets);
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchActivity = new Intent(ContactActivity.this, MainActivity.class);
                startActivity(launchActivity);
                finish();
            }
        });

        autoCompleteTextView = findViewById(R.id.contactRegionInputComplete);

        /*
        * Załadowanie do listy podpowiedzi już wcześniej wyszukanych jedostek
        * */
        updateAutoComplete();

        /*
         * Kiedy zostanie naciśnięty przycisk OK zostaje wyszukana dana jednostka sanitarna (funkcja search())
         * */
        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER || (actionId == EditorInfo.IME_ACTION_DONE))){
                    if(autoCompleteTextView.getText().toString().isEmpty()) return false;
                    TextView num = findViewById(R.id.contactNumberShow);
                    num.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
                    search(autoCompleteTextView.getText().toString());
                }
                return false;
            }
        });
        /*
        * Kiedy zostanie wybrana wcześniej wyszukiwana jednostka sanitarna to ładujemy ją z lokalnej bazy danych
        * */
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView num = findViewById(R.id.contactNumberShow);
                num.setVisibility(View.VISIBLE);
                try{
                    String value = DatabaseHelper.getValue(ContactActivity.this,"region_cached_"+adapter.getItem(position));
                    assert value != null;
                    String instytucja = value.split("/ulica")[0];
                    String adres = value.split("/ulica")[1].split("/numer")[0].split(";")[1];
                    String numer = value.split("/numer")[1].split(";")[1];
                    setText("Instytucja: "+instytucja+
                            "\nAdres: "+adres+
                            "\nNumer: ",numer);
                }catch (Exception ex) {
                    ex.printStackTrace();
                    Utils.log("error", "Błąd podczas wczytywania informacji (region_cached_X)");
                }
            }
        });
    }

    /*
    * Funkcja odpowiedzialna za ustawienie i sformatowanie tekstu informującego
    * */
    private void setupInfoText(){
        TextView infoText = findViewById(R.id.infoText);

        SpannableStringBuilder infoTextBuilder = new SpannableStringBuilder();

        SpannableString spannableString = new SpannableString(
                "Podejrzewasz u siebie koronawirusa?\n"
        );
        spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString.length(), 0);
        spannableString.setSpan(new RelativeSizeSpan(1.3f),0,spannableString.length(),0);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD),0,spannableString.length(),0);
        infoTextBuilder.append(spannableString);

        SpannableString spannableString1 = new SpannableString(
                "Sprawdź, czy masz objawy COVID-19.\n\n"
        );
        spannableString1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString1.length(), 0);
        infoTextBuilder.append(spannableString1);

        SpannableString spannableString2 = new SpannableString(
                "Jeśli miałeś kontakt z osobą zakażoną koronawirusem lub chorą, to natychmiast zadzwoń do stacji sanitarno-epidemiologicznej i powiadom o swojej sytuacji. Otrzymasz informację, jak masz dalej postępować.\n"
        );
        spannableString2.setSpan(new ForegroundColorSpan(0xFF0070FE), 0, spannableString2.length(), 0);
        infoTextBuilder.append(spannableString2);

        SpannableString spannableString3 = new SpannableString(
                "\nWpisz swoją miejscowość aby uzyskać adres oraz numer telefonu:"
        );
        spannableString3.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, spannableString3.length(), 0);
        infoTextBuilder.append(spannableString3);

        infoText.setText(infoTextBuilder, TextView.BufferType.SPANNABLE);
    }

    /*
    * Funkcja odpowiedzialna za załadowanie do listy podpowiedzi wcześniej wyszukiwanych regionów
    * */
    private void updateAutoComplete() {
        List<String> cFromDB = new ArrayList<>();

        final DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        try (SQLiteDatabase db = databaseHelper.getReadableDatabase()) {
            String query = String.format("SELECT * FROM %s WHERE %s LIKE ?",
                    DatabaseHelper.DB_TAB_korones
                    , DatabaseHelper.DB_COL_key);
            @SuppressLint("Recycle") Cursor cursor = db.rawQuery(query, new String[]{"region_cached_%"});
            if (cursor.moveToFirst()) {
                do {
                    cFromDB.add(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DB_COL_key)).substring("region_cached_".length()));
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.log("error", "Błąd podczas pobierania wartości z bazy (contact: numbers/region_cached_X)");
        }
        String[] complete = new String[cFromDB.size()];
        for (int i = 0; i<cFromDB.size(); i++){
            complete[i] = cFromDB.get(i);
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, complete);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.setAdapter(adapter);
    }

    /*
    * Otworzenie lokalnego okna przeglądarki z wyszukaniem ustawionym
    * na nieznaleziony punkt sanitarny
    * */
    @SuppressLint("SetJavaScriptEnabled")
    private void notFound(String name) throws UnsupportedEncodingException {
        CookieManager.getInstance().setAcceptCookie(true);
        mWebView = findViewById(R.id.contactMoreWebView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new XWebViewClient());
        mWebView.loadUrl("https://www.google.com/search?client=firefox-b-d&q=psse"+URLEncoder.encode(" "+name,"UTF-8"));
        mWebView.getSettings().setDefaultFontSize(12);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.setBackgroundColor(0x00000000);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        mWebView.getSettings().setAppCachePath(appCachePath);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
        assets.setVisibility(View.INVISIBLE);
        mWebView.setVisibility(View.VISIBLE);
    }

    /*
    * Funkcja odpowiedzialna za wysłanie i odebranie informacji od/do serwera.
    * Jeżeli serwer nie odnajdzie informacji to uruchomina zostanie lokalna przeglądarka
    * */
    private void search(final String name){
        @SuppressLint("HandlerLeak") final Handler notHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                try{
                    if(msg.getData().containsKey("found")){
                        setText(msg.getData().getString("found"),msg.getData().getString("number"));
                        updateAutoComplete();
                        return;
                    }
                    setText("Nie znaleziono informacji",null);
                    notFound(msg.getData().getString("search"));
                    Snackbar.make(findViewById(R.id.contactMoreWebView), "Serwer nie znalazł informacji, może tobie się uda", Snackbar.LENGTH_LONG).show();
                }catch (Exception ex){
                    ex.printStackTrace();
                    Utils.log("error","Błąd podczas wyszukiwania miasta (notFound+URL)");
                }
            }
        };
        DataManager.getDataSocketConnection().putCallback("/nazwa", new Callback() {
            @Override
            public void dataInputBack(String key, String value) {
                if(value == null || value.equals("null")){
                    Message message = notHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString("search",name);
                    message.setData(bundle);
                    notHandler.sendMessage(message);
                }else{
                    try{
                        String instytucja = value.split("/ulica")[0];
                        String adres = value.split("/ulica")[1].split("/numer")[0].split(";")[1];
                        String numer = value.split("/numer")[1].split(";")[1];
                        Message message = notHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("found","Instytucja: "+instytucja+
                                "\nAdres: "+adres+
                                "\nNumer: ");
                        bundle.putString("number",numer);
                        message.setData(bundle);
                        notHandler.sendMessage(message);
                        DatabaseHelper.setValue(ContactActivity.this,"region_cached_"+name,value);
                    }catch (Exception ex) {
                        ex.printStackTrace();
                        Utils.log("error", "Błąd podczas odbierania informacji (splitter)");
                    }
                }
            }
        });
        DataManager.getDataSocketConnection().putCallback("stateupdate", new Callback() {
            @Override
            public void dataInputBack(String key, String value) {
                if(value.equals("OPEN")){
                    DataManager.getDataSocketConnection().send("/numer;"+name);
                    DataManager.getDataSocketConnection().removeCallback(key,this);
                }
            }
        });
    }

    /*
    * Funkcja odpowiedzialna za odpowiednie ustawienie wyświetlanego tekstu.
    * Jeśli zostanie kliknięty, a numer będzie podany to przekieruje nas do wybierania numeru
    * */
    private void setText(String text, final String event){
        final TextView numberText = findViewById(R.id.contactNumberShow);

        SpannableStringBuilder numberTextBuilder = new SpannableStringBuilder();
        SpannableString spannableString = new SpannableString(text);
        numberTextBuilder.append(spannableString);

        if(event != null){
            SpannableString spannableString1 = new SpannableString(event);
            spannableString1.setSpan(new ForegroundColorSpan(Color.BLUE), 0, spannableString1.length(), 0);
            numberTextBuilder.append(spannableString1);
        }

        numberText.setText(numberTextBuilder, TextView.BufferType.SPANNABLE);
        if(event != null){
            numberText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:"+event));
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if(mWebView != null && mWebView.getVisibility() == View.VISIBLE){
            mWebView.setVisibility(View.INVISIBLE);
            assets.setVisibility(View.VISIBLE);
            return;
        }
        Intent launchActivity = new Intent(this, MainActivity.class);
        startActivity(launchActivity);
        finish();
    }

    private static class XWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webview, String url){
            webview.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mWebView != null && (keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack() && mWebView.getVisibility() == View.VISIBLE) {
            if(mWebView.canGoBack()){
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
