/*
 * Copyright (C) 2026 lalaki
 *
 * This file is part of LaLauncher.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see
 * <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 */
package a;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/***
 * Created on 2026-09-27
 *
 * @author lalaki i@lalaki.cn
 * @since la launcher
 */
public class a extends Activity implements View.OnClickListener {
    Intent t;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(null);
        if (t == null) {
            IntentFilter f = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            f.addAction(Intent.ACTION_PACKAGE_REMOVED);
            f.addDataScheme("package");
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent i) {
                    a.this.onCreate(null);
                }
            }, f);
        }
        t = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        int w = Build.VERSION.SDK_INT;
        if (w > Build.VERSION_CODES.HONEYCOMB_MR2) {
            setTheme(android.R.style.Theme_DeviceDefault_Wallpaper_NoTitleBar);
        } else if (w > Build.VERSION_CODES.DONUT) {
            setTheme(android.R.style.Theme_Wallpaper_NoTitleBar);
        }
        w = getResources().getDisplayMetrics().widthPixels;
        int wm = w / (w / (int) (new TextView(this).getTextSize() * 6)); // item size
        int ws = wm / 2; // logo size
        int ww = w / wm;
        int pd = ws / 5;
        LinearLayout y = new LinearLayout(this);
        LinearLayout l = null;
        y.setOrientation(LinearLayout.VERTICAL);
        List<ResolveInfo> q = getPackageManager().queryIntentActivities(t, 0);
        for (w = 0; w < q.size(); w++) {
            if (w % ww == 0) {
                l = new LinearLayout(this);
                l.setPadding(0, pd, 0, 0);
                y.addView(l);
            }
            ResolveInfo i = q.get(w);
            TextView v = new TextView(this);
            v.setWidth(wm);
            v.setGravity(Gravity.CENTER_HORIZONTAL);
            v.setText(i.loadLabel(getPackageManager()));
            Drawable icon = i.loadIcon(getPackageManager());
            icon.setBounds(0, 0, ws, ws);
            v.setCompoundDrawables(null, icon, null, null);
            v.setCompoundDrawablePadding(pd);
            v.setTag(new ComponentName(i.activityInfo.packageName, i.activityInfo.name));
            v.setOnClickListener(this);
            l.addView(v);
        }
        ScrollView s = new ScrollView(this);
        s.addView(y);
        setContentView(s);
    }

    @Override
    public void onClick(View v) {
        startActivity(t.setComponent((ComponentName) v.getTag()));
    }
}
