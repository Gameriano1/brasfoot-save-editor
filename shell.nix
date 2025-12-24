{ pkgs ? import <nixpkgs> { config.allowUnfree = true; } }:

let
  jdk = pkgs.jdk21;
in
pkgs.mkShell {
  packages = [
    jdk
    pkgs.wget
    pkgs.tree
  ];

  shellHook = ''
    echo "🎮 Ambiente Brasfoot Save Editor - GUI Moderna"
    
    export JAVA_HOME=${jdk}
    export PATH=$JAVA_HOME/bin:$PATH
    
    mkdir -p lib
    
    # Baixar FlatLaf (Look and Feel moderno)
    if [ ! -f "lib/flatlaf-3.2.5.jar" ]; then
      echo "⬇️  Baixando FlatLaf (tema moderno)..."
      wget -q -O lib/flatlaf-3.2.5.jar \
        https://repo1.maven.org/maven2/com/formdev/flatlaf/3.2.5/flatlaf-3.2.5.jar
      echo "✅ FlatLaf instalado!"
    fi
    
    echo "✨ Pronto! Execute './build.sh' e depois 'java -jar editor-final.jar'"
  '';
}