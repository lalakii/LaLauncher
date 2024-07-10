-libraryjars "C:\Users\sa\AppData\Local\Android\Sdk\platforms\android-35\android.jar"
-optimizationpasses 7
-dontusemixedcaseclassnames
-overloadaggressively
-repackageclasses "cn.lalaki"
-adaptresourcefilenames
-dontwarn java.lang.**
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static *** isLoggable(java.lang.String, ...);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
    public static java.lang.String getStackTraceString(java.lang.Throwable);}
-assumenosideeffects class java.io.PrintWriter{ *;}
-assumenosideeffects class android.window.OnBackInvokedDispatcher{ *;}
-assumenosideeffects class android.window.OnBackInvokedCallback{ *;}
-assumenosideeffects class java.lang.OutOfMemoryError{ *;}
-assumenosideeffects class java.util.logging.Logger{*;}
-assumenosideeffects class androidx.profileinstaller.**{ *;}
-assumenosideeffects class androidx.view.menu.**{ *;}
-assumenosideeffects class androidx.fragment.app.FragmentActivity{
    protected void onPause();
    protected void onStop();
    protected void onDestroy();
}
-assumenosideeffects class android.app.Activity{
    protected void onDestroy();
}
-assumenosideeffects class android.app.Application{
    public void onLowMemory();
    public void onTerminate();
}
-assumenosideeffects class androidx.versionedparcelable.**{ *;}
-assumenosideeffects class androidx.activity.ImmLeaksCleaner{ *;}
-assumenosideeffects class androidx.activity.OnBackPressedDispatcher{ *;}
-assumenosideeffects class androidx.activity.OnBackPressedDispatcher$LifecycleOnBackPressedCancellable{ *;}
# -assumenosideeffects class androidx.startup.**{ *;} ignored
-assumenosideeffects class java.lang.Throwable{
    public void printStackTrace();
}
-renamesourcefileattribute ''
-ignorewarnings
-verbose
-assumenosideeffects class androidx.recyclerview.widget.RecyclerView{
    static final java.lang.String TAG;
    static final java.lang.String TRACE_SCROLL_TAG;
    static final java.lang.String TRACE_ON_LAYOUT_TAG ;
    static final  java.lang.String TRACE_SCROLL_TAG;
    private static final java.lang.String TRACE_ON_LAYOUT_TAG ;
    private static final java.lang.String TRACE_ON_DATA_SET_CHANGE_LAYOUT_TAG ;
    private static final java.lang.String TRACE_HANDLE_ADAPTER_UPDATES_TAG ;
    static final  java.lang.String TRACE_BIND_VIEW_TAG ;
    static final  java.lang.String TRACE_PREFETCH_TAG ;
    static final  java.lang.String TRACE_NESTED_PREFETCH_TAG ;
    static final  java.lang.String TRACE_CREATE_VIEW_TAG;
    public java.lang.CharSequence getAccessibilityClassName();
}
-assumenosideeffects class java.lang.IllegalStateException{*;}
