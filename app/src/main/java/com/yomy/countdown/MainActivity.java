package com.yomy.countdown;

import android.app.*;import android.os.*;import android.graphics.*;import android.graphics.drawable.*;import android.view.*;import android.content.*;import java.time.*;import java.time.format.*;import java.util.*;

public class MainActivity extends Activity {
  CountdownView view;
  @Override public void onCreate(Bundle b){super.onCreate(b); view=new CountdownView(this); setContentView(view);}
  static class CountdownView extends View {
    Paint p=new Paint(3); Handler h=new Handler(Looper.getMainLooper());
    final ZoneId zone=ZoneId.of("Africa/Cairo");
    final long start=LocalDateTime.of(2026,9,17,0,0).atZone(zone).toInstant().toEpochMilli();
    final long target=LocalDateTime.of(2026,9,24,0,0).atZone(zone).toInstant().toEpochMilli();
    long left;
    Runnable tick=new Runnable(){public void run(){invalidate();h.postDelayed(this,250);}};
    CountdownView(Context c){super(c);p.setTypeface(Typeface.create("sans",Typeface.NORMAL));h.post(tick);}
    void txt(Canvas c,String s,float x,float y,float size,boolean bold){p.setTextSize(size);p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);c.drawText(s,x,y,p);}
    protected void onDraw(Canvas c){super.onDraw(c); int w=getWidth(),he=getHeight();
      c.drawColor(Color.rgb(8,9,15)); long now=System.currentTimeMillis(); left=Math.max(0,target-now); long sec=left/1000; long days=sec/86400; sec%=86400; long hours=sec/3600;sec%=3600;long mins=sec/60;sec%=60;
      txt(c,"YOMY",w/2f,Math.max(110,he*.18f),42,true); txt(c,"THE COUNTDOWN",w/2f,Math.max(145,he*.22f),13,false);
      float y=he*.43f; box(c,w*.08f,y,w*.92f,y+115,"DAYS",String.format(Locale.US,"%02d",days));
      box(c,w*.08f,y+135,w*.92f,y+250,"HOURS",String.format(Locale.US,"%02d",hours));
      box(c,w*.08f,y+270,w*.92f,y+385,"MINUTES",String.format(Locale.US,"%02d",mins));
      box(c,w*.08f,y+405,w*.92f,y+520,"SECONDS",String.format(Locale.US,"%02d",sec));
      float progress=Math.min(1f,Math.max(0f,(now-start)/(double)(target-start)));p.setColor(Color.rgb(45,48,60));c.drawRoundRect(w*.12f,he*.91f,w*.88f,he*.91f+8,8,8,p);p.setColor(Color.WHITE);c.drawRoundRect(w*.12f,he*.91f,w*.12f+(w*.76f*progress),he*.91f+8,8,8,p);
      txt(c,progress>=1?"THE MOMENT HAS ARRIVED":"24 SEPTEMBER 2026",w/2f,he*.965f,14,true);
    }
    void box(Canvas c,float l,float t,float r,float b,String label,String value){p.setColor(Color.rgb(17,19,28));c.drawRoundRect(l,t,r,b,22,22,p);txt(c,value,(l+r)/2,(t+b)/2+16,58,true);txt(c,label,(l+r)/2,b-17,11,false);}
  }
}
