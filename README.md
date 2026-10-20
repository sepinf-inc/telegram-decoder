# telegram-decoder

Telegram decoder plugin used by the [IPED](https://github.com/sepinf-inc/IPED) project
to parse the serialized objects (blobs) stored in the database of Telegram for Android.

The parsing code is the `org.telegram.tgnet` package copied from the official client
[DrKLO/Telegram](https://github.com/DrKLO/Telegram), compiled against minimal stubs of
the Android/app classes it depends on.

Modules and licenses:

- `telegram-decoder-api`: interfaces used by IPED — Apache v2
- `telegram-decoder-impl`: upstream `tgnet` code, stubs and `DecoderTelegram` — GNU GPL v3

## Build

Requires JDK 11+ and Maven:

```sh
mvn clean install
```

## Branches

- `main`: released code (upstream `tgnet` + local customizations).
- `DrKLO/Telegram`: pristine copy of upstream `tgnet`. Never edit it by hand.
- `update_1.0.x`: branch where `DrKLO/Telegram` is merged into `main`.

## Local customizations

Changes made to upstream files in `org/telegram/tgnet` (check them after each merge):

- `ConnectionsManager.java` removed; `TextUtils.java` added.
- `TLParseException.java`: `doThrowOrLog` body commented out.
- `TLRPC.java`: PotatoChat support (`TL_user_potato` and message constructors
  `0x41df7460`, `0x8c75c832`); `TranslateController` usage removed.
- `json/TLJsonBuilder.java`, `json/TLJsonParser.java`: `BuildConfig` checks removed.
- `tl/TL_stars.java`: `PaidReactionPrivacy.getDialogId()` returns `0`.
- `tl/TL_stories.java`: `StoryPrivacyBottomSheet` usage removed.

Prefer adding a stub over editing an upstream file: each edit is a future merge conflict.

## Upgrade

1. Clone or update the upstream project in a separate folder:

```sh
cd ..
git clone https://github.com/DrKLO/Telegram.git   # or: cd Telegram && git pull origin master
cd Telegram
grep -E 'APP_VERSION_(NAME|CODE)' gradle.properties  # version for the commit message
git log -1 --format=%h                               # commit for the commit message
```

2. Copy the upstream files into the `DrKLO/Telegram` branch:

```sh
cd ../telegram-decoder
git checkout DrKLO/Telegram
git pull origin DrKLO/Telegram

rm -rf telegram-decoder-impl/src/main/java/org/telegram/tgnet/*
cp -r ../Telegram/TMessagesProj/src/main/java/org/telegram/tgnet/* telegram-decoder-impl/src/main/java/org/telegram/tgnet/

git add -A
git commit -m "update to x.x.x (xxxx) DrKLO/Telegram@xxxxxxx"
git push origin DrKLO/Telegram
```

3. Merge it into a new branch and set the version `1.0.xx-SNAPSHOT` in the three `pom.xml`:

```sh
git checkout main
git pull origin main
git checkout -b update_1.0.xx
git merge DrKLO/Telegram
```

4. Resolve the conflicts, keeping the upstream code and re-applying the local
   customizations listed above. Before committing:
   - delete merge leftovers (`*.orig`, `*_BACKUP_*`, `*_BASE_*`, `*_LOCAL_*`, `*_REMOTE_*`):
     they break the build;
   - run `mvn clean compile`. For each new class referenced by upstream code
     (`cannot find symbol`), add a stub with the same package and signature
     under `telegram-decoder-impl/src/main/java`, keeping upstream values for
     flags/helpers used while parsing.

5. Test with IPED, commit, push and open a pull request to `main`.
