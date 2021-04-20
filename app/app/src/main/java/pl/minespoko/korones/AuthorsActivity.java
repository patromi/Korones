package pl.minespoko.korones;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AuthorsActivity extends AppCompatActivity {

    /*
     * Funkcja odpowiedzialna za odpowiednie sformatowanie i umieszczenie tekstu
     * */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authors);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final TextView viewTextAuthors = findViewById(R.id.authors_text);
        final SpannableStringBuilder viewTextAuthorsBuilder = new SpannableStringBuilder();

        SpannableString spannableString = new SpannableString("Autorami aplikacji \"Korones\" na 'Kaliski Konkurs Informatyczny' są uczniowie klasy 1 technikum z Zespołu Szkół Technicznych w Ostrowie Wielkopolskim:");
        spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString.length(), 0);
        viewTextAuthorsBuilder.append(spannableString);

        SpannableString spannableString1 = new SpannableString("\n- Patryk Migaj");
        spannableString1.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString1.length(), 0);
        spannableString1.setSpan(new StyleSpan(Typeface.ITALIC),0,spannableString1.length(),0);
        viewTextAuthorsBuilder.append(spannableString1);

        SpannableString spannableString2 = new SpannableString("\n- Marcin Kryjom");
        spannableString2.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString2.length(), 0);
        spannableString2.setSpan(new StyleSpan(Typeface.ITALIC),0,spannableString2.length(),0);
        viewTextAuthorsBuilder.append(spannableString2);

        SpannableString spannableString3 = new SpannableString("\n\nPrawa Autorskie");
        spannableString3.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString3.length(), 0);
        spannableString3.setSpan(new AlignmentSpan() {
            @Override
            public Layout.Alignment getAlignment() {
                return Layout.Alignment.ALIGN_CENTER;
            }
        }, 0, spannableString3.length(), 0);
        viewTextAuthorsBuilder.append(spannableString3);

        SpannableString spannableString4 = new SpannableString("\n\nJako zespół staraliśmy się w każdym calu aplikacji wykorzystać tylko i wyłącznie nasze umiejętności. W każdym razie podajemy źródła wszystkich materiałów pomocniczych, które posłużyły nam do stworzenia aplikacji.");
        spannableString4.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString4.length(), 0);
        spannableString4.setSpan(new RelativeSizeSpan(0.8f),0,spannableString4.length(),0);
        viewTextAuthorsBuilder.append(spannableString4);

        viewTextAuthors.setText(viewTextAuthorsBuilder, TextView.BufferType.SPANNABLE);

        final TextView viewTextLinks = findViewById(R.id.linksCpr);
        viewTextLinks.setMovementMethod(LinkMovementMethod.getInstance());

        final SpannableStringBuilder viewTextLinksBuilder = new SpannableStringBuilder();

        SpannableString spannableString5 = new SpannableString(
                "\nStatystyki:" +
                    "\n- ");
        spannableString5.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spannableString5.length(), 0);
        spannableString5.setSpan(new RelativeSizeSpan(0.6f),0,spannableString5.length(),0);
        spannableString5.setSpan(new URLSpan(""),0,1,0);
        viewTextLinksBuilder.append(spannableString5);

        SpannableString spannableString6 = new SpannableString(
                "https://www.worldometers.info/coronavirus/");
        spannableString6.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spannableString6.length(), 0);
        spannableString6.setSpan(new RelativeSizeSpan(0.6f),0,spannableString6.length(),0);
        spannableString6.setSpan(new URLSpan("https://www.worldometers.info/coronavirus/"),0,spannableString6.length(),0);
        viewTextLinksBuilder.append(spannableString6);

        SpannableString spannableString7 = new SpannableString("\nObostrzenia:" +
                    "\n- ");
        spannableString7.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spannableString7.length(), 0);
        spannableString7.setSpan(new RelativeSizeSpan(0.6f),0,spannableString7.length(),0);
        viewTextLinksBuilder.append(spannableString7);

        SpannableString spannableString8 = new SpannableString(
                "https://www.gov.pl/web/koronawirus/3etap/");
        spannableString8.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spannableString8.length(), 0);
        spannableString8.setSpan(new RelativeSizeSpan(0.6f),0,spannableString8.length(),0);
        spannableString8.setSpan(new URLSpan("https://www.gov.pl/web/koronawirus/3etap/"),0,spannableString8.length(),0);
        viewTextLinksBuilder.append(spannableString8);

        SpannableString spannableString9 = new SpannableString("\nGrafiki:" +
                "\n- ");
        spannableString9.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spannableString9.length(), 0);
        spannableString9.setSpan(new RelativeSizeSpan(0.6f),0,spannableString9.length(),0);
        viewTextLinksBuilder.append(spannableString9);

        SpannableString spannableStringa = new SpannableString(
                "https://pl.freepik.com/premium-wektory/wiezienie-kraty-lub-paski-w-stylu-3d_5235168.html");
        spannableStringa.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spannableStringa.length(), 0);
        spannableStringa.setSpan(new RelativeSizeSpan(0.6f),0,spannableStringa.length(),0);
        spannableStringa.setSpan(new URLSpan("https://pl.freepik.com/premium-wektory/wiezienie-kraty-lub-paski-w-stylu-3d_5235168.html"),0,spannableStringa.length(),0);
        viewTextLinksBuilder.append(spannableStringa);

        SpannableString spannableStringb = new SpannableString(
                "\n- https://pl.freepik.com/premium-wektory/pogotowie-samochodowe-lub-samochod-medyczny_6168014.html");
        spannableStringb.setSpan(new ForegroundColorSpan(Color.GRAY), 0, spannableStringb.length(), 0);
        spannableStringb.setSpan(new RelativeSizeSpan(0.6f),0,spannableStringb.length(),0);
        spannableStringb.setSpan(new URLSpan("https://pl.freepik.com/premium-wektory/pogotowie-samochodowe-lub-samochod-medyczny_6168014.html"),"\n- ".length(),spannableStringb.length(),0);
        viewTextLinksBuilder.append(spannableStringb);
        viewTextLinks.setText(viewTextLinksBuilder, TextView.BufferType.SPANNABLE);
    }

    @Override
    public void onBackPressed() {
        Intent launchActivity = new Intent(this, MainActivity.class);
        startActivity(launchActivity);
        finish();
    }
}
