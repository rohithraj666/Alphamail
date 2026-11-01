# Alphamail (UI prototype)

This is the first Android UI milestone for Alphamail, inspired by the user-provided LightMail screenshots.

## Included
- Three home views: All emails, Conversations, Senders
- All Emails: individual message rows with a dedicated inline Preview/Hide control
- Conversations: grouped by thread identifier in the sample data
- Senders: grouped by sender email address in the sample data
- LightMail-inspired message bubbles; tapping a message opens its original-message screen
- Dark, Light and warm Readable themes
- Bottom navigation shell and GitHub Actions debug APK workflow

## Important limitations
This milestone uses local sample messages only. It does NOT yet connect to Gmail, Yahoo, Outlook or IMAP/SMTP, and does not send or receive real mail. Provider authentication, secure token storage, synchronisation, real MIME/HTML rendering, attachments, compose/reply/forward, folder operations and true RFC email-thread parsing are later milestones.

## Build
Open in Android Studio with JDK 17 and Android Gradle Plugin support, or push to GitHub and run the `Build Alphamail APK` workflow.
