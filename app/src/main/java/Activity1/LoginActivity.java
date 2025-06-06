package Activity1;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.organizzeclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import Config.ConfiguracaoFirebase;
import Model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private Button butaoEntrar;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editiEmail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editEmail = findViewById(R.id.editEmailLogin);
        editSenha = findViewById(R.id.editiSenha);
        butaoEntrar = findViewById(R.id.butaoEntrar);

        butaoEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String senha = editSenha.getText().toString();

                if (email.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                } else {
                     usuario = new Usuario();
                    usuario.setEmail( email);
                    usuario.setSenha(senha);
                    ValidarLogin();
                }

            }
        });
    }
    public void ValidarLogin(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
         autenticacao.signInWithEmailAndPassword(
                 usuario.getEmail(),
                 usuario.getSenha()
         ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete(@NonNull Task<AuthResult> task) {
                  if(task.isSuccessful()){
                      abrirTelaPrincipal();
                  }else{

                      String excessao ="";
                      try {
                          throw task.getException();
                      }catch (FirebaseAuthInvalidUserException  e){
                          excessao = "Usuário não está cadastrado";
                      }catch (FirebaseAuthInvalidCredentialsException e){
                          excessao = "Usuário ou senha inválidos";
                      }catch (Exception e){
                          excessao = "Erro ao fazer login:" + e.getMessage();
                          e.printStackTrace();
                      }
                      Toast.makeText(LoginActivity.this, excessao, Toast.LENGTH_SHORT).show();
                  }
             }
         });

    }

    public void abrirTelaPrincipal(){
        startActivity(new Intent(this, TelaPrincipal.class));
    }
}