package br.com.alura.estoque.retrofit.basecallback;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class BaseCallback<T> implements Callback<T> {

    private final RespostaCallback callback;

    public BaseCallback(RespostaCallback callback) {
        this.callback = callback;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.isSuccessful()) {
            T resultado = response.body();
            if(resultado != null) {
                callback.quandoSucesso(resultado);
            }
        } else {
            callback.quandoFalha("Resposta não recebida");
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<T> call, Throwable t) {
            callback.quandoFalha("Falha na comunicação " + t.getMessage());
    }

    public interface RespostaCallback<T> {
        void quandoSucesso(T resposta);
        void quandoFalha(String erro);
    }
}
