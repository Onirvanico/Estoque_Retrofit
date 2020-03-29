package br.com.alura.estoque.repository;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

public class ProdutoRepository {

    private ProdutoDAO dao;
    private final ProdutoService produtoService;

    public ProdutoRepository(ProdutoDAO dao) {

        produtoService = new EstoqueRetrofit().getProdutoService();
        this.dao = dao;
    }

    public void buscaProdutos(DadosCarregadosListener<List<Produto>> listener) {

        atualizaInterno(listener);
    }

    private void atualizaInterno(DadosCarregadosListener<List<Produto>> listener) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    listener.quandoCarregado(resultado);
                    atualizaNaApi(listener);
                })
                .execute();
    }

    private void atualizaNaApi(DadosCarregadosListener<List<Produto>> listener) {
        Call<List<Produto>> call = produtoService.buscaTodos();
        new BaseAsyncTask<>(() -> {
            try {
                Response<List<Produto>> resposta = call.execute();
                List<Produto> produtos = resposta.body();
                dao.salva(produtos);
                return produtos;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return dao.buscaTodos();
        }, listener::quandoCarregado
        ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void salva(Produto produtoCriado, DadosCarregadosListener<Produto> listener, Context context) {
        Call<Produto> call = produtoService.salva(produtoCriado);
        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {

                Produto produtoSalvo = response.body();

                Log.i("produto_id ", produtoSalvo.toString());
                new BaseAsyncTask<>(() -> {
                    long id = dao.salva(produtoSalvo);
                    Log.i("produto ", dao.buscaProduto(id).toString());
                    return dao.buscaProduto(id);
                }, listener::quandoCarregado
                ).execute();
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {

            }
        });
    }

    public interface DadosCarregadosListener<T> {
        void quandoCarregado(T produtos);
    }
}
