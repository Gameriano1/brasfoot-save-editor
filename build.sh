#!/bin/bash

set -e

echo ">>> A limpar builds antigas..."
rm -rf build
rm -rf fatjar_temp
rm -f editor-final.jar

echo ">>> A criar diretórios de trabalho..."
mkdir -p build
mkdir -p fatjar_temp

echo ">>> A compilar o código Java a partir da pasta 'src'..."
javac -d build -cp "lib/*" $(find src -name "*.java")

echo ">>> A criar o ficheiro de manifesto..."
echo "Main-Class: br.com.saveeditor.brasfoot.Main" > manifest.txt

echo ">>> A extrair as dependências para o JAR final..."
cd fatjar_temp
for f in ../lib/*.jar; do
  jar -xf "$f"
done
cd ..

echo ">>> A adicionar o seu código compilado..."
cp -r build/* fatjar_temp/

echo ">>> A criar o Fat JAR final: editor-final.jar..."
jar -cvfm editor-final.jar manifest.txt -C fatjar_temp . > /dev/null

echo ">>> A limpar ficheiros temporários..."
rm -rf build
rm -rf fatjar_temp
rm -f manifest.txt

echo ""
echo "----------------------------------------------------"
echo "✅ SUCESSO! O ficheiro editor-final.jar foi criado."
echo "🚀 Execute com: java -jar editor-final.jar"
echo "----------------------------------------------------"