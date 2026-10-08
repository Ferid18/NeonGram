# 🚀 NeoGram
NeoGram is a high-performance, private third-party Telegram client built on the foundation of Nekogram. This repository is optimized for custom development, featuring tailored modifications for enhanced user experience and security.




- Telegram channel: https://t.me/appneogram
- Downloads: https://t.me/neoapks
- Feedback: https://github.com/Ferid18/NeonGram

## API, Protocol documentation

Telegram API manuals: https://core.telegram.org/api

MTProto protocol manuals: https://core.telegram.org/mtproto

## Compilation Guide

1. Download the Neogram source code ( `git clone https://github.com/Ferid18/NeoGgram.git` )
1. Fill out storeFile, storePassword, keyAlias, keyPassword in local.properties to access your release.keystore
1. Go to https://console.firebase.google.com/, create two android apps with application IDs com.neongroup.neogram and com.neongroup.neogram.beta, turn on firebase messaging and download `google-services.json`, which should be copied into `TMessagesProj_App` folder.
1. Open the project in the Studio (note that it should be opened, NOT imported).
1. Fill out values in `TMessagesProj/src/main/java/com/neongroup/neogram/Extra.java` – there’s a link for each of the variables showing where and which data to obtain.
1. You are ready to compile Neogram.

## Relationship helper bot backend

Neogram profile relationship cards are loaded through the helper bot, not directly from MongoDB in the Android app. The Android side calls Telegram's `bots.invokeWebViewCustomMethod` with the method name `getProfileRelationship`; the helper bot backend must handle that method and return JSON.

### Android config

Add the public helper bot identity to `local.properties` or to your build environment:

```properties
helperBotId=123456789
helperBotUsername=your_helper_bot
helperApiBaseUrl=https://your-backend.example.com
```

These values are read in `TMessagesProj/build.gradle` and exposed as `BuildConfig.HELPER_BOT_ID`, `BuildConfig.HELPER_BOT_USERNAME`, and `BuildConfig.HELPER_API_BASE_URL`. Android first tries Telegram's `bots.invokeWebViewCustomMethod`; if Telegram returns an RPC error or the helper bot identity is not available, it falls back to `HELPER_API_BASE_URL + /api/getProfileRelationship`.

Do not put `BOT_TOKEN` or `MONGO_URI` in Android code. Even if this repo is private, anything shipped inside the APK can be extracted.

### Backend config

Keep backend secrets on the server:

```env
BOT_TOKEN=123456:ABC...
MONGO_URI=mongodb+srv://user:password@cluster/db
```

The backend should use `BOT_TOKEN` to run the helper bot and `MONGO_URI` to read/write relationship data. Android only knows the bot id and username.

### Request from Android

`RelationshipHelper` sends this payload:

```json
{
  "user_id": 123456789
}
```

using method:

```text
getProfileRelationship
```

### Expected backend response

When the user has a public active relationship:

```json
{
  "ok": true,
  "relationship": {
    "partner_user_id": 987654321,
    "type": "romantic",
    "state": "active",
    "started_at": 1710000000,
    "duration_days": 120,
    "public_visible": true,
    "mutually_confirmed": true,
    "note": "optional"
  }
}
```

Supported values:

- `type`: `romantic` or `friend`
- `state`: `pending`, `active`, or `ended`
- `started_at`: Unix seconds or milliseconds
- `partner_user_id`: Telegram user id of the partner

Optional partner display fields are also accepted:

```json
{
  "partner_first_name": "Aylin",
  "partner_last_name": "Aliyeva",
  "partner_display_name": "Aylin Aliyeva",
  "partner_verified": true,
  "partner_premium": true
}
```

If the relationship is private, missing, pending, or ended, the backend can return `null`, `{ "ok": true, "relationship": null }`, or no relationship object. In that case Neogram shows the setup hint only on the current user's own profile; other users' profiles show no relationship block.

### UI behavior

- Own profile without relationship: setup hint is shown.
- Other user's profile without relationship: relationship area is hidden.
- Any profile with active public relationship: relationship card is shown.
- Results are cached in `RelationshipHelper` for about 60 seconds.

## Localization

Neogram is forked from Telegram, thus most locales follows the translations of Telegram for Android, checkout https://translations.telegram.org/en/android/.


