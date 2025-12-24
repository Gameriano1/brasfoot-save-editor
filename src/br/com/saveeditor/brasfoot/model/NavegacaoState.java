package br.com.saveeditor.brasfoot.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Armazena todo o estado da sessão de edição atual.
 */
public class NavegacaoState {
    private final Object objetoRaiz;
    private final Object dataAfQ;
    private final Stack<Object> trilhaNavegacao = new Stack<>();
    private final Map<Object, Integer> viewState = new HashMap<>();

    private String caminhoArquivoOriginal;
    private long ultimoTimestampModificacao;

    public NavegacaoState(Object objetoRaiz, Object dataAfQ, String caminhoArquivoOriginal) {
        this.objetoRaiz = objetoRaiz;
        this.dataAfQ = dataAfQ;
        this.caminhoArquivoOriginal = caminhoArquivoOriginal;
        this.ultimoTimestampModificacao = new File(caminhoArquivoOriginal).lastModified();
        this.trilhaNavegacao.push(objetoRaiz);
    }

    public Object getObjetoAtual() {
        return trilhaNavegacao.peek();
    }

    public void entrar(Object novoObjeto) {
        trilhaNavegacao.push(novoObjeto);
    }

    public void voltar() {
        if (trilhaNavegacao.size() > 1) {
            trilhaNavegacao.pop();
        }
    }

    public void irParaTopo() {
        while (trilhaNavegacao.size() > 1) {
            trilhaNavegacao.pop();
        }
    }
    
    public void limparNavegacaoEReiniciar() {
        trilhaNavegacao.clear();
        viewState.clear();
        trilhaNavegacao.push(objetoRaiz);
    }

    // Getters e Setters
    public Object getObjetoRaiz() { return objetoRaiz; }
    public Object getDataAfQ() { return dataAfQ; }
    public Stack<Object> getTrilhaNavegacao() { return trilhaNavegacao; }
    public Map<Object, Integer> getViewState() { return viewState; }
    public String getCaminhoArquivoOriginal() { return caminhoArquivoOriginal; }
    public void setCaminhoArquivoOriginal(String caminho) { this.caminhoArquivoOriginal = caminho; }
    public long getUltimoTimestampModificacao() { return ultimoTimestampModificacao; }
    public void setUltimoTimestampModificacao(long timestamp) { this.ultimoTimestampModificacao = timestamp; }
}