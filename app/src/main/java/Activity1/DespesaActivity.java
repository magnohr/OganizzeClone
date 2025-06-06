package Activity1;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.organizzeclone.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import Config.ConfiguracaoFirebase;
import Helper.Base64Custom;
import Helper.DateCustom;
import Model.Movimentacao;
import Model.Usuario;

public class DespesaActivity extends AppCompatActivity {

    private TextInputLayout layoutData, layoutCategoria, layoutDescricao;
    private EditText editValor, editData, editCategoria, editDescricao;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef;
    private FirebaseAuth autenticacao;
    private double despesaTotal = 0.0;
    private String idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_despesa);

        // Cor da status bar
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#FD5252"));

        // Margens com EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Firebase
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        FirebaseUser user = autenticacao.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        idUsuario = Base64Custom.codificarBase64(user.getEmail());

        // Inicializa os campos
        layoutData = findViewById(R.id.textInputData);
        layoutCategoria = findViewById(R.id.textInputCategoria);
        layoutDescricao = findViewById(R.id.textInputDescricao);

        editData = layoutData.getEditText();
        editCategoria = layoutCategoria.getEditText();
        editDescricao = layoutDescricao.getEditText();
        editValor = findViewById(R.id.editValor);

        // Define data atual
        if (editData != null) {
            editData.setText(DateCustom.dataAtual());
        }

        recuperarDespesas();
    }

    public void salvarDespesas(View view) {
        if (editValor == null || editData == null || editCategoria == null || editDescricao == null) {
            showMessage("Erro interno nos campos.");
            return;
        }

        String valorTexto = editValor.getText().toString().replace(",", ".");
        String data = editData.getText().toString();
        String categoria = editCategoria.getText().toString();
        String descricao = editDescricao.getText().toString();

        if (!validarCampos(valorTexto, data, categoria, descricao)) return;

        try {
            double valor = Double.parseDouble(valorTexto);

            if (valor <= 0) {
                showMessage("O valor deve ser maior que zero.");
                return;
            }

            // Criar movimentação
            movimentacao = new Movimentacao();
            movimentacao.setValor(valor);
            movimentacao.setData(data);
            movimentacao.setCategoria(categoria);
            movimentacao.setDescricao(descricao);
            movimentacao.setTipo("d"); // despesa

            double despesaAtualizada = despesaTotal + valor;
            atualizarDespesas(despesaAtualizada);
            movimentacao.salvar(data);

            showMessage("Despesa salva com sucesso!");
            finish();

        } catch (NumberFormatException e) {
            showMessage("Valor inválido.");
        }
    }

    private boolean validarCampos(String valorTexto, String data, String categoria, String descricao) {
        if (valorTexto.isEmpty()) {
            showMessage("Preencha o valor.");
            return false;
        }
        if (data.isEmpty()) {
            showMessage("Preencha a data.");
            return false;
        }
        if (!data.matches("\\d{2}/\\d{2}/\\d{4}")) {
            showMessage("Data inválida. Use o formato dd/MM/yyyy.");
            return false;
        }
        if (categoria.isEmpty()) {
            showMessage("Preencha a categoria.");
            return false;
        }
        if (descricao.isEmpty()) {
            showMessage("Preencha a descrição.");
            return false;
        }
        return true;
    }

    private void showMessage(String texto) {
        Snackbar.make(findViewById(R.id.main), texto, Snackbar.LENGTH_SHORT).show();
    }

    public void recuperarDespesas() {
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                if (usuario != null) {
                    despesaTotal = usuario.getDespesaTotal();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage("Erro ao recuperar dados.");
            }
        });
    }

    public void atualizarDespesas(double despesa) {
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.child("despesaTotal").setValue(despesa);
    }

    public void voltar(View view) {
        finish();
    }
}
