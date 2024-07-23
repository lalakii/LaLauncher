package c;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.KeyEvent;
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
    int padding, col, selected = -1, side = 1;
    Intent launch = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    View root;

    @Override
    public void onWindowFocusChanged(boolean __) {
        if (apps == null) {
            android.content.pm.PackageManager pm = getPackageManager();
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            int x = metrics.widthPixels, y = metrics.heightPixels - (int) (metrics.density * 24), i = 0, j = 0;
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
            root = new Lv();
            setContentView(root);
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
                if (k == selected) {
                    paint.setStyle(Paint.Style.STROKE);
                    g.drawRect(v.mIcon.getBounds(), paint);
                    paint.setColor(Color.MAGENTA);
                    launch.setClassName(v.mPackageName, v.mActivityName);
                } else {
                    paint.setColor(Color.BLACK);
                }
                paint.setStyle(Paint.Style.FILL);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        int tmpSelected;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (selected > 0) {
                    selected--;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                tmpSelected = selected;
                tmpSelected -= col;
                if (tmpSelected > -1) {
                    selected = tmpSelected;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (selected < apps.length - 1) {
                    selected++;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                tmpSelected = selected;
                tmpSelected += col;
                if (tmpSelected < apps.length) {
                    selected = tmpSelected;
                }
                break;
            case KeyEvent.KEYCODE_ENTER:
                startActivity(launch);
                break;
        }
        root.invalidate();
        return super.onKeyDown(keyCode, event);
    }
}