# YOMY Secrets & Services Contract

## Safe in the Android app
- Supabase project URL.
- Supabase publishable/anon key.
- A push provider public application ID, if documented as public.

## NEVER ship in the Android APK
- Supabase service-role/secret key.
- Vonage API secret/private key.
- OneSignal REST API key.
- Firebase service-account JSON/private key.
- Android signing passwords/keystores.
- Webhook signing secrets.

## Notification architecture
1. Android creates YOMY notification channels on startup.
2. A remote provider (FCM/OneSignal/etc.) may deliver a payload.
3. The provider adapter hands the payload to NotificationCenter.
4. NotificationCenter chooses channel, title/body, priority and notification ID.
5. Sleep Mode/privacy rules are applied before non-critical notifications.

## Phone verification
Phone verification must be server-side:
app -> Edge Function -> SMS provider -> Edge Function -> app.
Provider credentials remain in the trusted Edge Function/server and never enter the APK.

## GitHub Actions
Public client variables:
- YOMY_ENVIRONMENT
- SUPABASE_URL
- SUPABASE_PUBLISHABLE_KEY
- PUSH_PROVIDER
- PUSH_APP_ID

Private credentials belong in GitHub Actions Secrets or server-side Edge Functions only. Never print them in logs.

## Environments
- development
- staging
- production

Use isolated Supabase/push projects per environment where possible.
