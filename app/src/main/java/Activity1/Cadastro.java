package Activity1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.organizzeclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import Config.ConfiguracaoFirebase;
import Helper.Base64Custom;
import Model.Usuario;

public class Cadastro extends AppCompatActivity {
    private EditText editNome, editEmail, editTSenha;
    private Button buttonCadastrar;
    private FirebaseAuth autenticacao;
    private Usuario usuario;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        //// status bar
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#01C7d2"));

        //////
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editiEmail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cadastro");
        }

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editTSenha = findViewById(R.id.editTSenha);
        buttonCadastrar = findViewById(R.id.buttonCadastrar);

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = editNome.getText().toString();
                String email = editEmail.getText().toString();
                String senha = editTSenha.getText().toString();
                 if( nome.isEmpty() || email.isEmpty() || senha.isEmpty()){



                     Toast.makeText(Cadastro.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    }else{

                     Toast.makeText(Cadastro.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                      usuario = new Usuario();

                     usuario.setNome(nome);
                     usuario.setEmail(email);
                     usuario.setSenha(senha);
                     CadastroUsuario();


                 }




            }


        });
    }

    public void CadastroUsuario() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    // Pegando o UID do usuário autenticado
                    String idUsuario = autenticacao.getCurrentUser().getUid();
                    usuario.setIdUsuario(idUsuario);

                    // Salva o usuário no banco com UID como chave
                    usuario.salvar();

                    Toast.makeText(Cadastro.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Cadastro.this, LoginActivity.class));
                    finish(); // Finaliza a activity de cadastro

                } else {
                    String excessao = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e) {
                        excessao = "Digite uma senha mais forte!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excessao = "Digite um email válido!";
                    } catch (FirebaseAuthUserCollisionException e) {
                        excessao = "Essa conta já foi cadastrada!";
                    } catch (Exception e) {
                        excessao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(Cadastro.this, excessao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void TermosDeUso(View view){

        startActivity(new Intent(this, Termos.class));

    }

}