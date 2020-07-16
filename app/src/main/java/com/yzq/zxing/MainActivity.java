package com.yzq.zxing;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.util.List;

import static android.view.View.GONE;


/**
 * @author: yzq
 * @date: 2017/10/26 15:17
 * @declare :
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtContent;
    private ImageView imgQrcode;
    private int REQUEST_CODE_SCAN = 111;
//    private ImageView contentIvWithLogo;
    private ClipboardManager clipboard = null;
    private CheckBox cbxWithLogo;
    boolean isExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView() {
        /*扫描按钮*/
        final Button btnScan = findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);

        final Button btnCopy = findViewById(R.id.btnCopy);
        btnCopy.setOnClickListener(this);

        final Button btnPaste = findViewById(R.id.btnPaste);
        btnPaste.setOnClickListener(this);

        /*要生成二维码的输入框*/
//        txtContent = findViewById(R.id.txtContent);
        /*生成按钮*/
        final Button btnCreateQrcode = findViewById(R.id.btnCreateQrcode);
        btnCreateQrcode.setOnClickListener(this);
        /*生成的图片*/
        //imgQrcode = findViewById(R.id.imgQrcode);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.toolbar_Title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtContent = (EditText) findViewById(R.id.txtContent);
        imgQrcode = (ImageView) findViewById(R.id.imgQrcode);

        cbxWithLogo = (CheckBox) findViewById(R.id.cbxWithLogo);

        txtContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        txtContent.setEnabled(false);//默认为识别，所以默认不可编辑
        txtContent.setHint(R.string.txtContent_verifyText);
        btnCreateQrcode.setVisibility(View.GONE);//隐藏创建按钮
        cbxWithLogo.setVisibility(View.GONE);
        imgQrcode.setVisibility(View.GONE);
        btnScan.setVisibility(View.VISIBLE);
        btnCopy.setVisibility(View.VISIBLE);
        btnPaste.setVisibility(View.VISIBLE);

        RadioGroup rg = (RadioGroup) findViewById(R.id.rgOperType);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.rbtnVirfiy)
                {
                    //识别
                    txtContent.setEnabled(false);
                    txtContent.setHint(R.string.txtContent_verifyText);
                    btnCreateQrcode.setVisibility(View.GONE);//隐藏创建按钮
                    cbxWithLogo.setVisibility(View.GONE);
                    imgQrcode.setVisibility(View.GONE);
                    btnScan.setVisibility(View.VISIBLE);
                    btnCopy.setVisibility(View.VISIBLE);
                    btnPaste.setVisibility(View.VISIBLE);
                }
                else
                {
                    //创建
                    txtContent.setEnabled(true);
                    txtContent.setHint(R.string.txtContent_createText);
                    btnCreateQrcode.setVisibility(View.VISIBLE);//显示创建按钮
                    cbxWithLogo.setVisibility(View.VISIBLE);
                    imgQrcode.setVisibility(View.VISIBLE);
                    btnScan.setVisibility(View.GONE);
                    btnCopy.setVisibility(View.GONE);
                    btnPaste.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Bitmap bitmap = null;
        switch (v.getId()) {
            case R.id.btnScan:
                AndPermission.with(this)
                        .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                        .onGranted(new Action() {
                            @Override
                            public void onAction(List<String> permissions) {
                                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                                /*ZxingConfig是配置类
                                 *可以设置是否显示底部布局，闪光灯，相册，
                                 * 是否播放提示音  震动
                                 * 设置扫描框颜色等
                                 * 也可以不传这个参数
                                 * */
//                                ZxingConfig config = new ZxingConfig();
//                                config.setPlayBeep(true);//是否播放扫描声音 默认为true
//                                config.setShake(true);//是否震动  默认为true
//                                config.setDecodeBarCode(true);//是否扫描条形码 默认为true
//                                config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
//                                config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
//                                config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
//                                config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
//                                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                                startActivityForResult(intent, REQUEST_CODE_SCAN);
                            }
                        })
                        .onDenied(new Action() {
                            @Override
                            public void onAction(List<String> permissions) {
                                Uri packageURI = Uri.parse("package:" + getPackageName());
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                startActivity(intent);

                                Toast.makeText(MainActivity.this, "没有权限无法扫描呦", Toast.LENGTH_LONG).show();
                            }
                        }).start();
                break;
            case R.id.btnCreateQrcode://生成二维码
                imgQrcode.setImageBitmap(null);
                String contentEtString = txtContent.getText().toString().trim();
                if (TextUtils.isEmpty(contentEtString)) {
                    Toast.makeText(this, "请输入要生成二维码图片的字符串", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bitmap logo = null;
                try {
                    if(cbxWithLogo.isChecked())
                    {
                        //获取Logo
                        logo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                        if (logo == null) {
                            Toast.makeText(this, "Logo图片获取失败", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    //生成二维码
                    bitmap = CodeCreator.createQRCode(contentEtString, 400, 400, logo);
                } catch (WriterException e) {
                    Toast.makeText(this, "生成二维码异常", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                if (bitmap == null) {
                    Toast.makeText(this, "生成二维码为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                imgQrcode.setImageBitmap(bitmap);
                break;
            case R.id.btnCopy://复制到剪切板
                contentEtString = txtContent.getText().toString().trim();
                if (TextUtils.isEmpty(contentEtString)) {
                    Toast.makeText(this, "请输入要生成二维码图片的字符串", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (null == clipboard) {
                    clipboard = (ClipboardManager) getSystemService(this.CLIPBOARD_SERVICE);
                }
                if (clipboard == null) {
                    Toast.makeText(this, "clipboard is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                ClipData clip = ClipData.newPlainText("qrcode",contentEtString);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "已复制到剪切板", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnPaste://粘贴
                if (null == clipboard) {
                    clipboard = (ClipboardManager) getSystemService(this.CLIPBOARD_SERVICE);
                }
                if (clipboard == null) {
                    Toast.makeText(this, "clipboard is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder resultString = new StringBuilder();
                // 检查剪贴板是否有内容
                if (!clipboard.hasPrimaryClip()) {
                    Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                ClipData clipData = clipboard.getPrimaryClip();
                int count = 0;
                if (clipData != null) {
                    count = clipData.getItemCount();
                }

                for (int i = 0; i < count; ++i) {
                    ClipData.Item item = clipData.getItemAt(i);
                    CharSequence str = item.coerceToText(this);
                    resultString.append(str);
                }
                txtContent.setText(resultString.toString());
                Toast.makeText(this, "粘贴成功", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "未知处理逻辑", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                //result.setText("扫描结果为：" + content);
                txtContent.setText(content);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public void exit(){
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            System.exit(0);
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            isExit = false;
        }
    };


}
