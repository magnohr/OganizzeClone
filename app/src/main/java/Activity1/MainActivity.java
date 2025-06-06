package Activity1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.example.organizzeclone.R;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

public class MainActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica se já viu a introdução
        SharedPreferences preferences = getSharedPreferences("introPrefs", MODE_PRIVATE);
        boolean introVisto = preferences.getBoolean("intro_visto", false);

        if (introVisto) {
            // Se já viu a introdução, vai direto para LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Finaliza essa activity
            return;
        }

        // Caso contrário, exibe os slides
        setButtonBackVisible(false);
        setButtonNextVisible(false);

        addSlide(new FragmentSlide.Builder()
                .fragment(R.layout.intro_1)
                .background(android.R.color.white)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .fragment(R.layout.intro_2)
                .background(android.R.color.white)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .fragment(R.layout.intro_3)
                .background(android.R.color.white)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .fragment(R.layout.intro_4)
                .background(android.R.color.white)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .fragment(R.layout.intro_cadastro)
                .background(android.R.color.white)
                .canGoForward(false)
                .build()
        );
    }

    public void BtCadastrar(View view){
        salvarIntroVisto(); // Marca que já viu
        startActivity(new Intent(this, Cadastro.class));
        finish();
    }

    public void BtEntrar(View view){
        salvarIntroVisto(); // Marca que já viu
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void salvarIntroVisto() {
        SharedPreferences preferences = getSharedPreferences("introPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("intro_visto", true);
        editor.apply();
    }
}
