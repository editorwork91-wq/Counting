package com.yomy.counting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MainActivity extends Activity {
    private YomyView view;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Window w = getWindow();
        w.setStatusBarColor(Color.rgb(10, 13, 20));
        w.setNavigationBarColor(Color.rgb(10, 13, 20));
        if (Build.VERSION.SDK_INT >= 23) {
            w.getDecorView().setSystemUiVisibility(0);
        }
        view = new YomyView(this);
        setContentView(view);
    }

    @Override
    public void onBackPressed() {
        if (view.handleBack()) return;
        super.onBackPressed();
    }

    public static final class YomyView extends View {
        private static final int BG = Color.rgb(9, 12, 19);
        private static final int TEXT = Color.rgb(246, 248, 252);
        private static final int MUTED = Color.rgb(151, 160, 178);
        private static final int LINE = Color.argb(34, 255, 255, 255);
        private static final int GLASS = Color.argb(34, 255, 255, 255);
        private static final int GLASS_STRONG = Color.argb(54, 255, 255, 255);

        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF r = new RectF();
        private final Path path = new Path();
        private final Handler handler = new Handler(Looper.getMainLooper());

        private String screen = "home";
        private String tab = "chats";
        private boolean menuOpen = false;
        private boolean editorOpen = false;
        private boolean dark = true;
        private boolean arabic = false;
        private boolean sleep = false;
        private int fontScale = 1;
        private boolean composerFocused = false;
        private String draft = "";
        private String toast = "";
        private long toastUntil = 0L;
        private float downX, downY;
        private float settingsScroll = 0f;
        private boolean dragging = false;
        private float scrollStart = 0f;
        private float initialScroll = 0f;

        private final List<ChatItem> chats = new ArrayList<>();

        public YomyView(Context c) {
            super(c);
            setFocusable(true);
            p.setStrokeCap(Paint.Cap.ROUND);
            chats.add(new ChatItem("S", "Sami", "The glass is looking good.", "4m", 2, true));
            chats.add(new ChatItem("M", "Mariam", "Send me the new draft ✨", "18m", 0, false));
            chats.add(new ChatItem("Y", "YOMY Team", "Fedo is ready to explore.", "1h", 0, false));
            chats.add(new ChatItem("A", "Ahmed", "Voice message", "2h", 0, false));
            chats.add(new ChatItem("L", "Layla", "See you tomorrow.", "Yesterday", 0, false));
        }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            c.drawColor(BG);
            drawAmbient(c);
            if ("chat".equals(screen)) {
                drawChat(c);
            } else if ("profile".equals(screen)) {
                drawProfile(c);
            } else if ("settings".equals(screen)) {
                drawSettings(c);
            } else {
                drawHome(c);
            }
            if (menuOpen) drawActionMenu(c);
            if (editorOpen) drawEditor(c);
            if (!toast.isEmpty() && System.currentTimeMillis() < toastUntil) {
                drawToast(c);
                handler.postDelayed(this::invalidate, 120);
            }
        }

        private void drawAmbient(Canvas c) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(34, 120, 150, 255));
            c.drawCircle(getWidth() * .86f, dp(55), dp(150), p);
            p.setColor(Color.argb(22, 190, 120, 255));
            c.drawCircle(getWidth() * .08f, getHeight() * .55f, dp(170), p);
        }

        private void drawHome(Canvas c) {
            drawHeader(c, false);
            if ("chats".equals(tab)) drawChats(c);
            else if ("fedo".equals(tab)) drawFedo(c);
            else drawProfile(c);
            drawBottomNav(c);
        }

        private void drawHeader(Canvas c, boolean back) {
            float top = dp(16);
            if (back) {
                glass(c, dp(14), top, dp(52), top + dp(44), dp(18), GLASS);
                iconBack(c, dp(40), top + dp(22));
            } else {
                avatar(c, dp(40), top + dp(22), dp(17), "J");
            }

            float cx = getWidth() / 2f;
            glass(c, cx - dp(68), top, cx + dp(68), top + dp(44), dp(22), GLASS);
            drawLogoMark(c, cx - dp(39), top + dp(22), dp(12));
            text(c, "YOMY", cx + dp(14), top + dp(29), 19, true, TEXT);

            glass(c, getWidth() - dp(66), top, getWidth() - dp(14), top + dp(44), dp(18), GLASS);
            if ("chat".equals(screen)) iconMore(c, getWidth() - dp(40), top + dp(22));
            else iconSearch(c, getWidth() - dp(40), top + dp(22));
        }

        private void drawChats(Canvas c) {
            float y = dp(78);
            text(c, arabic ? "المحادثات" : "Chats", dp(20), y + dp(25), 28 * fontScale, true, TEXT, Paint.Align.LEFT);
            text(c, arabic ? "سريعة، هادئة، ومزامنة" : "Fast, calm, in sync", dp(20), y + dp(52), 13 * fontScale, false, MUTED, Paint.Align.LEFT);

            glass(c, dp(20), y + dp(70), getWidth() - dp(20), y + dp(116), dp(20), GLASS);
            iconSearch(c, dp(44), y + dp(93));
            text(c, arabic ? "ابحث في المحادثات" : "Search conversations", dp(64), y + dp(99), 14 * fontScale, false, MUTED, Paint.Align.LEFT);

            float rowY = y + dp(128);
            glass(c, dp(20), rowY - dp(6), getWidth() - dp(20), rowY + dp(54) + chats.size() * dp(68), dp(28), Color.argb(20,255,255,255));
            int index = 0;
            for (ChatItem item : chats) {
                drawChatRow(c, item, rowY + index * dp(68));
                index++;
            }
            glass(c, getWidth() - dp(74), getHeight() - dp(122), getWidth() - dp(20), getHeight() - dp(68), dp(27), GLASS_STRONG);
            iconPlus(c, getWidth() - dp(47), getHeight() - dp(95));
        }

        private void drawChatRow(Canvas c, ChatItem item, float y) {
            avatar(c, dp(50), y + dp(28), dp(22), item.initial);
            text(c, item.name, dp(84), y + dp(21), 16 * fontScale, true, TEXT, Paint.Align.LEFT);
            text(c, item.last, dp(84), y + dp(44), 13 * fontScale, false, MUTED, Paint.Align.LEFT);
            text(c, item.time, getWidth() - dp(28), y + dp(19), 11 * fontScale, false, MUTED, Paint.Align.RIGHT);
            if (item.unread > 0) {
                glass(c, getWidth() - dp(38), y + dp(32), getWidth() - dp(20), y + dp(50), dp(9), Color.argb(82,255,255,255));
                text(c, String.valueOf(item.unread), getWidth() - dp(29), y + dp(45), 9, true, BG);
            } else if (item.sent) {
                iconChecks(c, getWidth() - dp(35), y + dp(41));
            }
            p.setColor(LINE); p.setStrokeWidth(1);
            c.drawLine(dp(84), y + dp(67), getWidth() - dp(20), y + dp(67), p);
        }

        private void drawFedo(Canvas c) {
            float y = dp(78);
            text(c, arabic ? "فِيدو" : "Fedo", dp(20), y + dp(25), 28 * fontScale, true, TEXT, Paint.Align.LEFT);
            text(c, arabic ? "لحظاتك، بخصوصية وعلى إيقاعك" : "Moments, private by design", dp(20), y + dp(52), 13 * fontScale, false, MUTED, Paint.Align.LEFT);

            float railY = y + dp(78);
            drawStoryAvatar(c, dp(54), railY + dp(24), "J", "You", true);
            drawStoryAvatar(c, dp(124), railY + dp(24), "S", "Sami", false);
            drawStoryAvatar(c, dp(194), railY + dp(24), "M", "Mariam", true);
            drawStoryAvatar(c, dp(264), railY + dp(24), "A", "Ahmed", false);

            float cardTop = railY + dp(72);
            drawFedoCard(c, cardTop, "Sami", "12m ago", "Night drive • 00:18", "S", false);
            drawFedoCard(c, cardTop + dp(155), "Mariam", "1h ago", "Coffee & quiet mornings", "M", true);

            glass(c, getWidth() - dp(74), getHeight() - dp(122), getWidth() - dp(20), getHeight() - dp(68), dp(27), GLASS_STRONG);
            iconPlus(c, getWidth() - dp(47), getHeight() - dp(95));
        }

        private void drawStoryAvatar(Canvas c, float x, float y, String initial, String label, boolean own) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2.2f));
            p.setColor(own ? Color.WHITE : Color.argb(120,255,255,255));
            c.drawCircle(x, y, dp(30), p);
            p.setStyle(Paint.Style.FILL);
            avatar(c, x, y, dp(24), initial);
            text(c, label, x, y + dp(48), 10 * fontScale, false, MUTED);
        }

        private void drawFedoCard(Canvas c, float top, String name, String time, String title, String initial, boolean liked) {
            glass(c, dp(20), top, getWidth() - dp(20), top + dp(140), dp(26), Color.argb(30,255,255,255));
            // Thumbnail-first design: a lightweight precomposed surface, no heavy blur dependency.
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(42,255,255,255));
            r.set(dp(30), top + dp(12), getWidth() - dp(30), top + dp(88));
            c.drawRoundRect(r, dp(20), dp(20), p);
            text(c, "YOMY", dp(48), top + dp(52), 11, true, Color.argb(150,255,255,255), Paint.Align.LEFT);
            avatar(c, dp(50), top + dp(111), dp(20), initial);
            text(c, name, dp(80), top + dp(107), 14, true, TEXT, Paint.Align.LEFT);
            text(c, time, dp(80), top + dp(127), 10, false, MUTED, Paint.Align.LEFT);
            glass(c, getWidth() - dp(112), top + dp(101), getWidth() - dp(30), top + dp(134), dp(16), GLASS);
            iconHeart(c, getWidth() - dp(91), top + dp(117), liked);
            text(c, "Like", getWidth() - dp(69), top + dp(121), 10, false, TEXT, Paint.Align.LEFT);
            text(c, title, getWidth() - dp(31), top + dp(79), 12, true, TEXT, Paint.Align.RIGHT);
        }

        private void drawProfile(Canvas c) {
            drawHeader(c, false);
            float y = dp(82);
            avatar(c, getWidth()/2f, y + dp(55), dp(42), "J");
            text(c, "John", getWidth()/2f, y + dp(113), 24 * fontScale, true, TEXT);
            text(c, "@jomy", getWidth()/2f, y + dp(138), 13 * fontScale, false, MUTED);
            glass(c, dp(20), y + dp(160), getWidth() - dp(20), y + dp(220), dp(24), GLASS);
            stat(c, dp(65), y + dp(190), "24", arabic ? "فِيدو" : "Fedo");
            stat(c, getWidth()/2f, y + dp(190), "86", arabic ? "متابع" : "Friends");
            stat(c, getWidth() - dp(65), y + dp(190), "12", arabic ? "متابعة" : "Following");

            actionRow(c, y + dp(246), arabic ? "تعديل الملف" : "Edit profile", "edit");
            actionRow(c, y + dp(308), arabic ? "الإعدادات" : "Settings", "settings");
            actionRow(c, y + dp(370), arabic ? "الخصوصية" : "Privacy", "lock");
            actionRow(c, y + dp(432), arabic ? "المحظورون" : "Blocked", "shield");
        }

        private void drawSettings(Canvas c) {
            drawHeader(c, true);
            float y = dp(82) - settingsScroll;
            text(c, arabic ? "الإعدادات" : "Settings", dp(20), y + dp(24), 28 * fontScale, true, TEXT, Paint.Align.LEFT);
            text(c, arabic ? "تحكم في تجربتك" : "Control your experience", dp(20), y + dp(50), 13 * fontScale, false, MUTED, Paint.Align.LEFT);

            settingCard(c, y + dp(78), arabic ? "الوضع الداكن" : "Dark mode", arabic ? "مظهر زجاجي أعمق" : "Deeper glass appearance", dark, true);
            settingCard(c, y + dp(142), arabic ? "وضع النوم" : "Sleep mode", arabic ? "10م — 5ص بالتوقيت المحلي" : "10 PM — 5 AM local time", sleep, true);
            selectorCard(c, y + dp(206), arabic ? "حجم الخط" : "Font size", fontScale == 1 ? "Default" : (fontScale == 2 ? "Large" : "Small"));
            selectorCard(c, y + dp(270), arabic ? "اللغة" : "Language", arabic ? "العربية" : "English");
            actionRow(c, y + dp(340), arabic ? "الإشعارات" : "Notifications", "bell");
            actionRow(c, y + dp(402), arabic ? "الأمان" : "Security", "lock");
            actionRow(c, y + dp(464), arabic ? "التخزين" : "Storage", "box");
            actionRow(c, y + dp(526), arabic ? "حول YOMY" : "About YOMY", "info");
            text(c, "v1.0 • Glass foundation", dp(20), y + dp(600), 11, false, MUTED, Paint.Align.LEFT);
        }

        private void settingCard(Canvas c, float top, String title, String sub, boolean on, boolean toggle) {
            glass(c, dp(20), top, getWidth()-dp(20), top+dp(54), dp(19), GLASS);
            text(c, title, dp(36), top+dp(23), 14*fontScale, true, TEXT, Paint.Align.LEFT);
            text(c, sub, dp(36), top+dp(42), 10*fontScale, false, MUTED, Paint.Align.LEFT);
            toggle(c, getWidth()-dp(52), top+dp(27), on);
        }

        private void selectorCard(Canvas c, float top, String title, String value) {
            glass(c, dp(20), top, getWidth()-dp(20), top+dp(54), dp(19), GLASS);
            text(c, title, dp(36), top+dp(23), 14*fontScale, true, TEXT, Paint.Align.LEFT);
            text(c, value, getWidth()-dp(38), top+dp(32), 11, false, MUTED, Paint.Align.RIGHT);
        }

        private void drawChat(Canvas c) {
            drawHeader(c, true);
            float y = dp(78);
            avatar(c, dp(53), y+dp(18), dp(21), "S");
            text(c, "Sami", dp(84), y+dp(14), 16*fontScale, true, TEXT, Paint.Align.LEFT);
            text(c, "online now", dp(84), y+dp(34), 11*fontScale, false, MUTED, Paint.Align.LEFT);

            messageBubble(c, dp(20), dp(150), dp(250), dp(84), "Hey! This glass direction feels calm.", false, "4:02");
            messageBubble(c, getWidth()-dp(270), dp(246), dp(250), dp(64), "Exactly. Fast, not flashy.", true, "4:03");
            messageBubble(c, dp(20), dp(326), dp(288), dp(88), "I also kept the thumbnail-first feed.", false, "4:04");
            messageBubble(c, getWidth()-dp(286), dp(430), dp(266), dp(64), draft.isEmpty() ? "Nice. Send the build when ready." : draft, true, "4:05");

            float composerBottom = getHeight() - dp(82);
            glass(c, dp(14), composerBottom, getWidth()-dp(14), composerBottom+dp(58), dp(29), GLASS_STRONG);
            glass(c, dp(22), composerBottom+dp(9), dp(62), composerBottom+dp(49), dp(20), GLASS);
            iconPlus(c, dp(42), composerBottom+dp(29));
            text(c, draft.isEmpty() ? (arabic ? "اكتب رسالة" : "Message") : draft,
                    dp(76), composerBottom+dp(36), 14*fontScale, false,
                    draft.isEmpty() ? MUTED : TEXT, Paint.Align.LEFT);
            glass(c, getWidth()-dp(62), composerBottom+dp(9), getWidth()-dp(22), composerBottom+dp(49), dp(20), GLASS);
            iconSend(c, getWidth()-dp(42), composerBottom+dp(29));
        }

        private void messageBubble(Canvas c, float x, float y, float w, float h, String msg, boolean out, String time) {
            glass(c, x, y, x+w, y+h, dp(20), out ? Color.argb(56,255,255,255) : Color.argb(28,255,255,255));
            text(c, msg, x+dp(16), y+dp(25), 14*fontScale, false, TEXT, Paint.Align.LEFT);
            text(c, time, x+w-dp(12), y+h-dp(10), 9, false, MUTED, Paint.Align.RIGHT);
            if (out) iconChecks(c, x+w-dp(35), y+h-dp(11));
        }

        private void drawBottomNav(Canvas c) {
            float top = getHeight() - dp(64);
            glass(c, dp(24), top, getWidth()-dp(24), getHeight()-dp(10), dp(27), GLASS_STRONG);
            navItem(c, dp(72), top+dp(25), "chats".equals(tab), "Chats");
            navItem(c, getWidth()/2f, top+dp(25), "fedo".equals(tab), "Fedo");
            navItem(c, getWidth()-dp(72), top+dp(25), "profile".equals(tab), "Profile");
        }

        private void navItem(Canvas c, float x, float y, boolean active, String label) {
            if (active) {
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.argb(48,255,255,255));
                c.drawCircle(x, y-dp(2), dp(22), p);
            }
            if ("Chats".equals(label)) iconChat(c,x,y-1,active);
            else if ("Fedo".equals(label)) iconSpark(c,x,y-1,active);
            else iconPerson(c,x,y-1,active);
            if (active) text(c,label,x,y+dp(18),9,true,TEXT);
        }

        private void drawActionMenu(Canvas c) {
            float bottom = getHeight()-dp(78);
            glass(c, dp(30), bottom-dp(250), getWidth()-dp(30), bottom, dp(30), Color.argb(68,20,24,34));
            menuItem(c, bottom-dp(210), "Gallery", "gallery");
            menuItem(c, bottom-dp(158), "Camera", "camera");
            menuItem(c, bottom-dp(106), "File", "file");
            menuItem(c, bottom-dp(54), "Drawing", "draw");
        }

        private void menuItem(Canvas c, float y, String label, String kind) {
            float x = dp(52);
            if ("gallery".equals(kind)) iconImage(c,x,y);
            else if ("camera".equals(kind)) iconCamera(c,x,y);
            else if ("file".equals(kind)) iconFile(c,x,y);
            else iconPen(c,x,y);
            text(c,label,x+dp(28),y+dp(5),13,true,TEXT,Paint.Align.LEFT);
        }

        private void drawEditor(Canvas c) {
            // Full-screen glass editor overlay; preview remains dominant.
            p.setColor(Color.argb(165,4,7,13));
            c.drawRect(0,0,getWidth(),getHeight(),p);
            glass(c, dp(18), dp(20), getWidth()-dp(18), getHeight()-dp(20), dp(30), Color.argb(42,255,255,255));
            r.set(dp(48),dp(90),getWidth()-dp(48),getHeight()-dp(168));
            p.setStyle(Paint.Style.FILL); p.setColor(Color.argb(28,255,255,255)); c.drawRoundRect(r,dp(30),dp(30),p);
            text(c,"YOMY",getWidth()/2f,dp(150),24,true,TEXT);
            text(c,arabic ? "المعاينة الرئيسية" : "Preview",getWidth()/2f,dp(178),12,false,MUTED);

            String[] tools = {"Edit","Filters","Text","Music","24h"};
            for(int i=0;i<tools.length;i++){
                float yy=dp(150)+i*dp(62);
                glass(c,dp(24),yy-dp(21),dp(112),yy+dp(21),dp(20),i==4?GLASS_STRONG:GLASS);
                text(c,tools[i],dp(68),yy+dp(5),11,true,TEXT);
            }

            glass(c,dp(42),getHeight()-dp(112),dp(128),getHeight()-dp(62),dp(24),GLASS);
            text(c,"Discard",dp(85),getHeight()-dp(82),12,true,TEXT);

            glass(c,getWidth()-dp(164),getHeight()-dp(112),getWidth()-dp(42),getHeight()-dp(62),dp(24),GLASS_STRONG);
            text(c,"Publish",getWidth()-dp(103),getHeight()-dp(82),12,true,TEXT);
        }

        private void drawToast(Canvas c) {
            float w = Math.min(getWidth()-dp(44), dp(310));
            float x = (getWidth()-w)/2f;
            float y = getHeight()-dp(138);
            glass(c,x,y,x+w,y+dp(44),dp(22),GLASS_STRONG);
            text(c,toast,getWidth()/2f,y+dp(28),12,true,TEXT);
        }

        private void actionRow(Canvas c, float y, String label, String kind) {
            glass(c,dp(20),y,getWidth()-dp(20),y+dp(54),dp(19),GLASS);
            float x=dp(42);
            if("settings".equals(kind)) iconGear(c,x,y+dp(27));
            else if("lock".equals(kind)) iconLock(c,x,y+dp(27));
            else if("shield".equals(kind)) iconShield(c,x,y+dp(27));
            else if("bell".equals(kind)) iconBell(c,x,y+dp(27));
            else if("box".equals(kind)) iconBox(c,x,y+dp(27));
            else if("info".equals(kind)) iconInfo(c,x,y+dp(27));
            else iconEdit(c,x,y+dp(27));
            text(c,label,dp(68),y+dp(33),14*fontScale,true,TEXT,Paint.Align.LEFT);
            iconChevron(c,getWidth()-dp(40),y+dp(27));
        }

        private void stat(Canvas c,float x,float y,String n,String label){
            text(c,n,x,y,18,true,TEXT);
            text(c,label,x,y+dp(19),10,false,MUTED);
        }

        private void toggle(Canvas c,float x,float y,boolean on){
            p.setStyle(Paint.Style.FILL); p.setColor(on?Color.argb(130,255,255,255):Color.argb(42,255,255,255));
            r.set(x-dp(25),y-dp(14),x+dp(25),y+dp(14)); c.drawRoundRect(r,dp(14),dp(14),p);
            p.setColor(on?BG:Color.argb(210,255,255,255)); c.drawCircle(x+(on?dp(11):-dp(11)),y,dp(9),p);
        }

        private void glass(Canvas c,float l,float t,float rr,float b,float rad,int color){
            p.setStyle(Paint.Style.FILL); p.setColor(color); r.set(l,t,rr,b); c.drawRoundRect(r,rad,rad,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); p.setColor(Color.argb(36,255,255,255)); c.drawRoundRect(r,rad,rad,p);
            p.setStyle(Paint.Style.FILL);
        }

        private void avatar(Canvas c,float x,float y,float radius,String initial){
            p.setStyle(Paint.Style.FILL); p.setColor(Color.argb(74,255,255,255)); c.drawCircle(x,y,radius,p);
            text(c,initial,x,y+radius*.34f, radius*.82f,true,TEXT);
        }

        private void drawLogoMark(Canvas c,float x,float y,float s){
            p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE);
            path.reset();
            path.moveTo(x-s,y-s*.72f); path.cubicTo(x-s*1.1f,y-s*1.05f,x+s*1.1f,y-s*1.05f,x+s,y-s*.72f);
            path.lineTo(x+s,y+s*.55f); path.cubicTo(x+s,y+s*.95f,x+s*.55f,y+s,x+s*.05f,y+s);
            path.lineTo(x-s*.46f,y+s*1.28f); path.lineTo(x-s*.46f,y+s); path.lineTo(x-s*.05f,y+s);
            path.cubicTo(x-s*.55f,y+s,x-s,y+s*.55f,x-s,y-s*.72f);
            c.drawPath(path,p);
            p.setColor(BG); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.8f));
            c.drawLine(x-s*.32f,y-s*.34f,x-s*.06f,y+s*.24f,p); c.drawLine(x-s*.06f,y+s*.24f,x+s*.28f,y-s*.34f,p);
            p.setStyle(Paint.Style.FILL);
        }

        private void text(Canvas c,String v,float x,float y,float size,boolean bold,int color){
            text(c,v,x,y,size,bold,color,Paint.Align.CENTER);
        }
        private void text(Canvas c,String v,float x,float y,float size,boolean bold,int color,Paint.Align align){
            p.setStyle(Paint.Style.FILL); p.setColor(color); p.setTextSize(sp(size)); p.setTextAlign(align);
            p.setTypeface(android.graphics.Typeface.create("sans",bold?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL));
            c.drawText(v,x,y,p);
        }

        private float sp(float value){ return value * getResources().getDisplayMetrics().scaledDensity; }
        private float dp(float value){ return value * getResources().getDisplayMetrics().density; }

        private void strokeSetup(){
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1.8f)); p.setColor(Color.argb(200,255,255,255));
        }

        private void iconBack(Canvas c,float x,float y){strokeSetup();c.drawLine(x+dp(7),y-dp(8),x-dp(5),y,p);c.drawLine(x-dp(5),y,x+dp(7),y+dp(8),p);}
        private void iconMore(Canvas c,float x,float y){p.setStyle(Paint.Style.FILL);p.setColor(TEXT);c.drawCircle(x-dp(5),y,dp(2),p);c.drawCircle(x,y,dp(2),p);c.drawCircle(x+dp(5),y,dp(2),p);}
        private void iconSearch(Canvas c,float x,float y){strokeSetup();c.drawCircle(x-dp(2),y-dp(2),dp(7),p);c.drawLine(x+dp(4),y+dp(4),x+dp(10),y+dp(10),p);}
        private void iconPlus(Canvas c,float x,float y){strokeSetup();c.drawLine(x-dp(8),y,x+dp(8),y,p);c.drawLine(x,y-dp(8),x,y+dp(8),p);}
        private void iconSend(Canvas c,float x,float y){strokeSetup();path.reset();path.moveTo(x-dp(9),y-dp(7));path.lineTo(x+dp(10),y);path.lineTo(x-dp(9),y+dp(7));path.close();c.drawPath(path,p);c.drawLine(x-dp(5),y,x+dp(5),y,p);}
        private void iconChecks(Canvas c,float x,float y){strokeSetup();c.drawLine(x-dp(9),y,x-dp(5),y+dp(4),p);c.drawLine(x-dp(5),y+dp(4),x+dp(1),y-dp(4),p);c.drawLine(x-dp(2),y+dp(1),x+dp(2),y+dp(5),p);c.drawLine(x+dp(2),y+dp(5),x+dp(8),y-dp(3),p);}
        private void iconHeart(Canvas c,float x,float y,boolean filled){p.setStyle(filled?Paint.Style.FILL:Paint.Style.STROKE);p.setStrokeWidth(dp(1.7f));p.setColor(TEXT);path.reset();path.moveTo(x,y+dp(8));path.cubicTo(x-dp(19),y-dp(3),x-dp(10),y-dp(12),x,y-dp(5));path.cubicTo(x+dp(10),y-dp(12),x+dp(19),y-dp(3),x,y+dp(8));c.drawPath(path,p);}
        private void iconChat(Canvas c,float x,float y,boolean active){strokeSetup();r.set(x-dp(9),y-dp(7),x+dp(9),y+dp(7));c.drawRoundRect(r,dp(5),dp(5),p);c.drawLine(x-dp(4),y+dp(7),x-dp(1),y+dp(11),p);}
        private void iconSpark(Canvas c,float x,float y,boolean active){strokeSetup();c.drawLine(x,y-dp(10),x,y+dp(10),p);c.drawLine(x-dp(10),y,x+dp(10),y,p);c.drawLine(x-dp(6),y-dp(6),x+dp(6),y+dp(6),p);c.drawLine(x-dp(6),y+dp(6),x+dp(6),y-dp(6),p);}
        private void iconPerson(Canvas c,float x,float y,boolean active){strokeSetup();c.drawCircle(x,y-dp(5),dp(5),p);r.set(x-dp(9),y+dp(2),x+dp(9),y+dp(11));c.drawArc(r,180,180,false,p);}
        private void iconImage(Canvas c,float x,float y){strokeSetup();r.set(x-dp(9),y-dp(8),x+dp(9),y+dp(8));c.drawRoundRect(r,dp(3),dp(3),p);c.drawCircle(x-dp(3),y-dp(2),dp(2),p);c.drawLine(x-dp(8),y+dp(5),x-dp(1),y,x+dp(3),y+dp(3),p);}
        private void iconCamera(Canvas c,float x,float y){strokeSetup();r.set(x-dp(10),y-dp(6),x+dp(10),y+dp(7));c.drawRoundRect(r,dp(4),dp(4),p);c.drawCircle(x,y,dp(4),p);c.drawLine(x-dp(6),y-dp(7),x-dp(2),y-dp(11),p);}
        private void iconFile(Canvas c,float x,float y){strokeSetup();r.set(x-dp(7),y-dp(9),x+dp(7),y+dp(9));c.drawRoundRect(r,dp(3),dp(3),p);c.drawLine(x-dp(3),y-dp(2),x+dp(3),y-dp(2),p);c.drawLine(x-dp(3),y+dp(3),x+dp(3),y+dp(3),p);}
        private void iconPen(Canvas c,float x,float y){strokeSetup();c.drawLine(x-dp(7),y+dp(7),x+dp(7),y-dp(7),p);c.drawLine(x-dp(8),y+dp(8),x-dp(4),y+dp(8),p);}
        private void iconGear(Canvas c,float x,float y){strokeSetup();c.drawCircle(x,y,dp(7),p);c.drawCircle(x,y,dp(2.5f),p);for(int i=0;i<8;i++){double a=i*Math.PI/4; c.drawLine(x+(float)Math.cos(a)*dp(8),y+(float)Math.sin(a)*dp(8),x+(float)Math.cos(a)*dp(11),y+(float)Math.sin(a)*dp(11),p);}}
        private void iconLock(Canvas c,float x,float y){strokeSetup();r.set(x-dp(8),y-dp(2),x+dp(8),y+dp(8));c.drawRoundRect(r,dp(2),dp(2),p);r.set(x-dp(5),y-dp(9),x+dp(5),y+dp(3));c.drawArc(r,180,-180,false,p);}
        private void iconShield(Canvas c,float x,float y){strokeSetup();path.reset();path.moveTo(x,y-dp(10));path.lineTo(x+dp(8),y-dp(6));path.lineTo(x+dp(6),y+dp(5));path.quadTo(x,y+dp(12),x-dp(6),y+dp(5));path.lineTo(x-dp(8),y-dp(6));path.close();c.drawPath(path,p);}
        private void iconBell(Canvas c,float x,float y){strokeSetup();path.reset();path.moveTo(x-dp(7),y+dp(5));path.lineTo(x-dp(7),y-dp(2));path.quadTo(x-dp(7),y-dp(9),x,y-dp(9));path.quadTo(x+dp(7),y-dp(9),x+dp(7),y-dp(2));path.lineTo(x+dp(7),y+dp(5));c.drawPath(path,p);c.drawLine(x-dp(9),y+dp(5),x+dp(9),y+dp(5),p);}
        private void iconBox(Canvas c,float x,float y){strokeSetup();r.set(x-dp(8),y-dp(6),x+dp(8),y+dp(7));c.drawRoundRect(r,dp(2),dp(2),p);c.drawLine(x-dp(8),y-dp(1),x+dp(8),y-dp(1),p);}
        private void iconInfo(Canvas c,float x,float y){strokeSetup();c.drawCircle(x,y,dp(9),p);p.setStyle(Paint.Style.FILL);c.drawCircle(x,y-dp(4),dp(1.5f),p);c.drawRect(x-dp(1),y-dp(1),x+dp(1),y+dp(6),p);}
        private void iconEdit(Canvas c,float x,float y){strokeSetup();c.drawLine(x-dp(8),y+dp(7),x+dp(6),y-dp(7),p);c.drawLine(x-dp(8),y+dp(7),x-dp(2),y+dp(6),p);}
        private void iconChevron(Canvas c,float x,float y){strokeSetup();c.drawLine(x-dp(3),y-dp(6),x+dp(3),y,p);c.drawLine(x+dp(3),y,x-dp(3),y+dp(6),p);}

        private void performTap(float x,float y) {
            if (editorOpen) {
                if (y > getHeight()-dp(125) && x < dp(160)) { editorOpen=false; toast("Draft discarded"); }
                else if (y > getHeight()-dp(125) && x > getWidth()-dp(180)) { editorOpen=false; toast("Story draft queued"); }
                else if (x < dp(132)) {
                    toast("Editor: " + (y < dp(190) ? "Edit" : y < dp(252) ? "Filters" : y < dp(314) ? "Text" : y < dp(376) ? "Music" : "Duration 24h"));
                }
                invalidate(); return;
            }

            if (menuOpen) {
                float bottom=getHeight()-dp(78);
                if(y>bottom-dp(250)&&y<bottom){
                    if(y<bottom-dp(186)) toast("Gallery ready");
                    else if(y<bottom-dp(134)) toast("Camera ready");
                    else if(y<bottom-dp(82)) toast("File picker ready");
                    else { menuOpen=false; editorOpen=true; }
                } else menuOpen=false;
                invalidate(); return;
            }

            if ("chat".equals(screen)) {
                if (y < dp(70) && x < dp(80)) { screen="home"; invalidate(); return; }
                if (y > getHeight()-dp(92) && x < dp(75)) { menuOpen=true; invalidate(); return; }
                if (y > getHeight()-dp(92) && x > getWidth()-dp(78)) { sendDraft(); invalidate(); return; }
                if (y > getHeight()-dp(92)) { composerFocused=true; if(draft.isEmpty()){draft="Typing…";} invalidate(); return; }
            } else if ("settings".equals(screen)) {
                if (y < dp(70) && x < dp(80)) { screen="profile"; invalidate(); return; }
                float yy=y+settingsScroll-dp(82);
                if(yy>dp(78)&&yy<dp(132)){dark=!dark; toast(dark?"Dark glass on":"Light glass on");}
                else if(yy>dp(142)&&yy<dp(196)){sleep=!sleep; toast(sleep?"Sleep Mode 10 PM—5 AM":"Sleep Mode off");}
                else if(yy>dp(206)&&yy<dp(260)){fontScale=fontScale==1?2:fontScale==2?0:1;toast(fontScale==2?"Large text":fontScale==0?"Small text":"Default text");}
                else if(yy>dp(270)&&yy<dp(324)){arabic=!arabic;toast(arabic?"العربية":"English");}
                invalidate(); return;
            } else {
                if (y < dp(70) && x < dp(80)) { screen="profile"; invalidate(); return; }
                if (tab.equals("chats") && y > dp(150) && y < dp(620)) { screen="chat"; invalidate(); return; }
                if (tab.equals("profile") && y > dp(300) && y < dp(520) && x > dp(20) && x < getWidth()-dp(20)) {
                    screen="settings"; invalidate(); return;
                }
                if (y > getHeight()-dp(80)) {
                    if (x < getWidth()/3f) tab="chats";
                    else if (x < getWidth()*2/3f) tab="fedo";
                    else tab="profile";
                    invalidate(); return;
                }
                if (tab.equals("fedo") && y > getHeight()-dp(150) && x>getWidth()-dp(90)) { menuOpen=true; invalidate(); return; }
                if (tab.equals("chats") && y > getHeight()-dp(150) && x>getWidth()-dp(90)) { menuOpen=true; invalidate(); return; }
            }
        }

        private void sendDraft(){
            if(draft.trim().isEmpty()){toast("Write a message first");return;}
            if(draft.equals("Typing…")) draft="";
            else {draft=""; toast(isOnline()?"Message sent":"Saved as pending");}
        }

        private boolean isOnline(){
            ConnectivityManager cm=(ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm==null)return false;
            if(Build.VERSION.SDK_INT>=23){
                android.net.Network n=cm.getActiveNetwork();
                NetworkCapabilities cap=cm.getNetworkCapabilities(n);
                return cap!=null && cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
            return true;
        }

        private void toast(String s){toast=s;toastUntil=System.currentTimeMillis()+1800;performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);}

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(), y=e.getY();
            if("settings".equals(screen)){
                if(e.getAction()==MotionEvent.ACTION_DOWN){downX=x;downY=y;scrollStart=y;initialScroll=settingsScroll;dragging=false;return true;}
                if(e.getAction()==MotionEvent.ACTION_MOVE){
                    float dy=y-scrollStart;
                    if(Math.abs(dy)>dp(6)){dragging=true;settingsScroll=Math.max(0,Math.min(dp(240),initialScroll-dy));invalidate();}
                    return true;
                }
                if(e.getAction()==MotionEvent.ACTION_UP){if(!dragging)performTap(x,y);return true;}
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_DOWN){downX=x;downY=y;return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){if(Math.abs(x-downX)<dp(18)&&Math.abs(y-downY)<dp(18))performTap(x,y);return true;}
            return true;
        }

        public boolean handleBack(){
            if(editorOpen){editorOpen=false;invalidate();return true;}
            if(menuOpen){menuOpen=false;invalidate();return true;}
            if(composerFocused){composerFocused=false;if(draft.equals("Typing…"))draft="";invalidate();return true;}
            if("chat".equals(screen)){screen="home";invalidate();return true;}
            if("settings".equals(screen)){screen="profile";invalidate();return true;}
            return false;
        }
    }

    static final class ChatItem {
        final String initial,name,last,time; final int unread; final boolean sent;
        ChatItem(String i,String n,String l,String t,int u,boolean s){initial=i;name=n;last=l;time=t;unread=u;sent=s;}
    }
}
