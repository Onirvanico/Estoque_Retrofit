package br.com.alura.estoque.retrofit.basecallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static br.com.alura.estoque.retrofit.basecallback.MensagemErro.MENSAGEM_ERRO_COMUNICACAO;
import static br.com.alura.estoque.retrofit.basecallback.MensagemErro.MENSAGEM_ERRO_RESPOSTA;

public class CallbackSemRetorno implements Callback<Void> {

    private final RespostaCallbackSemRetorno respostaCallback;

    public CallbackSemRetorno(RespostaCallbackSemRetorno respostaCallback) {
        this.respostaCallback = respostaCallback;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<Void> call, Response<Void> response) {
        if(response.isSuccessful()) {
            respostaCallback.quandoSucesso();
        } else {
            respostaCallback.quandoFalha(MENSAGEM_ERRO_RESPOSTA);
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<Void> call, Throwable t) {
            respostaCallback.quandoFalha(MENSAGEM_ERRO_COMUNICACAO + t.getMessage());
    }

    public interface RespostaCallbackSemRetorno {
        void quandoSucesso();
        void quandoFalha(String erro);
    }
}
