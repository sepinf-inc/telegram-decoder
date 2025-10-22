# telegram-decoder
Telegram decoder plugin used by IPED project.

Licenses:

- telegram-decoder-api: Apache v2

- telegram-decoder-impl: GNU GPL v3


# Upgrade

1. Clone the project https://github.com/DrKLO/Telegram in a separated folder:

```sh
cd ..
git clone https://github.com/DrKLO/Telegram.git
```

2. Optionally get latest changes from GitHub repository:

```sh
cd Telegram
git checkout master
git pull origin master
```

3. Go to this project and copy the files in branch `DrKLO/Telegram`:

```sh 
cd ../telegram-decoder
git checkout DrKLO/Telegram
git pull origin DrKLO/Telegram

rm -rf telegram-decoder-impl/src/main/java/org/telegram/tgnet/*
cp -r ../Telegram/TMessagesProj/src/main/java/org/telegram/tgnet/* telegram-decoder-impl/src/main/java/org/telegram/tgnet/

