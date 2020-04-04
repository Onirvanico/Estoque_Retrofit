package br.com.alura.estoque.repository;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ProdutoRepository {

    private ProdutoDAO dao;
    private final ProdutoService produtoService;

    public ProdutoRepository(ProdutoDAO dao) {

        produtoService = new EstoqueRetrofit().getProdutoService();
        this.dao = dao;
    }

    public void buscaProdutos(DadosCarregadosCallback<List<Produto>> call) {

        atualizaInterno(call);
    }

    private void atualizaInterno(DadosCarregadosCallback<List<Produto>> call) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    call.quandoSucesso(resultado);
                    atualizaNaApi(call);
                })
                .execute();
    }

    private void atualizaNaApi(DadosCarregadosCallback<List<Produto>> callback) {
        Call<List<Produto>> call = produtoService.buscaTodos();

        call.enqueue(
                new Callback<List<Produto>>() {
                    @Override
                    public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                        if(response.isSuccessful()) {
                            List<Produto> produtosCarregados = response.body();
                            if(produtosCarregados != null) {
                                atualizaInterno(produtosCarregados, callback);
                            }
                        } else {
                            callback.quandoFalha("Resposta não sucedida");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Produto>> call, Throwable t) {
                        callback.quandoFalha("Falha na comunicação");
                    }
                }


        );
    }

    private void atualizaInterno(List<Produto> produtosCarregados, DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>( () -> {
            dao.salva(produtosCarregados);
            return dao.buscaTodos();

        }, callback::quandoSucesso)
                .execute();
    }

    public void salva(Produto produtoCriado, DadosCarregadosCallback<Produto> callBack) {
        salvaNaApi(produtoCriado, callBack);
    }

    private void salvaNaApi(Produto produtoCriado, DadosCarregadosCallback<Produto> callBack) {
        Call<Produto> call = produtoService.salva(produtoCriado);
        call.enqueue(new Callback<Produto>() {

            @EverythingIsNonNull
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {

                if(response.isSuccessful()) {
                    Produto produtoSalvo = response.body();
                    if(produtoSalvo != null) {
                        salvaInterno(produtoSalvo, callBack);
                        callBack.quandoSucesso(produtoSalvo);
                    }
                } else {
                        callBack.quandoFalha("Falha ao tentar receber uma resposta");
                }


            }

            @EverythingIsNonNull
            @Override
            public void onFailure(Call<Produto> call, Throwable t) {
                callBack.quandoFalha("Falha na comunicação " + t.getMessage());
            }
        });
    }

    private void salvaInterno(Produto produtoSalvo, DadosCarregadosCallback<Produto> callBack) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produtoSalvo);
            Log.i("produto ", dao.buscaProduto(id).toString());
            return dao.buscaProduto(id);
        }, callBack::quandoSucesso
        ).execute();
    }


    public interface DadosCarregadosCallback<T> {
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
