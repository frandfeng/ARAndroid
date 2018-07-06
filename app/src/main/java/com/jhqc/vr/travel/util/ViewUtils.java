package com.jhqc.vr.travel.util;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


/**
 * Created by Solomon on 2017/10/20 0020.
 */
public class ViewUtils {

    /**
     * Convert the dip to the pixel
     */
    public static int dipToPixel(float dpValue, Context mcontext) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, mcontext.getResources().getDisplayMetrics());
    }

    /**
     * Convert the pixel to the dip
     */
    public static int pixelToDip(float pxValue, Context mcontext) {
        float scale = mcontext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * Get display metrics
     */
    public static DisplayMetrics getDisplayMetrics(Context mcontext) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) mcontext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    public static int getDensityDpi(Context mcontext) {
        return mcontext.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * Get the screen height
     */
    public static int getScreenHeight(Context mcontext) {
        return mcontext.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * Get the screen width
     */
    public static int getScreenWidth(Context mcontext) {
        return mcontext.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScaleWidth(int width, Context mcontext) {
        width = width * getScreenWidth(mcontext) / 480;
        if (width > getScreenWidth(mcontext)) {
            return getScreenWidth(mcontext);
        }
        return width;
    }

    public static int getScaleHeight(int height, Context mcontext) {
        // return height * getScreenHeight() / 800;
        return height * getScreenWidth(mcontext) / 480;
    }

    // 宽度适配，获取相同比例的高
    /*
     * public static int getScaleHeightByWidth(int width, int height){ int
	 * tempWidth = width * getScreenWidth() / 480; return (tempWidth * height) /
	 * width; }
	 */

    /**
     * 创建快捷方式
     *
     * @param clazz 要跳转到哪个activity
     * @param data  进入activity时，带入的值。可以为null 需要权限
     *              com.android.launcher.permission.INSTALL_SHORTCUT
     */
    public static void createShortCut(final Context context, Class<?> clazz, int drawableId, Bitmap bmp, String titleName,
                                      Bundle data) {
        // 创建快捷方式的Intent
        final Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        // 不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        // 需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, titleName);
        // 快捷图片
        if (bmp != null) {
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bmp);
        } else {
            Parcelable icon = Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), drawableId);
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        }
        // 点击快捷图片，运行的程序主入口
        Intent intent = new Intent(context, clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 下面这两句，卸载APK后，删除快捷方式
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        if (data != null) {
            intent.putExtras(data);
        }
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        // 发送广播。OK
        /*SingleHandler.getInstance(true).post(new Runnable() {
            @Override
            public void run() {
                context.sendBroadcast(shortcutintent);
            }
        });*/
    }

    /**
     * 根据 title 判断快捷方式是否存在 需要权限 com.android.launcher.permission.READ_SETTINGS
     */
    public static boolean hasShortcut(final Context context, final String title) {
        String AUTHORITY = getAuthorityFromPermission(context, "com.android.launcher.permission.READ_SETTINGS");
        if (AUTHORITY == null) {
            AUTHORITY = getAuthorityFromPermission(context, "com.android.launcher.permission.WRITE_SETTINGS");
        }
        if (AUTHORITY == null) {
            Log.e("shortCut", "no authority");

            if (Build.VERSION.SDK_INT < 8) {
                AUTHORITY = "com.android.launcher.settings";
            } else {
                AUTHORITY = "com.android.launcher2.settings";
            }
        }
        Log.e("shortCut", "authority:" + AUTHORITY);

        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");

        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(CONTENT_URI, new String[]
                {
                        "title", "iconResource"
                }, "title=?", new String[]
                {
                        title
                }, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                /*SingleHandler.getInstance(true).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, title + context.getString(R.string.UF_SHORTCUT_EXIST), Toast.LENGTH_SHORT).show();
                    }
                });*/
            }
            cursor.close();
            return true;
        }
        return false;
    }

    /**
     * 创建快捷方式没有提示
     *
     * @param clazz 要跳转到哪个activity
     * @param data  进入activity时，带入的值。可以为null 需要权限
     *              com.android.launcher.permission.INSTALL_SHORTCUT
     */
    public static void createShortCutcutNotToast(final Context context, Class<?> clazz, int drawableId, Bitmap bmp,
                                                 String titleName, Bundle data) {
        // 创建快捷方式的Intent
        final Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        // 不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        // 需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, titleName);
        // 快捷图片
        if (bmp != null) {
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bmp);
        } else {
            Parcelable icon = Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), drawableId);
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        }
        // 点击快捷图片，运行的程序主入口
        Intent intent = new Intent(context, clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 下面这两句，卸载APK后，删除快捷方式
        intent.setAction("android.intent.action.MAIN");
        // intent.addCategory("android.intent.category.LAUNCHER");
        if (data != null) {
            intent.putExtras(data);
        }
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        // 发送广播。OK
        /*SingleHandler.getInstance(true).post(new Runnable() {
            @Override
            public void run() {
                context.sendBroadcast(shortcutintent);
            }
        });*/
    }

    /**
     * 根据 title 判断快捷方式是否存在 需要权限 com.android.launcher.permission.READ_SETTINGS
     */
    public static boolean hasShortcutNotToast(final Context context, final String title) {
        String AUTHORITY = getAuthorityFromPermission(context, "com.android.launcher.permission.READ_SETTINGS");
        if (AUTHORITY == null) {
            AUTHORITY = getAuthorityFromPermission(context, "com.android.launcher.permission.WRITE_SETTINGS");
        }
        if (AUTHORITY == null) {
            Log.e("shortCut", "no authority");

            if (Build.VERSION.SDK_INT < 8) {
                AUTHORITY = "com.android.launcher.settings";
            } else {
                AUTHORITY = "com.android.launcher2.settings";
            }
        }
        Log.e("shortCut", "authority:" + AUTHORITY);

        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");

        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(CONTENT_URI, new String[]
                {
                        "title", "iconResource"
                }, "title=?", new String[]
                {
                        title
                }, null);
        if (cursor != null) {

            if (cursor.getCount() > 0) {
                /*SingleHandler.getInstance(true).post(new Runnable() {
                    @Override
                    public void run() {
                        // Toast.makeText(context, title + "  快捷方式已经存在！",
                        // Toast.LENGTH_SHORT).show();
                    }
                });*/
            }
            cursor.close();
            return true;
        }
        return false;
    }

    /**
     * 获取authority
     */
    public static String getAuthorityFromPermission(Context context, String permission) {
        if (permission == null) {
            return null;
        }
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
        if (packs != null) {
            for (PackageInfo pack : packs) {
                ProviderInfo[] providers = pack.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        if (permission.equals(provider.readPermission)) {
                            return provider.authority;
                        }
                        if (permission.equals(provider.writePermission)) {
                            return provider.authority;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 隐藏 键盘
     */
    public static void closeBoard(Context mcontext, View edit) {
        InputMethodManager imm = (InputMethodManager) mcontext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
    }

    /**
     * 判断系统当前应用是否是本应用
     */
    public static boolean isCurrentSys(Context mcontext) {
        // 需要权限 android.permission.GET_TASKS
        ActivityManager manager = (ActivityManager) mcontext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
        RunningTaskInfo cinfo = runningTasks.get(0);
        ComponentName component = cinfo.topActivity;

        String componentName = "";
        if (component == null) {
            return false;
        }
        componentName = component.getClassName();
        if (TextUtils.isEmpty(componentName)) {
            return false;
        }
        String packName = mcontext.getPackageName();
        if (TextUtils.isEmpty(packName)) {
            return false;
        }
        return componentName.startsWith(packName);
    }

    private static final String TAG = "UiUtils";

    /**
     * Time zone to use when formatting all session times. To always use the phone
     * local time, use {@link TimeZone#getDefault()}.
     */
    public static final TimeZone CONFERENCE_TIME_ZONE = TimeZone.getTimeZone("America/Los_Angeles");

    public static final Uri CONFERENCE_URL = Uri.parse("http://www.google.com/events/io/2011/");

    // /** Flags used with {@link DateUtils#formatDateRange}. */
    // private static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME
    // | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;

    // /** {@link StringBuilder} used for formatting time block. */
    // private static StringBuilder sBuilder = new StringBuilder(50);
    // /** {@link Formatter} used for formatting time block. */
    // private static Formatter sFormatter = new Formatter(sBuilder,
    // Locale.getDefault());

    private static StyleSpan sBoldSpan = new StyleSpan(Typeface.BOLD);

    /**
     * Populate the given {@link TextView} with the requested text, formatting
     * through {@link Html#fromHtml(String)} when applicable. Also sets
     * {@link TextView#setMovementMethod} so inline links are handled.
     */
    public static void setTextMaybeHtml(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setText("");
            return;
        }
        if (text.contains("<") && text.contains(">")) {
            view.setText(Html.fromHtml(text));
            view.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            view.setText(text);
        }
    }

    /**
     * Given a snippet string with matching segments surrounded by curly braces,
     * turn those areas into bold spans, removing the curly braces.
     */
    public static Spannable buildStyledSnippet(String snippet) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(snippet);

        // Walk through string, inserting bold snippet spans
        int startIndex = -1, endIndex = -1, delta = 0;
        while ((startIndex = snippet.indexOf('{', endIndex)) != -1) {
            endIndex = snippet.indexOf('}', startIndex);

            // Remove braces from both sides
            builder.delete(startIndex - delta, startIndex - delta + 1);
            builder.delete(endIndex - delta - 1, endIndex - delta);

            // Insert bold style
            builder.setSpan(sBoldSpan, startIndex - delta, endIndex - delta - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            delta += 2;
        }

        return builder;
    }

    public static String getLastUsedTrackID(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString("last_track_id", null);
    }

    public static void setLastUsedTrackID(Context context, String trackID) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString("last_track_id", trackID).apply();
    }

    private static final int BRIGHTNESS_THRESHOLD = 130;

    /**
     * Calculate whether a color is light or dark, based on a commonly known
     * brightness formula.
     *
     * @see {@literal http://en.wikipedia.org/wiki/HSV_color_space%23Lightness}
     */
    public static boolean isColorDark(int color) {
        return ((30 * Color.red(color) + 59 * Color.green(color) + 11 * Color.blue(color)) / 100) <= BRIGHTNESS_THRESHOLD;
    }

    public static boolean isHoneycomb() {
        // Can use static final constants like HONEYCOMB, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed
        // behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isHoneycombTablet(Context context) {
        return isHoneycomb() && isTablet(context);
    }

    public static long getCurrentTime(final Context context) {
        // SharedPreferences prefs = context.getSharedPreferences("mock_data", 0);
        // prefs.edit().commit();
        // return prefs.getLong("mock_current_time", System.currentTimeMillis());
        return System.currentTimeMillis();
    }

    public static Drawable getIconForIntent(final Context context, Intent i) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> infos = pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
        if (infos.size() > 0) {
            return infos.get(0).loadIcon(pm);
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static int sdkVersion() {
        return Integer.parseInt(Build.VERSION.SDK);
    }

    public static void startDialerPanel(Context context, String phoneNumber) {
        try {
            Intent dial = new Intent();
            dial.setAction(Intent.ACTION_DIAL);
            dial.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(dial);
        } catch (Exception ex) {
            Log.e(TAG, "Error starting phone dialer intent.", ex);
            Toast.makeText(context, "Sorry, we couldn't find any app to place a phone call!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void startDialer(Context context, String phoneNumber) {
        try {
            Intent dial = new Intent();
            dial.setAction(Intent.ACTION_DIAL);
            dial.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(dial);
        } catch (Exception ex) {
            Log.e(TAG, "Error starting phone dialer intent.", ex);
            Toast.makeText(context, "Sorry, we couldn't find any app to place a phone call!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void startSmsIntent(Context context, String phoneNumber) {
        try {
            Uri uri = Uri.parse("sms:" + phoneNumber);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.putExtra("address", phoneNumber);
            intent.setType("vnd.android-dir/mms-sms");
            context.startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, "Error starting sms intent.", ex);
            Toast.makeText(context, "Sorry, we couldn't find any app to send an SMS!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void startEmailIntent(Context context, String emailAddress) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]
                    {
                            emailAddress
                    });
            context.startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, "Error starting email intent.", ex);
            Toast.makeText(context, "Sorry, we couldn't find any app for sending emails!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void startWebIntent(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception ex) {
            Log.e(TAG, "Error starting url intent.", ex);
            Toast.makeText(context, "Sorry, we couldn't find any app for viewing this url!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 构造请求参数json串
     */
    @SuppressWarnings("unchecked")
    public static Object getRequestParams(Object request) {
        @SuppressWarnings("rawtypes")
        Map param = new HashMap();
        param.put("request", request);
        return param;
    }


    public static final String SEPARATE_ICON = "    ";// 分隔


    /**
     * 刷新list列表的UI视图
     */
    public static void refreshNewIcon(ListView pageList, int refreshView) {
        int displayCount = pageList.getLastVisiblePosition() - pageList.getFirstVisiblePosition() + 1;
        for (int i = 0; i < displayCount; i++) {
            View item = pageList.getChildAt(i);
            if (item != null) {
                View sub = item.findViewById(refreshView);
                if (sub != null) {
                    sub.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 公共dialog
     */
    public static Builder getDialog(Context context, String title, View contentView) {
        return new Builder(context).setTitle(title).setView(contentView);
    }

    /**
     * 得到几天后的时间
     */
    public static long getDateAfter(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return now.getTimeInMillis();
    }

    /**
     * 隐藏输入法框
     */
    public final static void hideInputKeyboard(final Activity activity, final View force) {
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

//		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);

//		if (force != null)
//		{
//			SingleHandler.getInstance(true).postDelayed(new Runnable()
//			{
//				public void run()
//				{
//					imm.hideSoftInputFromWindow(force.getWindowToken(), 0);
//				}
//			},300);
//		}

//		SingleHandler.getInstance(true).postDelayed(new Runnable()
//		{
//			public void run()
//			{
//				imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),0);
//			}
//		},300);
    }

    /**
     * 显示输入法框
     *
     * @author fengyun.zl
     */
    public final static void showInputKeyboard(final Activity activity) {
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 显示输入法框
     *
     * @author fengyun.zl
     */
    public final static void showInputKeyboard(final Activity activity, final View force) {
        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!isInputKeyboard(activity)) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
//		if (force != null)
//		{
//			force.requestFocus();
//			SingleHandler.getInstance(true).postDelayed(new Runnable()
//			{
//				public void run()
//				{
//					imm.showSoftInput(force,0);
//					// imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
//				}
//			},100);
//		}
    }

    /**
     * 输入法是否打开
     *
     * @return true打开，false关闭
     */
    public final static boolean isInputKeyboard(final Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();//isOpen若返回true，则表示输入法打
        return isOpen;
    }

    public static Bitmap disposeBmpToRound(Bitmap bmp, int width, int height, float roundPx) {
        // 创建新的位图
        Bitmap bgBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        // 把创建的位图作为画板
        Canvas mCanvas = new Canvas(bgBitmap);

        Paint mPaint = new Paint();
        Rect mRect = new Rect(0, 0, width, height);
        RectF mRectF = new RectF(mRect);

        mPaint.setAntiAlias(true);
        // 先绘制圆角矩形
        mCanvas.drawRoundRect(mRectF, roundPx, roundPx, mPaint);

        // 设置图像的叠加模式
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // 绘制图像
        Rect rawRect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
        mCanvas.drawBitmap(bmp, rawRect, mRect, mPaint);

        return bgBitmap;
    }


    /**
     * 释放某个组件上的图片资源(ImageView)
     */
    public static void releaseImageViewResource(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        Drawable drawable = imageView.getDrawable();
        imageView.setImageDrawable(null);
        imageView.unscheduleDrawable(drawable);
        if (drawable != null) {
            releaseDrawable(drawable);
        }
    }

    /**
     * 释放某个组件上的背景图片资源
     */
    @SuppressWarnings("deprecation")
    public static void releaseBackgroundDrawable(View iview) {
        if (iview == null) {
            return;
        }
        Drawable bd = iview.getBackground();
        if (bd != null) {
            iview.setBackgroundDrawable(null);
            iview.unscheduleDrawable(bd);
            releaseDrawable(bd);
        }
    }

    public static void releaseDrawable(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        if (drawable != null && drawable instanceof BitmapDrawable) {
            drawable.setCallback(null);

            /*
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }*/
        } else {
            drawable.setCallback(null);
        }
    }


}
