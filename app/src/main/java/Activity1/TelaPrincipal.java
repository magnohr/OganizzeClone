package Activity1;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.organizzeclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import Adapter.AdapterMovimentacao;
import Config.ConfiguracaoFirebase;
import Helper.Base64Custom;
import Model.Movimentacao;
import Model.Usuario;

public class TelaPrincipal extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView textSaldo, textSaudacao;

    private double despesaTotal = 0.0;
    private double receitaTotal = 0.0;

    private RecyclerView recyclerView;
    private AdapterMovimentacao adapterMovimentacao;

    private final List<Movimentacao> movimentacoes = new ArrayList<>();

    private final FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private final DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacao;
    private DatabaseReference movimentacoesRef;

    private String mesAnoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#03D8C4"));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ORGANIZZE");
        }

        textSaldo = findViewById(R.id.textSaldo);
        textSaudacao = findViewById(R.id.textSaudacao);
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerView);

        configuraCalendarView();

        adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterMovimentacao);

        configurarSwipe();
    }

    private void configurarSwipe() {
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Movimentacao movimentacao = movimentacoes.get(position);

                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custom.codificarBase64(emailUsuario);
                String chave = movimentacao.getKey();

                DatabaseReference movimentacaoRef = firebaseRef.child("movimentacao")
                        .child(idUsuario)
                        .child(mesAnoSelecionado)
                        .child(chave);

                movimentacaoRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        atualizarSaldo(movimentacao);
                        Toast.makeText(TelaPrincipal.this, "Movimentação excluída", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TelaPrincipal.this, "Erro ao excluir", Toast.LENGTH_SHORT).show();
                    }
                });

                movimentacoes.remove(position);
                adapterMovimentacao.notifyItemRemoved(position);
            }
        };

        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerView);
    }

    private String formatarMesAno(CalendarDay data) {
        int mes = data.getMonth() + 1;
        int ano = data.getYear();
        return String.format("%02d%d", mes, ano);
    }

    public void atualizarSaldo(Movimentacao movimentacao) {
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        if (movimentacao.getTipo().equals("r")) {
            receitaTotal -= movimentacao.getValor();
            if (receitaTotal < 0) receitaTotal = 0;
            usuarioRef.child("receitatotal").setValue(receitaTotal);
        } else if (movimentacao.getTipo().equals("d")) {
            despesaTotal -= movimentacao.getValor();
            if (despesaTotal < 0) despesaTotal = 0;
            usuarioRef.child("despesatotal").setValue(despesaTotal);
        }

        double saldoTotal = receitaTotal - despesaTotal;
        DecimalFormat decimalFormat = new DecimalFormat("###,##0.00");
        textSaldo.setText("R$ " + decimalFormat.format(saldoTotal));
    }

    public void recuperarMovimentacoes() {
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custom.codificarBase64(emailUsuario);

        if (mesAnoSelecionado == null) {
            CalendarDay hoje = calendarView.getCurrentDate();
            mesAnoSelecionado = formatarMesAno(hoje);
        }

        movimentacoesRef = firebaseRef.child("movimentacao")
                .child(idUsuario)
                .child(mesAnoSelecionado);

        if (valueEventListenerMovimentacao != null && movimentacoesRef != null) {
            movimentacoesRef.removeEventListener(valueEventListenerMovimentacao);
        }

        valueEventListenerMovimentacao = movimentacoesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movimentacoes.clear();
                for (DataSnapshot dados : snapshot.getChildren()) {
                    Movimentacao movimentacao = dados.getValue(Movimentacao.class);
                    movimentacao.setKey(dados.getKey());
                    movimentacoes.add(movimentacao);
                }
                adapterMovimentacao.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("recuperarMovimentacoes", "Erro ao carregar movimentações", error.toException());
            }
        });
    }

    private void recuperarResumo() {
        if (autenticacao.getCurrentUser() != null) {
            String emailUsuario = autenticacao.getCurrentUser().getEmail();
            String idUsuario = Base64Custom.codificarBase64(emailUsuario);
            usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

            if (valueEventListenerUsuario != null && usuarioRef != null) {
                usuarioRef.removeEventListener(valueEventListenerUsuario);
            }

            valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Usuario usuario = snapshot.getValue(Usuario.class);

                    if (usuario != null) {
                        despesaTotal = usuario.getDespesaTotal();
                        receitaTotal = usuario.getReceitaTotal();

                        double saldoTotal = receitaTotal - despesaTotal;
                        DecimalFormat decimalFormat = new DecimalFormat("###,##0.00");
                        textSaudacao.setText("Olá, " + usuario.getNome());
                        textSaldo.setText("R$ " + decimalFormat.format(saldoTotal));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    textSaudacao.setText("Erro ao carregar");
                    textSaldo.setText("R$ 0,00");
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuSair) {
            autenticacao.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void Receita(View view) {
        startActivity(new Intent(this, ReceitaActivity.class));
    }

    public void Despesas(View view) {
        startActivity(new Intent(this, DespesaActivity.class));
    }

    private void configuraCalendarView() {
        CharSequence[] meses = {
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        calendarView.setTitleMonths(meses);

        CalendarDay dataAtual = calendarView.getCurrentDate();
        mesAnoSelecionado = formatarMesAno(dataAtual);

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                mesAnoSelecionado = formatarMesAno(date);
                if (movimentacoesRef != null && valueEventListenerMovimentacao != null) {
                    movimentacoesRef.removeEventListener(valueEventListenerMovimentacao);
                }
                recuperarMovimentacoes();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (usuarioRef != null && valueEventListenerUsuario != null) {
            usuarioRef.removeEventListener(valueEventListenerUsuario);
        }
        if (movimentacoesRef != null && valueEventListenerMovimentacao != null) {
            movimentacoesRef.removeEventListener(valueEventListenerMovimentacao);
        }
    }
}
