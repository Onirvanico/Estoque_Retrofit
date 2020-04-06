package br.com.alura.estoque.repository;

import android.util.Log;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.basecallback.BaseCallback;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;

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
                new BaseCallback<>(new BaseCallback.RespostaCallback<List<Produto>>() {
                    @Override
                    public void quandoSucesso(List<Produto> resposta) {
                        atualizaInterno(resposta, callback);
                    }

                    @Override
                    public void quandoFalha(String erro) {
                        callback.quandoFalha(erro);
                    }
                })
               );
    }

    private void atualizaInterno(List<Produto> produtosCarregados, DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>( () -> {
            dao.salva(produtosCarregados);
            return dao.buscaTodos();

        }, callback::quandoSucesso)
                .execute();
    }

    public void salva(Produto produtoCriado, DadosCarregadosCallback<Produto> callback) {
        salvaNaApi(produtoCriado, callback);
    }

    private void salvaNaApi(Produto produtoCriado, DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = produtoService.salva(produtoCriado);
        call.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto resposta) {
                salvaInterno(resposta, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));

    }

    private void salvaInterno(Produto produtoSalvo, DadosCarregadosCallback<Produto> callBack) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produtoSalvo);
            Log.i("produto ", dao.buscaProduto(id).toString());
            return dao.buscaProduto(id);
        }, callBack::quandoSucesso
        ).execute();
    }

    public void edita(Produto produto, DadosCarregadosCallback<Produto> callback) {
        editaNaApi(produto, callback);

    }

    private void editaNaApi(Produto produto, DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = produtoService.edita(produto.getId(), produto);

        call.enqueue(
                new BaseCallback<>(new BaseCallback.RespostaCallback<Produto>() {
                    @Override
                    public void quandoSucesso(Produto resposta) {
                        editaInterno(produto, callback);
                    }

                    @Override
                    public void quandoFalha(String erro) {
                         callback.quandoFalha(erro);
                    }
                })
        );
    }

    private void editaInterno(Produto produto, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            dao.atualiza(produto);
            return produto;
        }, callback::quandoSucesso)
                .execute();
    }

    public void remove(Produto produtoEscolhido, DadosCarregadosCallback<Void> callback) {

        removeNaApi(produtoEscolhido, callback);

    }

    private void removeNaApi(Produto produtoEscolhido, DadosCarregadosCallback<Void> callback) {
        Call<Void> call = produtoService.remove(produtoEscolhido.getId());
        call.enqueue(
                new BaseCallback<>(new BaseCallback.RespostaCallback<Void>() {
                    @Override
                    public void quandoSucesso(Void resposta) {
                        removeInterno(produtoEscolhido, callback);
                    }

                    @Override
                    public void quandoFalha(String erro) {
                        callback.quandoFalha(erro);
                    }
                })
        );
    }

    private void removeInterno(Produto produtoEscolhido, DadosCarregadosCallback<Void> callback) {
        new BaseAsyncTask<>(() -> {
            dao.remove(produtoEscolhido);
            return null;
        }, callback::quandoSucesso)
                .execute();
    }


    public interface DadosCarregadosCallback<T> {
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
