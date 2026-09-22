# YOMY Countdown

A standalone Android countdown app for the YOMY launch moment.

## Current target
- Countdown start: **17 September 2026, 00:00 Cairo time**
- Launch target: **24 September 2026, 00:00 Cairo time**
- Offline after installation
- Live days / hours / minutes / seconds

## Build
GitHub Actions automatically builds a debug APK on every push to `main` and on manual workflow dispatch.

Artifact: `YOMY-countdown-debug`

## Change the date
Edit the two `dateMillis(...)` values in `app/src/main/java/com/yomy/countdown/MainActivity.java` and push to `main`.

## Project
Application ID: `com.yomy.countdown`
Version: `1.1`
