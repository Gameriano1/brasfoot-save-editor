package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Serviço responsável por todas as operações de ficheiro.
 * IMPORTANTE: Usa Kryo separados para leitura e escrita!
 */
public class SaveFileService {
    private final Kryo kryoReader;  // Apenas para leitura

    public SaveFileService() {
        this.kryoReader = new Kryo();
        configurarKryoParaLeitura(this.kryoReader);
    }

    private void configurarKryoParaLeitura(Kryo kryo) {
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.setRegistrationRequired(false);
        kryo.setClassLoader(this.getClass().getClassLoader());
        
        // Serializer especial para ArrayList (apenas para leitura)
        CollectionSerializer arrayListSerializer = new CollectionSerializer() {
            @Override
            protected Collection create(Kryo kryo, Input input, Class<Collection> type) {
                return new ArrayList();
            }
        };
        kryo.register(ArrayList.class, arrayListSerializer);
    }
    
    /**
     * Cria um Kryo LIMPO apenas para escrita.
     * Não usa as configurações especiais de leitura que podem corromper o arquivo.
     */
    private Kryo criarKryoParaEscrita() {
        Kryo kryoWriter = new Kryo();
        kryoWriter.setRegistrationRequired(false);
        kryoWriter.setClassLoader(this.getClass().getClassLoader());
        // NÃO configura InstantiatorStrategy nem CollectionSerializer customizado
        return kryoWriter;
    }

    public Optional<NavegacaoState> carregarSave(String filePath) {
        // Criar backup
        try {
            String backupPath = filePath + ".bak";
            System.out.println("A criar backup do ficheiro original em: " + backupPath);
            Files.copy(Paths.get(filePath), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("✔ Backup criado com sucesso!");
        } catch (IOException e) {
            System.err.println("✖ Falha ao criar o backup: " + e.getMessage());
        }

        // Ler arquivo
        System.out.println("\nA tentar ler o ficheiro: " + filePath);
        try (Input input = new Input(new FileInputStream(filePath))) {
            Object objetoRaiz = kryoReader.readClassAndObject(input);
            Object dataAfQ = kryoReader.readClassAndObject(input);
            System.out.println("✔ Ficheiro lido com sucesso!");
            return Optional.of(new NavegacaoState(objetoRaiz, dataAfQ, filePath));
        } catch (Exception e) {
            System.err.println("✖ Erro ao ler o ficheiro: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void salvarSave(NavegacaoState estado, String nomeArquivo) {
        File originalFile = new File(estado.getCaminhoArquivoOriginal());
        String parentDirectory = originalFile.getParent();
        if (parentDirectory == null) {
            parentDirectory = ".";
        }
        String fullSavePath = Paths.get(parentDirectory, nomeArquivo).toString();

        System.out.println("\n💾 A salvar o estado atual para o ficheiro: " + fullSavePath);
        
        // IMPORTANTE: Criar um Kryo NOVO e LIMPO apenas para escrita!
        Kryo kryoWriter = criarKryoParaEscrita();
        
        try (Output output = new Output(new FileOutputStream(fullSavePath))) {
            // Salvar na mesma ordem que foi lido
            kryoWriter.writeClassAndObject(output, estado.getObjetoRaiz());
            kryoWriter.writeClassAndObject(output, estado.getDataAfQ());
            
            output.flush(); // Garantir que tudo foi escrito
            
            System.out.println("✔ Ficheiro salvo com sucesso!");
            System.out.println("📁 Localização: " + fullSavePath);

            // Atualizar timestamp se for o mesmo arquivo
            try {
                if (new File(fullSavePath).getCanonicalPath().equals(originalFile.getCanonicalPath())) {
                    estado.setUltimoTimestampModificacao(new File(fullSavePath).lastModified());
                    System.out.println("🔄 Timestamp atualizado");
                }
            } catch (IOException e) {
                // Ignorar erro de comparação de paths
            }
            
        } catch (Exception e) {
            System.err.println("✖ Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
            
            // Tentar deletar arquivo corrompido
            try {
                File corruptedFile = new File(fullSavePath);
                if (corruptedFile.exists() && corruptedFile.length() == 0) {
                    corruptedFile.delete();
                    System.err.println("🗑️ Arquivo corrompido removido");
                }
            } catch (Exception ex) {
                // Ignorar
            }
        }
    }
    
    /**
     * Valida se um arquivo salvo pode ser lido novamente.
     * Útil para testar se o salvamento foi bem-sucedido.
     */
    public boolean validarArquivoSalvo(String filePath) {
        System.out.println("\n🔍 Validando arquivo salvo...");
        
        Kryo kryoTest = new Kryo();
        configurarKryoParaLeitura(kryoTest);
        
        try (Input input = new Input(new FileInputStream(filePath))) {
            Object obj1 = kryoTest.readClassAndObject(input);
            Object obj2 = kryoTest.readClassAndObject(input);
            
            if (obj1 == null || obj2 == null) {
                System.err.println("✖ Validação falhou: Objetos nulos");
                return false;
            }
            
            System.out.println("✔ Arquivo validado com sucesso!");
            return true;
            
        } catch (Exception e) {
            System.err.println("✖ Validação falhou: " + e.getMessage());
            return false;
        }
    }
}