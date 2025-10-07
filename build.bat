@echo off
setlocal

echo.
echo ^>^>^> A limpar builds antigas...
if exist build rmdir /s /q build
if exist fatjar_temp rmdir /s /q fatjar_temp
if exist editor-final.jar del editor-final.jar

echo.
echo ^>^>^> A criar diretorios de trabalho...
mkdir build
mkdir fatjar_temp

echo.
echo ^>^>^> A compilar o codigo Java...
:: Compila o codigo, usando os JARs da pasta 'lib' e colocando o resultado em 'build'
javac -d build -cp "lib\*" EditorInterativo.java
if %errorlevel% neq 0 (
    echo.
    echo ----------------------------------------------------
    echo ERRO: Falha na compilacao do Java.
    echo ----------------------------------------------------
    exit /b 1
)


echo.
echo ^>^>^> A criar o ficheiro de manifesto...
:: Cria o manifesto que aponta para a sua classe principal
echo Main-Class: EditorInterativo > manifest.txt

echo.
echo ^>^>^> A extrair as dependencias para o JAR final...
:: Entra na pasta temporária e extrai o conteúdo de todos os JARs da pasta 'lib'
pushd fatjar_temp
for %%f in (..\lib\*.jar) do (
    jar -xf "%%f"
)
popd

echo.
echo ^>^>^> A adicionar o seu codigo compilado...
:: Copia o codigo compilado para a pasta temporária
xcopy build\* fatjar_temp\ /s /i /y /q

echo.
echo ^>^>^> A criar o Fat JAR final: editor-final.jar...
:: Cria o JAR final, usando o manifesto e todo o conteúdo da pasta temporária
jar -cvfm editor-final.jar manifest.txt -C fatjar_temp . > nul

echo.
echo ^>^>^> A limpar ficheiros temporarios...
:: Apaga as pastas e ficheiros que não são mais necessários
rmdir /s /q build
rmdir /s /q fatjar_temp
del manifest.txt

echo.
echo ----------------------------------------------------
echo SUCESSO! O ficheiro editor-final.jar foi criado.
echo Execute com: java -jar editor-final.jar
echo ----------------------------------------------------
echo.

endlocal
