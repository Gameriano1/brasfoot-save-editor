#!/bin/bash

# Este comando faz o script parar imediatamente se algum comando falhar. É uma boa prática.
set -e

echo ">>> A limpar builds antigas..."
# Remove as pastas e ficheiros de builds anteriores para garantir um build limpo
rm -rf build
rm -rf fatjar_temp
rm -f editor-final.jar

echo ">>> A criar diretórios de trabalho..."
mkdir build
mkdir fatjar_temp

echo ">>> A compilar o código Java..."
# Compila o seu código, usando os JARs da pasta 'lib' e colocando o resultado em 'build'
javac -d build -cp "lib/*" EditorInterativo.java

echo ">>> A criar o ficheiro de manifesto..."
# Cria o manifesto que aponta para a sua classe principal
echo "Main-Class: EditorInterativo" > manifest.txt

echo ">>> A extrair as dependências para o JAR final..."
# Entra na pasta temporária e extrai o conteúdo de todos os JARs da pasta 'lib'
cd fatjar_temp
for f in ../lib/*.jar; do
  jar -xf "$f"
done
cd ..

echo ">>> A adicionar o seu código compilado..."
# Copia o seu código compilado para a pasta temporária
cp -r build/* fatjar_temp/

echo ">>> A criar o Fat JAR final: editor-final.jar..."
# Cria o JAR final, usando o manifesto e todo o conteúdo da pasta temporária
jar -cvfm editor-final.jar manifest.txt -C fatjar_temp .

echo ">>> A limpar ficheiros temporários..."
# Apaga as pastas e ficheiros que não são mais necessários
rm -rf build
rm -rf fatjar_temp
rm -f manifest.txt

echo ""
echo "----------------------------------------------------"
echo "SUCESSO! O ficheiro editor-final.jar foi criado."
echo "Execute com: java -jar editor-final.jar"
echo "----------------------------------------------------"