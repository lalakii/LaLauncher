package cn.lalaki;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

@SuppressWarnings("all")
/***
 * Created on 2024-06-23
 *
 * @author lalaki
 * @since la launcher
 */ public class La extends android.app.Activity {
    List<Lv> apps = new java.util.ArrayList<>();
    Paint paint = new Paint();
    int padding, len, side = 1;
    String sPackage;
    Intent launch = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    @Override
    public void onWindowFocusChanged(boolean __) {
        if (len == 0) {
            android.content.pm.PackageManager pm = getPackageManager();
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            int x = metrics.widthPixels, y = metrics.heightPixels - (int) (metrics.density * 24), i = 0, j = 0, col;
            List<ResolveInfo> ls = pm.queryIntentActivities(launch, 0);
            len = ls.size();
            for (; ; ) {
                col = x / side;
                if (len / col * side >= y) {
                    col++;
                    side = x / col;
                    padding = side / col;
                    paint.setTextSize(padding * 0.8f);
                    break;
                }
                side++;
            }
            for (int k = 0; k < len; k++) {
                ResolveInfo l = ls.get(k);
                Lv v = new Lv();
                apps.add(v);
                v.mIcon = l.loadIcon(pm);
                v.mActivityName = l.activityInfo.name;
                v.mPackageName = l.activityInfo.packageName;
                v.mAppName = (String) pm.getApplicationLabel(l.activityInfo.applicationInfo);
                x = j * side;
                y = i * side;
                v.mIcon.setBounds(x + padding, y + padding, x + side - padding, y + side - padding);
                j++;
                j = j % col;
                i = j == 0 ? ++i : i;
            }
            setContentView(new Lv());
        }
    }

    class Lv extends View implements View.OnLongClickListener {
        android.graphics.drawable.Drawable mIcon;
        String mAppName, mPackageName, mActivityName;

        public Lv() {
            super(La.this);
        }

        @Override
        public void onDraw(android.graphics.Canvas g) {
            setOnLongClickListener(this);
            for (int k = 0; k < len; k++) {
                Lv v = apps.get(k);
                v.mIcon.draw(g);
                String name = v.mAppName.substring(0, paint.breakText(v.mAppName, true, side, null)), arg = v.mAppName.replace(name, "");
                Rect rect = v.mIcon.getBounds();
                int c = rect.left - padding;
                int d = rect.top - padding;
                g.drawText(name, (side - paint.measureText(name)) / 2 + c, side + d, paint);
                g.drawText(arg, (side - paint.measureText(arg)) / 2 + c, side + d + paint.getTextSize(), paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for (int k = 0; k < len; k++) {
                        Lv v = apps.get(k);
                        if (v.mIcon.getBounds().contains((int) e.getX(), (int) e.getY())) {
                            sPackage = v.mPackageName;
                            launch.setClassName(sPackage, v.mActivityName);
                            break;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (sPackage != null) {
                        startActivity(launch);
                    }
                    sPackage = null;
                    break;
            }
            return super.onTouchEvent(e);
        }

        @Override
        public boolean onLongClick(View __) {
            if (sPackage != null) {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.fromParts("package", sPackage, null)));
                sPackage = null;
            }
            return false;
        }
    }

    @Override
    public void onBackPressed() {
    }
}