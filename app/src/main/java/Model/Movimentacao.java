package Model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import Config.ConfiguracaoFirebase;
import Helper.Base64Custom;
import Helper.DateCustom;

public class Movimentacao {
    private String key;       // Chave única do Firebase para essa movimentação
    private String data;
    private String categoria;
    private String descricao;
    private String tipo; // "r" para receita, "d" para despesa
    private double valor;

    public Movimentacao() {
    }

    public void salvar(String dataEscolhida) {
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        String idUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());
        String mesAno = DateCustom.mesAnoDataEscolhida(dataEscolhida);

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // Salva a movimentação
        firebaseRef.child("movimentacao")
                .child(idUsuario)
                .child(mesAno)
                .push()
                .setValue(this)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Se salvou a movimentação, atualiza os totais
                        atualizarTotaisUsuario(idUsuario);
                    }
                });
    }

    private void atualizarTotaisUsuario(String idUsuario) {
        DatabaseReference movimentacoesRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("movimentacao")
                .child(idUsuario);

        movimentacoesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DataSnapshot snapshot = task.getResult();

                double somaReceita = 0.0;
                double somaDespesa = 0.0;

                for (DataSnapshot mesAnoSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot movSnap : mesAnoSnapshot.getChildren()) {
                        Movimentacao mov = movSnap.getValue(Movimentacao.class);
                        if (mov != null) {
                            if ("r".equals(mov.getTipo())) {
                                somaReceita += mov.getValor();
                            } else if ("d".equals(mov.getTipo())) {
                                somaDespesa += mov.getValor();
                            }
                        }
                    }
                }

                double saldoTotal = somaReceita - somaDespesa;

                DatabaseReference usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                        .child("usuarios")
                        .child(idUsuario);

                usuarioRef.child("receitaTotal").setValue(somaReceita);
                usuarioRef.child("despesaTotal").setValue(somaDespesa);
                usuarioRef.child("saldoTotal").setValue(saldoTotal);
            }
        });
    }

    // Getters e setters

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
}
