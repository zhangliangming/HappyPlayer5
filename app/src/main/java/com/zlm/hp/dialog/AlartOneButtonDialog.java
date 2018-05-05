package com.zlm.hp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.zlm.hp.ui.R;
import com.zlm.hp.widget.dialog.AlartDialogCenterButton;


public class AlartOneButtonDialog extends Dialog {

    private ButtonDialogListener listener;
    /**
     * 提示
     */
    private TextView tipTextView;
    /**
     * 按钮文字提示
     */
    private TextView centerTextView;

    private AlartDialogCenterButton alartDialogCenterButton;

    public AlartOneButtonDialog(Context context,
                                ButtonDialogListener listener) {
        super(context, R.style.alertDialog);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_alert_dialog_onebutton);

        WindowManager windowManager = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth(); // 设置宽度
        getWindow().setAttributes(lp);

        setCanceledOnTouchOutside(false);

        tipTextView = findViewById(R.id.tipCom);
        centerTextView = findViewById(R.id.centerTip);

        alartDialogCenterButton = findViewById(R.id.alartDialogCenterButton);
        alartDialogCenterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (listener != null) {
                    dismiss();
                    listener.ButtonClick();
                }
            }
        });
    }

    private Handler mhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            show();
            String[] text = (String[]) msg.obj;
            tipTextView.setText(text[0]);
            centerTextView.setText(text[1]);
        }

    };

    /**
     * 提示
     */
    public void showDialog(String tipText, String centerText) {
        String[] text = {tipText, centerText};
        Message msg = new Message();
        msg.obj = text;
        mhandler.sendMessage(msg);
    }

    public interface ButtonDialogListener {
        void ButtonClick();

    }

}
