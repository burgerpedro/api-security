@echo off
REM Gera certificado autoassinado para desenvolvimento

echo Gerando certificado SSL...

keytool -genkeypair ^
    -alias apigateway ^
    -keyalg RSA ^
    -keysize 2048 ^
    -storetype PKCS12 ^
    -keystore src\main\resources\keystore.p12 ^
    -validity 3650 ^
    -dname "CN=localhost, OU=Seguranca, O=EscalacaoTech, L=Cidade, ST=Estado, C=BR" ^
    -storepass senha123 ^
    -keypass senha123

echo.
echo Certificado gerado em: src\main\resources\keystore.p12
echo Senha do keystore: senha123
pause