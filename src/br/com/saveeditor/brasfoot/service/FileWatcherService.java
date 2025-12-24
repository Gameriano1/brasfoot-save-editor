package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.config.PreferencesManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Serviço para monitorar mudanças externas em arquivos.
 * Usa WatchService API do Java NIO.
 */
public class FileWatcherService {
    
    private final PreferencesManager preferencesManager;
    private WatchService watchService;
    private Thread watcherThread;
    private Path watchedFile;
    private String lastKnownHash;
    private final AtomicBoolean watching = new AtomicBoolean(false);
    private final AtomicBoolean hasLocalChanges = new AtomicBoolean(false);
    
    private FileChangeListener listener;
    
    public FileWatcherService() {
        this.preferencesManager = PreferencesManager.getInstance();
    }
    
    /**
     * Inicia o monitoramento de um arquivo.
     */
    public void startWatching(File file, FileChangeListener listener) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("Arquivo inválido ou não existe");
        }
        
        if (!preferencesManager.isAutoRefreshEnabled()) {
            System.out.println("⚠ Auto-refresh desabilitado nas preferências");
            return;
        }
        
        stopWatching();
        
        this.watchedFile = file.toPath();
        this.listener = listener;
        this.lastKnownHash = calculateFileHash(file);
        this.watching.set(true);
        
        // Iniciar thread de monitoramento
        watcherThread = new Thread(this::watchLoop, "FileWatcher-Thread");
        watcherThread.setDaemon(true);
        watcherThread.start();
        
        System.out.println("👁 Monitorando arquivo: " + file.getName());
    }
    
    /**
     * Para o monitoramento.
     */
    public void stopWatching() {
        watching.set(false);
        
        if (watcherThread != null && watcherThread.isAlive()) {
            watcherThread.interrupt();
            try {
                watcherThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                // Ignorar
            }
        }
        
        System.out.println("🛑 Monitoramento parado");
    }
    
    /**
     * Loop principal de monitoramento.
     */
    private void watchLoop() {
        try {
            // Criar WatchService para o diretório do arquivo
            Path directory = watchedFile.getParent();
            if (directory == null) {
                directory = Paths.get(".");
            }
            
            watchService = FileSystems.getDefault().newWatchService();
            directory.register(watchService, 
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
            
            long lastCheckTime = System.currentTimeMillis();
            int debounceMs = 500; // Evitar múltiplos triggers
            
            while (watching.get() && !Thread.currentThread().isInterrupted()) {
                WatchKey key;
                try {
                    // Poll com timeout
                    key = watchService.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    break;
                }
                
                if (key == null) {
                    continue;
                }
                
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    
                    // Verificar se é o arquivo que estamos monitorando
                    if (filename.equals(watchedFile.getFileName())) {
                        // Debounce: evitar múltiplos eventos em sequência
                        long now = System.currentTimeMillis();
                        if (now - lastCheckTime < debounceMs) {
                            continue;
                        }
                        lastCheckTime = now;
                        
                        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            handleFileDeleted();
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            handleFileModified();
                        }
                    }
                }
                
                key.reset();
            }
            
        } catch (IOException e) {
            System.err.println("❌ Erro no FileWatcher: " + e.getMessage());
            if (listener != null) {
                listener.onError(e);
            }
        }
    }
    
    /**
     * Trata modificação do arquivo.
     */
    private void handleFileModified() {
        // Esperar um pouco para garantir que a escrita terminou
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            return;
        }
        
        File file = watchedFile.toFile();
        if (!file.exists()) {
            return;
        }
        
        // Calcular novo hash
        String newHash = calculateFileHash(file);
        
        // Verificar se realmente mudou
        if (newHash != null && !newHash.equals(lastKnownHash)) {
            System.out.println("🔄 Arquivo modificado externamente detectado");
            
            // Verificar se há conflito (mudanças locais não salvas)
            if (hasLocalChanges.get()) {
                if (listener != null) {
                    listener.onConflictDetected();
                }
            } else {
                // Auto-reload sem conflito
                lastKnownHash = newHash;
                if (listener != null) {
                    listener.onFileChanged();
                }
            }
        }
    }
    
    /**
     * Trata deleção do arquivo.
     */
    private void handleFileDeleted() {
        System.out.println("🗑 Arquivo foi deletado");
        if (listener != null) {
            listener.onFileDeleted();
        }
        stopWatching();
    }
    
    /**
     * Calcula o hash SHA-256 de um arquivo.
     */
    private String calculateFileHash(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            System.err.println("⚠ Erro ao calcular hash: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Marca que há mudanças locais não salvas.
     */
    public void markLocalChanges(boolean hasChanges) {
        this.hasLocalChanges.set(hasChanges);
    }
    
    /**
     * Atualiza o hash conhecido após salvar.
     */
    public void updateKnownHash(File file) {
        this.lastKnownHash = calculateFileHash(file);
        this.hasLocalChanges.set(false);
        System.out.println("✅ Hash atualizado após salvamento");
    }
    
    /**
     * Verifica se está monitorando.
     */
    public boolean isWatching() {
        return watching.get();
    }
    
    /**
     * Interface para callbacks de mudanças.
     */
    public interface FileChangeListener {
        /**
         * Chamado quando o arquivo é modificado externamente (sem conflito).
         */
        void onFileChanged();
        
        /**
         * Chamado quando há conflito (arquivo externo mudou + mudanças locais).
         */
        void onConflictDetected();
        
        /**
         * Chamado quando o arquivo é deletado.
         */
        void onFileDeleted();
        
        /**
         * Chamado quando há erro no watcher.
         */
        void onError(Exception e);
    }
    
    /**
     * Estratégia de resolução de conflito.
     */
    public enum ResolutionStrategy {
        KEEP_LOCAL,       // Manter mudanças locais (ignorar externas)
        LOAD_EXTERNAL,    // Carregar mudanças externas (perder locais)
        SAVE_AND_RELOAD   // Salvar em novo arquivo e recarregar externas
    }
}
