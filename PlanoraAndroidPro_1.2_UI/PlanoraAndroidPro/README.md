# Planora Android Pro 1.1.0

نسخه حرفه‌ای‌تر Planora برای Android Studio.

## قابلیت‌های مهم
- داشبورد روزانه با پیشرفت واقعی همان روز
- Tasks با توضیحات، اولویت، دسته‌بندی، تاریخ/ساعت، Deadline، تکرار و Reminder
- Subtask قابل تیک‌زدن
- اتصال Task به Goal
- جستجو و فیلتر بدون از دست رفتن فوکوس ورودی
- تقویم ماه/هفته/روز + انتقال Task بین روزها با Drag & Drop در دستگاه‌هایی که Drag دارند
- Time Blocking + جابه‌جایی بلوک‌ها
- دفترچه روزانه: غذا، امتیاز غذا، آب، خواب، Mood، آب‌وهوا، مالی، قدردانی و یادداشت فردا
- Habit Tracker با Score 1-10 و Streak
- Pomodoro و Focus Mode
- Goals
- Smart Planning اولیه بر اساس Task و Habit
- آمار و تحلیل
- Settings، Export/Import JSON و Reset
- LocalStorage برای داده‌های اپ
- Native Android notifications با AlarmManager و زمان‌بندی مجدد بعد از reboot/timezone change
- مجوز Notification در Android 13+
- پشتیبانی از Exact Alarm در صورت مجوز سیستم

## ساخت APK
1. Android Studio را نصب کن و Android SDK/Build Tools 35 را نصب کن.
2. این پوشه را با Android Studio باز کن.
3. Gradle Sync را انجام بده.
4. برای تست روی گوشی: USB Debugging را فعال و Run بزن.
5. برای APK: Build > Build APK(s)
6. برای نسخه انتشار: Build > Generate Signed App Bundle / APK

## نکته
این پروژه از WebView + JavaScript برای رابط کاربری و Bridge بومی Android برای اعلان‌ها، آلارم دقیق، Backup و File Picker استفاده می‌کند.
