# Message Test Tool — Android

Versão Android com **bolinha flutuante** + digitação via **Serviço de Acessibilidade**.

## O que faz

- Mostra uma bolinha flutuante na tela
- Ao tocar na bolinha, abre um painel de controle
- Você configura mensagem, quantidade e intervalo
- O app tenta digitar a mensagem no campo de texto que estiver focado (WhatsApp, Discord, etc.)

## Limitações importantes

- Precisa ativar **duas permissões** (Overlay + Acessibilidade)
- Nem todos os apps aceitam bem a injeção de texto
- Alguns apps (especialmente bancos e alguns mensageiros) bloqueiam Acessibilidade
- Não é 100% confiável como a versão de Windows

## Como compilar

1. Instale o **Android Studio** (última versão estável)
2. Abra esta pasta como projeto
3. Espere o Gradle sincronizar
4. Conecte um celular com **Depuração USB** ativada **ou** use um emulador
5. Clique em **Run** (▶)

### Gerar APK

- **Build → Build Bundle(s) / APK(s) → Build APK(s)**
- O APK fica em `app/build/outputs/apk/debug/`

## Como usar no celular

1. Abra o app
2. Toque em **“Permitir janela flutuante”** e autorize
3. Toque em **“Ativar Acessibilidade”** → procure **Message Test Tool** e ative
4. Toque em **“Abrir bolinha flutuante”**
5. Abra o app de destino (WhatsApp, Discord…)
6. Toque no campo de texto onde quer digitar
7. Toque na bolinha → configure → **Iniciar**

## Aviso

Ferramentas que usam Acessibilidade têm poder alto.  
Use apenas em contextos legítimos e com responsabilidade.
