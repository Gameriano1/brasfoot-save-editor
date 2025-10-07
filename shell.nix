{ pkgs ? import <nixpkgs> { config.allowUnfree = true; } }:

let
  # Define a versão do Java Development Kit (JDK) a ser usada
  # Você pode mudar para jdk17, jdk21, etc., dependendo da sua necessidade
  jdk = pkgs.jdk21; 
in
pkgs.mkShell {
  # Ferramentas focadas em Java e Engenharia Reversa
  packages = [
    # --- Ferramentas Java ---
    jdk  # Java Development Kit (inclui java, javac, jar)

    # --- Ferramentas Essenciais de Build (caso use nativo/C/C++) ---
    pkgs.gcc 
    pkgs.gnumake 
    pkgs.cmake 
    pkgs.pkg-config 
    
    # --- Decompilação e Engenharia Reversa ---
    # Descompilador gráfico para bytecode Java (.jar, .class)
    pkgs.bytecode-viewer      
    # Ferramenta avançada para análise de binários (útil para código nativo)
    pkgs.radare2              
    # Editor hexadecimal para o terminal
    pkgs.hexedit              
    # Essencial para extrair arquivos .jar e outros pacotes
    pkgs.unzip                
    
    # --- Utilitários de Shell ---
    pkgs.zlib 
  ];

  # Variáveis de ambiente
  shellHook = ''
    echo "Ambiente de desenvolvimento Java e Engenharia Reversa carregado."
    export JAVA_HOME=${jdk}
    export PATH=$JAVA_HOME/bin:$PATH
  '';
}