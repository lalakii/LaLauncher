package c;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

@SuppressWarnings("all")
/***
 * Created on 2024-06-23
 *
 * @author lalaki
 * @since la launcher
 */ public class La extends android.app.Activity {
    Lv[] apps;
    String sPackage;
    Paint paint = new Paint();
    int padding, side = 1;
    Intent launch = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    @Override
    public void onWindowFocusChanged(boolean __) {
        if (apps == null) {
            android.content.pm.PackageManager pm = getPackageManager();
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            int x = metrics.widthPixels, y = metrics.heightPixels - (int) (metrics.density * 24), i = 0, j = 0, col;
            java.util.List<ResolveInfo> ls = pm.queryIntentActivities(launch, 0);
            apps = new Lv[ls.size()];
            for (; ; ) {
                col = x / side;
                if (apps.length / col * side >= y) {
                    col++;
                    side = x / col;
                    padding = side / col;
                    paint.setTextSize(padding);
                    break;
                }
                side++;
            }
            for (int k = 0; k < apps.length; k++) {
                ResolveInfo l = ls.get(k);
                Lv v = new Lv();
                apps[k] = v;
                v.mIcon = l.loadIcon(pm);
                v.mActivityName = l.activityInfo.name;
                v.mPackageName = l.activityInfo.packageName;
                v.mAppName = (String) l.activityInfo.loadLabel(pm);
                x = j * side;
                y = i * side;
                v.mIcon.setBounds(x + padding, y + padding, x + side - padding, y + side - padding);
                j++;
                i = (j %= col) == 0 ? ++i : i;
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
            for (int k = 0; k < apps.length; k++) {
                Lv v = apps[k];
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
                    for (int k = 0; k < apps.length; k++) {
                        Lv v = apps[k];
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