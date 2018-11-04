package com.teamll.expectlauncher.ultilities;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class Tool {
    private static final String TAG="Tool";

    private static Tool tool;
    private Context context;
    public static void Init(Context context) {
        if(tool==null) tool = new Tool();
        tool.context = context;
        tool.resumeWallpaperTracking();
    }
    public static Tool getInstance() {
        return tool;
    }
    private ArrayList<WallpaperChangedNotifier> notifiers = new ArrayList<>();
    private ArrayList<Boolean> CallFirstTime = new ArrayList<>();
    public void AddWallpaperChangedNotifier(WallpaperChangedNotifier notifier) {
        notifiers.add(notifier);
        CallFirstTime.add(false);
    }

    public interface WallpaperChangedNotifier {
        void onWallpaperChanged(Bitmap original, Bitmap blur) ;
    }

    private Bitmap originalWallPaper;
    private Bitmap blurWallPaper;
    private Bitmap getActiveWallPaper()
    {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bmp = ((BitmapDrawable)wallpaperDrawable).getBitmap();
        if(bmp.getWidth()>0) return bmp.copy(bmp.getConfig(),true);
        return Bitmap.createBitmap(150,150, Bitmap.Config.ARGB_8888);
    }

    private Bitmap blurWallBitmap() {
        return BitmapEditor.getBlurredWithGoodPerformance(context,originalWallPaper,1,12,1.4f);
    }
    private boolean status = false;
    public void stopWallpaperTracking() {
        if(status) {
            status = false;
            mHandler.removeCallbacks(mHandlerTask);
        }
    }
    public void resumeWallpaperTracking() {
        if(!status) {
            status = true;
            mHandlerTask.run();
        }
    }
    private final static int INTERVAL = 1000  * 2; //2 minutes
    private Handler mHandler = new Handler();
    private boolean runningAsyncTask = false;

    private Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            if (!runningAsyncTask) {
                runningAsyncTask = true;
                new WallpaperLoadAndCompare().execute(Tool.this);
            }
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };
    private static class WallpaperLoadAndCompare extends AsyncTask<Tool, Void,Boolean> {
        Tool tool;
        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG,"compare result : "+result);
            if(tool.status) {
                if (result) {
                    // nếu có thay đổi
                    for (int i = 0; i < tool.notifiers.size(); i++) {
                        WallpaperChangedNotifier item = tool.notifiers.get(i);
                        item.onWallpaperChanged(tool.originalWallPaper, tool.blurWallPaper);
                        tool.CallFirstTime.set(i, true);
                    }

                } else {
                    // ngược lại duyệt mảng xem phần tử nào chưa dc gọi lần đầu thì gọi
                    for (int i = tool.notifiers.size() - 1; i != -1; i--) {
                        if (!tool.CallFirstTime.get(i)) {
                            tool.CallFirstTime.set(i, true);
                            tool.notifiers.get(i).onWallpaperChanged(tool.originalWallPaper, tool.blurWallPaper);
                        } else break;
                    }

                }
            }
            tool.runningAsyncTask = false;
        }

        @Override
        protected Boolean doInBackground(Tool... t) {
            this.tool = t[0];
            Bitmap origin = tool.originalWallPaper;

            // nếu ảnh gốc chưa được load lần đầu
            if(origin ==null) {
                tool.originalWallPaper = tool.getActiveWallPaper();
                tool.blurWallPaper = tool.blurWallBitmap();
                return true;
            }
            // ngược lại ta so sánh ảnh mới và ảnh gốc
            Bitmap newOrigin = tool.getActiveWallPaper();
            if(!origin.sameAs(newOrigin)) {
                tool.originalWallPaper = newOrigin;
                tool.blurWallPaper = tool.blurWallBitmap();
                return true;
            } else return false;
        }
    }



    private static int GlobalColor = 0xffff4081;
    private static int SurfaceColor =0xff00dbde;
    public static void setSurfaceColor(int surfaceColor) {
        SurfaceColor = surfaceColor;
    }

    /**
     *  A color get in 7 basic color, nearly the global color
     * @return a color which nearly the global color
     */
    public static int getSurfaceColor() {
        return SurfaceColor;
    }
    public static void setGlobalColor(int globalColor)
    {
        GlobalColor = globalColor;
    }

    /**
     * The most common color get from the art song.
     * @return integer value of color
     */
    public static int getGlobalColor()
    {
        return GlobalColor;
    }

    public static void setOneDps(float width) {
        oneDPs =width;
        Log.d(TAG, "oneDps = " + oneDPs);
    }
    public static int getHeavyColor() {
        switch (SurfaceColor) {
            case 0xffFF3B30 : return 0xff770000;
            case 0xffFF9500 : return 0xff923C00;
            case 0xffFFCC00 : return  0xffAF8700;
            case 0xff4CD964 :return 0xff005800;
            case 0xff5AC8FA: return 0xff0058AA;
            case 0xff007AFF: return 0xff00218B;
            case 0xff5855D6: return 0xff162EA6;
            default: //0xffFB2C57
                return  0xffb60024;
        }
    }
    public static int getHeavyColor(int color_in7_basic) {
        switch (color_in7_basic) {
            case 0xffFF3B30 : return 0xff770000;
            case 0xffFF9500 : return 0xff923C00;
            case 0xffFFCC00 : return  0xff802D00;
            case 0xff4CD964 :return 0xff005800;
            case 0xff5AC8FA: return 0xff0058AA;
            case 0xff007AFF: return 0xff00218B;
            case 0xff5855D6: return 0xff162EA6;
            default: //0xffFB2C57
                return  0xffb60024;
        }
    }
    public static class Avatar {
        public static int getDevideSize(int sizeUWant, Bitmap original) {
            float sizeYouWant= sizeUWant;
            int original_width = original.getWidth();
            int original_height = original.getHeight();
            int sizeOriginal = (original_height < original_width) ? original_width : original_height; // lấy cái lớn hơn
            float devided = (sizeOriginal/sizeYouWant);
            //     System.out.printf(devided+" ");
            int i=1;
            while (true)
            {
                if(devided<i)
                    break;
                else i*=2;
            }
            //   System.out.printf(i+" ");
            if((devided-i/2)<(1/6.0f*i))
                i/=2;
            return i;
        }
    }
    public static int Path_Is_Exist(String dir_path)
    {
        File dir = new File(dir_path);
        if(!dir.exists()) return -1;
        if(dir.isDirectory()) return 1;
        if(dir.isFile()) return  2;
        return 0;
    }
    public static int StatusHeight = -1;
    public static  int getStatusHeight(Resources myR)
    {
        if(StatusHeight!=-1) return StatusHeight;
        int height;
        int idSbHeight = myR.getIdentifier("status_bar_height", "dimen", "android");
        if (idSbHeight > 0) {
            height = myR.getDimensionPixelOffset(idSbHeight);
            //   Toast.makeText(this, "Status Bar Height = "+ height, Toast.LENGTH_SHORT).show();
        } else {
            height = 0;
            //        Toast.makeText(this,"Resources NOT found",Toast.LENGTH_LONG).show();
        }
        StatusHeight =height;
        return StatusHeight;
    }
    public static float getPixelsFromDPs(Context activity, int dps){
        /*
            public abstract Resources getResources ()

                Return A Resources instance for your application's package.
        */
        Resources r = activity.getResources();

        /*
            TypedValue

                Container for A dynamically typed data value. Primarily
                used with Resources for holding resource values.
        */

        /*
            applyDimension(int unit, float value, DisplayMetrics metrics)

                Converts an unpacked complex data value holding
                A dimension to its final floating pp_point value.
        */

        /*
            Density-independent pixel (dp)

                A virtual pixel unit that you should use when defining UI layout,
                to express layout dimensions or posTop in A density-independent way.

                The density-independent pixel is equivalent to one physical pixel on
                A 160 dpi screen, which is the baseline density assumed by the system
                for A "medium" density screen. At runtime, the system transparently handles
                any scaling of the dp units, as necessary, based on the actual density
                of the screen in use. The conversion of dp units to screen pixels
                is simple: px = dp * (dpi / 160). For example, on A 240 dpi screen,
                1 dp equals 1.5 physical pixels. You should always use dp
                units when defining your application's UI, to ensure proper
                display of your UI on screens with different densities.
        */

        /*
            public static final int COMPLEX_UNIT_DIP

                TYPE_DIMENSION complex unit: Value is Device Independent Pixels.
                Constant Value: 1 (0x00000001)
        */
        return (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dps, r.getDisplayMetrics()));
    }
    public static float getOneDps(Context context)
    {
        if(oneDPs !=-1) return oneDPs;
        //      oneDPs = context.getResources().getDimensionPixelOffset(R.dimen.oneDP);
        oneDPs = getPixelsFromDPs((Activity)context,1);
        return oneDPs;
    }
    public static float oneDPs =-1;
    public static int getDpsFromPixel(Activity activity,int px) {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
    static int[] screenSize;
    static float[] screenSizeInDp;
    public static boolean HAD_GOT_SCREEN_SIZE = false;
    public static int[] getScreenSize(Context context)
    {
        if(!HAD_GOT_SCREEN_SIZE) {
            Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); // this will get the view of screen
            int width = d.getWidth();
            int height = d.getHeight();
            screenSize = new int[] {width,height};
            screenSizeInDp = new float[] {(width+0.0f)/getOneDps(context),(height+0.0f)/getOneDps(context)};
            HAD_GOT_SCREEN_SIZE = true;
        }
        return screenSize;
    }
    public static int[] getScreenSize(boolean sure) {
        return screenSize;
    }

    public static int[] getRefreshScreenSize(Context context)
    {
        HAD_GOT_SCREEN_SIZE = false;
        return getScreenSize(context);
    }

    public static float[] getScreenSizeInDp(Context context)
    {
        if(!HAD_GOT_SCREEN_SIZE) getScreenSize(context);
        return screenSizeInDp;
    }


    public static boolean hasSoftKeys(WindowManager windowManager){
        Display d = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);

        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getNavigationHeight(Activity activity)
    {

        int navigationBarHeight = 0;
        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }
        if(!hasSoftKeys(activity.getWindowManager())) return 0;
        return  navigationBarHeight;
    }
    public static String convertByteArrayToString(byte[] b)
    {
        String r ="";
        int len = b.length;
        for(int i=0;i<len;i++)
            if(i!=len-1) r+=Integer.toHexString(b[i])+":";
            else r+=b[i];
        return  r;
    }
    public static void showToast(Context context,String text, int time)
    {

        final Toast toast =   Toast.makeText(context,text,Toast.LENGTH_SHORT);
        toast.show();
        //   TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
        //  textView.setBackgroundColor(Color.WHITE);
        //textView.setTextColor(Color.BLACK);
        //   ((View)textView.getParent()).setBackground(R.drawable.corner_layout);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();

            }
        }, time);
    }
    private static boolean drawn = false;

    public static boolean isDrawn() {
        return drawn;
    }

    public static void setDrawn(boolean Drawn) {
        drawn = Drawn;
    }
    private static boolean splashGone = false;

    public static boolean isSplashGone() {
        return splashGone;
    }

    public static void setSplashGone(boolean splashGone) {
        Tool.splashGone = splashGone;
    }
    public static String getStringTagForView(View v){
        //   Log.d("Sticky","getStringTagForView");

        Object tagObject = v.getTag();
        return String.valueOf(tagObject);
    }

}
