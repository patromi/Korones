package pl.minespoko.korones;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import pl.minespoko.korones.data.Callback;
import pl.minespoko.korones.data.DataManager;

public class RestrictionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restrictions);
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final TextView viewTextRestrictions = findViewById(R.id.restrictionText);
        viewTextRestrictions.setMovementMethod(LinkMovementMethod.getInstance());
        /*
         * Handler odpowiedzialny za odpowiednie formatowanie odebranego
         * kodu html zgodnie z daną wersją systemu android
         * */
        @SuppressLint("HandlerLeak")
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    viewTextRestrictions.append(Html.fromHtml(msg.getData().getString("text"),0));
                }else{
                    viewTextRestrictions.append(Html.fromHtml(msg.getData().getString("text")));
                }
            }
        };
        /*
         * Po otrzymaniu informacji wysłanie wiadomości do handlera
         * */
        DataManager.getDataSocketConnection().putCallback("/obo", new Callback() {
            @Override
            public void dataInputBack(String key, String value) {
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString("text",value);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        });
        /*
         * Jeżeli połączenie jest otwarte to wysyła zapytanie o objawy
         * */
        DataManager.getDataSocketConnection().putCallback("stateupdate", new Callback() {
            @Override
            public void dataInputBack(String key, String value) {
                if(value.equals("OPEN")){
                    DataManager.getDataSocketConnection().send("/obostrzenia");
                    DataManager.getDataSocketConnection().removeCallback(key,this);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent launchActivity = new Intent(this, MainActivity.class);
        startActivity(launchActivity);
        finish();
    }
}
