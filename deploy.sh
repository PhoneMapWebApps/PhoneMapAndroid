curl \
-F "status=2" \
-F "notify=1" \
-F "ipa=@app/build/outputs/apk/app-debug.apk" \
-H "X-HockeyAppToken: $HockeyAppToken" \
https://rink.hockeyapp.net/api/2/apps/$AndroidAppId/app_versions/upload