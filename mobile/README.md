# AI Tutor Mobile

Expo React Native app for iOS, Android, and tablets.

## Run

Node 20.19+ is required because the app targets Expo SDK 54.

```bash
cd mobile
npm install
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080 npm run start
```

This MVP does not support the Expo web target. Do not press `w` in Expo CLI and do not open the Expo bundle in a browser. Use Expo Go, an iOS simulator, an Android emulator, or a native dev build.

`http://localhost:8080` is the Spring Boot API, not the mobile app UI.

If Expo starts Metro and then fails with `TypeError: fetch failed`, run offline mode:

```bash
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080 npm run start:offline
```

The start scripts set `EXPO_NO_DEPENDENCY_VALIDATION=1` so Expo skips remote dependency-version checks. This is useful when network access to Expo services is blocked.

If you see `Unable to resolve manifest assets. Icons and fonts might not work. fetch failed`, Expo is failing a remote manifest asset lookup. Continue with the local bundle first; app icons/fonts can be checked after network access to Expo services works.

Use a LAN IP instead of `localhost` when testing from a physical phone. If Expo remote fetches are working, use LAN mode:

```bash
EXPO_PUBLIC_API_BASE_URL=http://192.168.x.x:8080 npm run start:lan
```

## Hermes Debugging

The app explicitly uses Hermes in `app.json`:

```json
"jsEngine": "hermes"
```

Start Metro, open the app in Expo Go or a native dev build, then press:

```text
j
```

in the Expo CLI terminal to open the JavaScript debugger in Chrome or Edge.

You can also open the developer menu in Expo Go and choose `Open DevTools`.

If the debugger says no compatible app is connected, verify Metro can see the Hermes target:

```bash
curl http://127.0.0.1:8081/json/list
```

If the response is empty, restart Expo using localhost mode for simulator testing:

```bash
EXPO_PUBLIC_API_BASE_URL=http://localhost:8080 npm run start:localhost
```

## MVP Screens

- Local register/login.
- Homework capture.
- Assignment analysis.
- Timeline view.
- Manual checkpoint completion.
- Progress capture.
- In-app focus nudge.
- Spoken focus nudge.
- Ask Tutor hint flow.

The focus nudge is currently driven by local app state. A native ML Kit bridge can replace that trigger later while keeping the same backend `focus-events` API.
