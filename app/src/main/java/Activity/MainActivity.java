package Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.organizzeclone.R;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

public class MainActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setButtonBackVisible(false);
        setButtonNextVisible(false);

        addSlide( new FragmentSlide.Builder()
                .fragment(R.layout.intro_1)
                .background(android.R.color.white)
                .build()
        );

        addSlide( new FragmentSlide.Builder()
                .fragment(R.layout.intro_2)
                .background(android.R.color.white)
                .build()
        );

        addSlide( new FragmentSlide.Builder()
                .fragment(R.layout.intro_3)
                .background(android.R.color.white)
                .build()
        );

        addSlide( new FragmentSlide.Builder()
                .fragment(R.layout.intro_4)
                .background(android.R.color.white)
              //  .canGoForward(false)
              //  .canGoBackward(false)
                .build()
        );

        addSlide( new FragmentSlide.Builder()
                .fragment(R.layout.tela_cadastro)
                .background(android.R.color.holo_orange_light)
                .canGoForward(false)

                .build()
        );



    }

    public void BtCadastrar(View view){

        startActivity(new Intent(this, Tela_Cadastro.class));



    }

    public void BtEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
    }
}