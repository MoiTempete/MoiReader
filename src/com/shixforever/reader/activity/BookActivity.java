package com.shixforever.reader.activity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import com.shixforever.reader.R;
import com.shixforever.reader.data.BookFile;
import com.shixforever.reader.module.BookMark;
import com.shixforever.reader.utils.FusionField;
import com.shixforever.reader.view.PageWidget;
import com.shixforever.reader.db.DBManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class BookActivity extends Activity implements OnSeekBarChangeListener,
        OnClickListener {

    private static final String TAG = "BookActivity";

    public static final int DIR_CODE = 123;

    public static final String DIR_KEY = "begin";

    public static final String DIR_NAME = "filepath";

    private PageWidget mPageWidget;

    Bitmap mCurPageBitmap, mNextPageBitmap;

    Canvas mCurPageCanvas, mNextPageCanvas;

    BookPageFactory pagefactory;

    private String filepath;

    private int width;

    private int hight;

    private static int begin = 0;// 记录的书籍开始位置

    private SharedPreferences sp;

    private SharedPreferences.Editor editor;

    private int light; // 亮度值

    private int size; // 字体大小

    private static String word = "";// 记录当前页面的文字

    // catch路径
    private String filecatchpath = "/data/data/"
            + FusionField.baseActivity.getPackageName() + "/";

    private PopupWindow mPopupWindow, mToolpop, mToolpop1, mToolpop2,
            mToolpop3, mToolpop4;

    private View popupwindwow, toolpop, toolpop1, toolpop2, toolpop3, toolpop4, topBar;

    private SeekBar seekBar1, seekBar2, seekBar4;

    private Boolean show = false;// popwindow是否显示

    private int a = 0, b = 0;// 记录toolpop的位置

    private ImageButton imageBtn4_1, imageBtn4_2;

    private boolean isNight; // 亮度模式,白天和晚上

    private TextView markEdit4;

    int defaultSize = 30;

    // int readHeight; // 电子书显示高度
    private Context mContext = null;

    private DBManager mgr;

    private List<BookMark> bookmarks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mgr = new DBManager(this);

        mContext = getBaseContext();

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        hight = dm.heightPixels;

        sp = getSharedPreferences("config", MODE_PRIVATE);
        editor = sp.edit();
        // 读取SP记录
        begin = sp.getInt(filepath + "begin", 0);
        light = sp.getInt("light", 5);
        isNight = sp.getBoolean("night", false);
        size = sp.getInt("size", defaultSize);

        mPageWidget = new PageWidget(this, width, hight);
        // 当前页
        mCurPageBitmap = Bitmap.createBitmap(width, hight,
                Bitmap.Config.ARGB_8888);
        // 下一页
        mNextPageBitmap = Bitmap.createBitmap(width, hight,
                Bitmap.Config.ARGB_8888);
        // 画布
        mCurPageCanvas = new Canvas(mCurPageBitmap);
        mNextPageCanvas = new Canvas(mNextPageBitmap);
        setContentView(R.layout.read);
        RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.readlayout);
        topBar = findViewById(R.id.top_bar);
        rlayout.addView(mPageWidget);

        // 工厂
        pagefactory = new BookPageFactory(this, width, hight);

        BookFile bookFile = (BookFile) getIntent().getExtras().getSerializable("path");
        filepath = bookFile.name;
        initTopBar();

        // 阅读背景
        pagefactory.setBgBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.color.bg_read_day));

        try {
            if (bookFile.flag.equals("1")) {
                pagefactory.openbook(bookFile.path, begin);
            } else {
                pagefactory.openbook(filecatchpath + "catch.txt", begin);
            }
            pagefactory.setM_fontSize(size);
            pagefactory.onDraw(mCurPageCanvas);
        } catch (IOException e1) {
            e1.printStackTrace();
            Toast.makeText(this, "no find file", Toast.LENGTH_SHORT).show();
        }

        pagefactory.setBgBitmap(BitmapFactory.decodeResource(
                this.getResources(), R.color.bg_read_day));

        mPageWidget.setBitmaps(mCurPageBitmap, mCurPageBitmap);

        mPageWidget.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {

                boolean ret;
                if (v == mPageWidget) {
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        if (e.getY() > hight) {
                            return false;
                        }
                        mPageWidget.abortAnimation();
                        mPageWidget.calcCornerXY(e.getX(), e.getY());
                        pagefactory.onDraw(mCurPageCanvas);
                        if (mPageWidget.DragToRight()) {
                            try {
                                pagefactory.prePage();
                                begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
                                word = pagefactory.getFirstLineText();// 获取当前阅读位置的首行文字
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            if (pagefactory.isfirstPage()) {
                                Toast.makeText(mContext, "当前是第一页",
                                        Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            pagefactory.onDraw(mNextPageCanvas);
                        } else {
                            try {
                                pagefactory.nextPage();
                                begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
                                word = pagefactory.getFirstLineText();// 获取当前阅读位置的首行文字
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            if (pagefactory.islastPage()) {
                                Toast.makeText(mContext, "已经是最后一页了",
                                        Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            pagefactory.onDraw(mNextPageCanvas);
                        }
                        mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
                    }
                    editor.putInt(filepath + "begin", begin).commit();
                    ret = mPageWidget.doTouchEvent(e);
                    return ret;
                }
                return false;
            }
        });
        setPop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 保存记录点
            if (mToolpop.isShowing()) {
                popDismiss();
                return false;
            }
            if (show) {
                getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                show = false;
                mPopupWindow.dismiss();
                popDismiss();
                return false;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (show) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                show = false;
                mPopupWindow.dismiss();
                popDismiss();

            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                show = true;
                pop();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mgr.closeDB();
        saveSp();
    }

    /**
     * 刷新界面
     */
    public void postInvalidateUI() {
        mPageWidget.abortAnimation();
        pagefactory.onDraw(mCurPageCanvas);
        try {
            pagefactory.currentPage();
            begin = pagefactory.getM_mbBufBegin();// 获取当前阅读位置
            word = pagefactory.getFirstLineText();// 获取当前阅读位置的首行文字
        } catch (IOException e1) {
        }

        pagefactory.onDraw(mNextPageCanvas);

        mPageWidget.setBitmaps(mCurPageBitmap, mNextPageBitmap);
        mPageWidget.postInvalidate();
    }

    /**
     * 初始化所有POPUPWINDOW
     */
    private void setPop() {
        popupwindwow = this.getLayoutInflater().inflate(R.layout.bookpop, null);
        toolpop = this.getLayoutInflater().inflate(R.layout.toolpop, null);
        mPopupWindow = new PopupWindow(popupwindwow, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        mToolpop = new PopupWindow(toolpop, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        toolpop1 = this.getLayoutInflater().inflate(R.layout.tool11, null);
        mToolpop1 = new PopupWindow(toolpop1, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        toolpop2 = this.getLayoutInflater().inflate(R.layout.tool22, null);
        mToolpop2 = new PopupWindow(toolpop2, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        toolpop3 = this.getLayoutInflater().inflate(R.layout.tool33, null);
        mToolpop3 = new PopupWindow(toolpop3, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        toolpop4 = this.getLayoutInflater().inflate(R.layout.tool44, null);
        mToolpop4 = new PopupWindow(toolpop4, LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
    }

    /**
     * popupwindow的弹出,工具栏
     */
    public void pop() {

        topBar.setVisibility(View.VISIBLE);
        mPopupWindow.showAtLocation(mPageWidget, Gravity.BOTTOM, 0, 0);
        TextView btnDirectory = (TextView) popupwindwow.findViewById(R.id.btn_directory);
        TextView btnProgress = (TextView) popupwindwow.findViewById(R.id.btn_progress);
        TextView btnTextSize = (TextView) popupwindwow.findViewById(R.id.btn_text_size);
        TextView btnBrightness = (TextView) popupwindwow.findViewById(R.id.btn_brightness);
        final TextView btnNight = (TextView) popupwindwow.findViewById(R.id.btn_night);
        //        if (isNight) {
        //            btnNight.setCompoundDrawables(null, getResources().getDrawable(R.drawable.btn_night), null, null);
        //            btnNight.setCompoundDrawablePadding(6);
        //            btnNight.setText(getString(R.string.bookpop_night));
        //        } else {
        //            btnNight.setCompoundDrawables(null, getResources().getDrawable(R.drawable.btn_day), null, null);
        //            btnNight.setCompoundDrawablePadding(6);
        //            btnNight.setText(getString(R.string.bookpop_day));
        //        }

        btnDirectory.setOnClickListener(this);
        btnProgress.setOnClickListener(this);
        btnTextSize.setOnClickListener(this);
        btnBrightness.setOnClickListener(this);
        btnNight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNight) {
                    btnNight.setCompoundDrawables(null, getResources().getDrawable(R.drawable.btn_night), null, null);
                    //                    btnNight.setCompoundDrawablePadding(6);
                    btnNight.setText(getString(R.string.bookpop_night));
                    isNight = true;
                    //TODO
                } else {
                    btnNight.setCompoundDrawables(null, getResources().getDrawable(R.drawable.btn_day), null, null);
                    //                    btnNight.setCompoundDrawablePadding(6);
                    btnNight.setText(getString(R.string.bookpop_day));
                    isNight = false;
                    //TODO
                }
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekBar1:
                pagefactory.setTextSize(seekBar1.getProgress() + 15);
                System.out.println(seekBar1.getProgress());
                pagefactory.onDraw(mCurPageCanvas);
                pagefactory.onDraw(mNextPageCanvas);
                mPageWidget.invalidate();
                size = seekBar1.getProgress() + 15;
                break;
            case R.id.seekBar2:
                // 取得当前进度
                int tmpInt = seekBar2.getProgress();
                // 当进度小于80时，设置成80，防止太黑看不见的后果。
                if (tmpInt < 80) {
                    tmpInt = 80;
                }
                WindowManager.LayoutParams lp = BookActivity.this.getWindow()
                        .getAttributes();
                lp.screenBrightness = (float) tmpInt * (1f / 255f);
                BookActivity.this.getWindow().setAttributes(lp);
                light = tmpInt;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DIR_CODE:
                if (data != null) {
                    int markBegin = data.getExtras().getInt(DIR_KEY);
                    if (markBegin > 0) {
                        try {
                            pagefactory.nextPage();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        pagefactory.setM_mbBufEnd(markBegin);
                        pagefactory.setM_mbBufBegin(markBegin);
                        pagefactory.onDraw(mNextPageCanvas);
                        mPageWidget.setBitmaps(mCurPageBitmap,
                                mNextPageBitmap);
                        mPageWidget.invalidate();
                        postInvalidateUI();
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ib_back_top:
                finish();
                break;

            case R.id.ib_add_mark_top:
                BookMark mark = new BookMark();
                mark.name = filepath;
                mark.begin = begin;
                mark.time = getStringCurrentDate();
                if (word.trim().equals("")) {
                    mark.word = pagefactory.getSecLineText().trim();
                } else {
                    mark.word = word.trim();
                }
                mark.word += "\n" + mark.time;
                mgr.addMarks(mark);
                Toast.makeText(getApplication(), "书签添加成功", Toast.LENGTH_SHORT).show();
                break;

            // 目录
            case R.id.btn_directory:
                //                a = 3;
                //                setToolPop(a);
                Intent intent = new Intent(this, DirectoryActivity.class);
                intent.putExtra(DIR_NAME, filepath);
                startActivityForResult(intent, DIR_CODE);
                break;

            // 进度
            case R.id.btn_progress:
                a = 4;
                setToolPop(a);
                break;

            // 字体按钮
            case R.id.btn_text_size:
                a = 1;
                setToolPop(a);
                break;
            // 亮度按钮
            case R.id.btn_brightness:
                a = 2;
                setToolPop(a);
                break;

            default:
                break;
        }
    }

    /**
     * 设置popupwindow的显示与隐藏
     *
     * @param a
     */
    public void setToolPop(int a) {
        if (a == b && a != 0) {
            if (mToolpop.isShowing()) {
                popDismiss();
            } else {
                mToolpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                        width * 45 / 320);
                // Font settings
                if (a == 1) {
                    mToolpop1.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                            width * 45 / 320);
                    seekBar1 = (SeekBar) toolpop1.findViewById(R.id.seekBar1);
                    seekBar1.setMax(100);
                    seekBar1.setProgress((size - 15));
                    seekBar1.setOnSeekBarChangeListener(this);
                }
                // adjusting brightness
                if (a == 2) {
                    mToolpop2.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                            width * 45 / 320);
                    seekBar2 = (SeekBar) toolpop2.findViewById(R.id.seekBar2);
                    seekBar2.setProgress(light);
                    seekBar2.setOnSeekBarChangeListener(this);
                }
                // Bookmarks button
                if (a == 3) {
                    mToolpop3.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                            width * 45 / 320);
                    ImageButton imageBtn3_1 = (ImageButton) toolpop3
                            .findViewById(R.id.ib_add_mark);
                    ImageButton imageBtn3_2 = (ImageButton) toolpop3
                            .findViewById(R.id.ib_all_mark);
                    imageBtn3_1.setOnClickListener(this);
                    imageBtn3_2.setOnClickListener(this);
                }
                // 当点击跳转按钮
                if (a == 4) {
                    mToolpop4.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                            width * 45 / 320);
                    imageBtn4_1 = (ImageButton) toolpop4
                            .findViewById(R.id.imageBtn4_1);
                    imageBtn4_2 = (ImageButton) toolpop4
                            .findViewById(R.id.imageBtn4_2);
                    seekBar4 = (SeekBar) toolpop4.findViewById(R.id.seekBar4);
                    markEdit4 = (TextView) toolpop4
                            .findViewById(R.id.markEdit4);
                    // begin = sp.getInt(bookPath + "begin", 1);
                    float fPercent = (float) (begin * 1.0 / pagefactory
                            .getM_mbBufLen());
                    DecimalFormat df = new DecimalFormat("#0");
                    String strPercent = df.format(fPercent * 100) + "%";
                    markEdit4.setText(strPercent);
                    seekBar4.setProgress(Integer.parseInt(df
                            .format(fPercent * 100)));
                    seekBar4.setOnSeekBarChangeListener(this);
                    imageBtn4_1.setOnClickListener(this);
                    imageBtn4_2.setOnClickListener(this);
                }
            }
        } else {
            if (mToolpop.isShowing()) {
                // 对数据的记录
                popDismiss();
            }
            mToolpop.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                    width * 45 / 320);
            // 点击字体按钮
            if (a == 1) {
                mToolpop1.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                        width * 45 / 320);
                seekBar1 = (SeekBar) toolpop1.findViewById(R.id.seekBar1);
                seekBar1.setMax(100);
                seekBar1.setProgress(size - 15);
                seekBar1.setOnSeekBarChangeListener(this);
            }
            // 点击亮度按钮
            if (a == 2) {
                mToolpop2.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                        width * 45 / 320);
                seekBar2 = (SeekBar) toolpop2.findViewById(R.id.seekBar2);
                seekBar2.setMax(255);
                // 取得当前亮度
                int normal = Settings.System.getInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 255);
                // 进度条绑定当前亮度
                seekBar2.setProgress(normal);
                seekBar2.setProgress(light);

                setReadBg();
                seekBar2.setOnSeekBarChangeListener(this);

            }
            // 点击书签按钮
            if (a == 3) {
                mToolpop3.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                        width * 45 / 320);
                ImageButton imageBtn3_1 = (ImageButton) toolpop3
                        .findViewById(R.id.ib_add_mark);
                ImageButton imageBtn3_2 = (ImageButton) toolpop3
                        .findViewById(R.id.ib_all_mark);
                imageBtn3_1.setOnClickListener(this);
                imageBtn3_2.setOnClickListener(this);
            }
            // 点击跳转按钮
            if (a == 4) {
                mToolpop4.showAtLocation(mPageWidget, Gravity.BOTTOM, 0,
                        width * 45 / 320);
                imageBtn4_1 = (ImageButton) toolpop4
                        .findViewById(R.id.imageBtn4_1);
                imageBtn4_2 = (ImageButton) toolpop4
                        .findViewById(R.id.imageBtn4_2);
                seekBar4 = (SeekBar) toolpop4.findViewById(R.id.seekBar4);
                markEdit4 = (TextView) toolpop4.findViewById(R.id.markEdit4);
                // jumpPage = sp.getInt(bookPath + "jumpPage", 1);
                float fPercent = (float) (begin * 1.0 / pagefactory
                        .getM_mbBufLen());
                DecimalFormat df = new DecimalFormat("#0");
                String strPercent = df.format(fPercent * 100) + "%";
                markEdit4.setText(strPercent);
                seekBar4.setProgress(Integer.parseInt(df.format(fPercent * 100)));
                seekBar4.setOnSeekBarChangeListener(this);
                imageBtn4_1.setOnClickListener(this);
                imageBtn4_2.setOnClickListener(this);
            }
        }
        // 记录上次点击的是哪一个
        b = a;
    }

    /**
     * 关闭55个弹出pop
     */
    public void popDismiss() {
        mToolpop.dismiss();
        mToolpop1.dismiss();
        mToolpop2.dismiss();
        mToolpop3.dismiss();
        mToolpop4.dismiss();
        topBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void initTopBar() {
        ImageButton back = (ImageButton) findViewById(R.id.ib_back_top);
        ImageButton addMark = (ImageButton) findViewById(R.id.ib_add_mark_top);
        TextView name = (TextView) findViewById(R.id.tv_name_top);
        name.setText(filepath);
        back.setOnClickListener(this);
        addMark.setOnClickListener(this);
    }

    private void setReadBg() {
        if (isNight) {
            pagefactory.setBgBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.color.bg_read_night));
        } else {
            pagefactory.setBgBitmap(BitmapFactory.decodeResource(
                    this.getResources(), R.color.bg_read_day));
        }
    }

    private void saveSp() {
        editor.putInt("light", light);
        editor.putBoolean("night", isNight);
        editor.putInt("size", size);
        editor.commit();
    }

    public static String getStringCurrentDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }
}
