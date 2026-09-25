package c;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;

/***
 * Created on 2026-09-25
 *
 * @author lalaki
 * @since la launcher for TV
 */
public class a extends android.app.Activity {
    z[] apps;
    int padding, col, selected = -1, side = 1;
    Intent launch = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    @SuppressWarnings({"ReassignedVariable", "DataFlowIssue", "NewApi", "ResourceType", "deprecation"})
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme(16973920);
        super.onStart();
        if (apps == null) {
            if (side == 1) {
                IntentFilter f = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
                f.addAction(Intent.ACTION_PACKAGE_REMOVED);
                f.addDataScheme("package");
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        apps = null;
                        onCreate(null);
                    }
                }, f);
            }
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            int x = metrics.widthPixels, y = metrics.heightPixels - (int) metrics.density * 25, i = -1, j;
            android.content.pm.PackageManager pm = getPackageManager();
            java.util.List<ResolveInfo> ls = pm.queryIntentActivities(launch, 0);
            int sz = ls.size();
            apps = new z[sz];
            while (x * y / (side * side) >= sz) {
                side++;
            }
            col = x / side;
            while ((sz / col + (sz % col == 0 ? 0 : 1)) * side > y) {
                col++;
            }
            side = x / col;
            float s = side * 0.16f;
            padding = (int) (1.43 * s);
            for (int k = 0; k < sz; k++) {
                i = (j = k % col) == 0 ? ++i : i;
                x = j * side;
                y = i * side;
                z v = new z();
                apps[k] = v;
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX, s );
                ResolveInfo l = ls.get(k);
                v.mIcon = l.loadIcon(pm);
                v.mPackageName = l.activityInfo.packageName;
                v.mActivityName = l.activityInfo.name;
                v.mAppName = new StaticLayout(
                        l.activityInfo.loadLabel(pm),
                        v.getPaint(), side, Layout.Alignment.ALIGN_CENTER, 0.9f, 0, false);
                v.mIcon.setBounds(x + padding, y + padding, x + side - padding, y + side - padding);
            }
            setContentView(new z());
        }
    }

    private class z extends Button implements Runnable {
        public z() {
            super(a.this);
            setBackgroundColor(0);
        }

        android.graphics.drawable.Drawable mIcon;
        String mPackageName, mActivityName;
        StaticLayout mAppName;
        int x;
        int y;
        String s;

        @SuppressWarnings("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent e) {
            int a = e.getAction();
            if (a == MotionEvent.ACTION_DOWN) {
                x = (int) e.getX();
                y = (int) e.getY();
                invalidate();
            } else if (a == MotionEvent.ACTION_UP) {
                if (s != null) {
                    startActivity(launch);
                    s = null;
                }
            }
            return true;
        }

        @Override
        public boolean onKeyDown(int i, KeyEvent e) {
            int tmpSelected;
            switch (i) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    startActivity(launch);
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (selected > 0) {
                        selected--;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (selected < apps.length - 1) {
                        selected++;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    tmpSelected = selected;
                    tmpSelected -= col;
                    if (tmpSelected > -1) {
                        selected = tmpSelected;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    tmpSelected = selected;
                    tmpSelected += col;
                    if (tmpSelected < apps.length) {
                        selected = tmpSelected;
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                    return true;
            }
            invalidate();
            return true;
        }

        @Override
        public void onDraw(android.graphics.Canvas g) {
            for (int k = 0; k < apps.length; k++) {
                z v = apps[k];
                Rect rect = v.mIcon.getBounds();
                if (rect.contains(x, y)) {
                    s = v.mPackageName;
                    launch.setClassName(s, v.mActivityName);
                    postDelayed(this, 500);
                    x = -1;
                }
                v.mIcon.draw(g);
                Paint paint = getPaint();
                if (k == selected) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(3f);
                    paint.setColor(0xFFFF2222);
                    g.drawRect(rect, paint);
                    launch.setClassName(v.mPackageName, v.mActivityName);
                } else {
                    paint.setColor(-1);
                }
                g.save();
                g.translate(rect.left - padding, rect.bottom + (padding * 0.3f));
                v.mAppName.draw(g);
                g.restore();
            }
        }

        @SuppressWarnings("NewApi")
        @Override
        public void run() {
            String i = s;
            if (i != null) {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, android.net.Uri.fromParts("package", i, null)));
                s = null;
            }
        }
    }
}