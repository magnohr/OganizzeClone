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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import Config.ConfiguracaoFirebase;
import Helper.Base64Custom;
import Helper.DateCustom;
import Model.Movimentacao;
import Model.Usuario;

public class ReceitaActivity extends AppCompatActivity {

    // Layouts dos campos de entrada
    private TextInputLayout layoutData, layoutCategoria, layoutDescricao;
    private EditText editValor, editData, editCategoria, editDescricao;

    private Movimentacao movimentacao;

    // Firebase
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private double receitaTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receita);

        // Muda a cor da barra de status
        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#69EEAD"));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa campos
        layoutData = findViewById(R.id.textInputData);
        layoutCategoria = findViewById(R.id.textInputCategoria);
        layoutDescricao = findViewById(R.id.textInputDescricao);

        editData = layoutData.getEditText();
        editCategoria = layoutCategoria.getEditText();
        editDescricao = layoutDescricao.getEditText();
        editValor = findViewById(R.id.editValor);

        if (editData != null) {
            editData.setText(DateCustom.dataAtual());
        }

        recuperarReceitas();
    }

    public void salvarReceita(View view) {
        String valorTexto = editValor.getText().toString().replace(",", ".").trim();
        String data = editData.getText().toString().trim();
        String categoria = editCategoria.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();

        if (!validarCampos(valorTexto, data, categoria, descricao)) return;

        try {
            double valor = Double.parseDouble(valorTexto);

            if (valor <= 0) {
                editValor.setError("O valor deve ser maior que zero");
                return;
            } else {
                editValor.setError(null);
            }

            movimentacao = new Movimentacao();
            movimentacao.setValor(valor);
            movimentacao.setData(data);
            movimentacao.setCategoria(categoria);
            movimentacao.setDescricao(descricao);
            movimentacao.setTipo("r");

            double receitaAtualizada = receitaTotal + valor;
            atualizarReceita(receitaAtualizada);
            movimentacao.salvar(data);

            Toast.makeText(this, "Receita salva com sucesso!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            editValor.setError("Valor inválido");
        }
    }

    private boolean validarCampos(String valor, String data, String categoria, String descricao) {
        boolean valido = true;

        if (valor.isEmpty()) {
            editValor.setError("Preencha o valor");
            valido = false;
        } else {
            editValor.setError(null);
        }

        if (data.isEmpty()) {
            layoutData.setError("Preencha a data");
            valido = false;
        } else if (!data.matches("\\d{2}/\\d{2}/\\d{4}")) {
            layoutData.setError("Data inválida. Use o formato dd/MM/yyyy");
            valido = false;
        } else {
            layoutData.setError(null);
        }

        if (categoria.isEmpty()) {
            layoutCategoria.setError("Preencha a categoria");
            valido = false;
        } else {
            layoutCategoria.setError(null);
        }

        if (descricao.isEmpty()) {
            layoutDescricao.setError("Preencha a descrição");
            valido = false;
        } else {
            layoutDescricao.setError(null);
        }

        return valido;
    }

    public void recuperarReceitas() {
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                if (usuario != null) {
                    receitaTotal = usuario.getReceitaTotal();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReceitaActivity.this, "Erro ao recuperar dados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void atualizarReceita(double receita) {
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
        usuarioRef.child("receitaTotal").setValue(receita);
    }
}
