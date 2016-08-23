package com.goldenratio.commonweal.ui.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.goldenratio.commonweal.R;
import com.goldenratio.commonweal.iview.IMySqlManager;
import com.goldenratio.commonweal.iview.impl.MySqlManagerImpl;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2016/7/3.
 * 捐赠时弹出的页面
 */

public class HelpDonateActivity extends Activity implements IMySqlManager {

    @BindView(R.id.checkBox6)
    RadioButton mCheckBox6;
    @BindView(R.id.rg_money)
    RadioGroup mRgMoney;
    @BindView(R.id.et_other)
    EditText mEtOther;
    @BindView(R.id.avail)
    TextView mAvail;

    private String mCoin;

    private String mStrUserCoin;
    private String mSixPwd;

    private MySqlManagerImpl mySqlManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_donate);
        ButterKnife.bind(this);

        mCoin = "10";

        mySqlManager = new MySqlManagerImpl(this, this);
        mySqlManager.queryUserCoinAndSixPwdByObjectId(null, null);

        mRgMoney.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == mCheckBox6.getId()) {
                    mEtOther.setVisibility(View.VISIBLE);
                } else {
                    mEtOther.setVisibility(View.GONE);
                    mCoin = ((RadioButton) findViewById(checkedId)).getText().toString();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.iv_back, R.id.tv_donate_mo})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_donate_mo:
                if (mEtOther.getVisibility() == View.VISIBLE) {
                    mCoin = mEtOther.getText().toString();
                }
                //是否已设置支付密码
                if (TextUtils.isEmpty(mSixPwd) || mSixPwd.equals("0")) {
                    Toast.makeText(this, "请先设置六位数密码", Toast.LENGTH_SHORT).show();
                    showPayKeyBoard2();
                } else {
                    double usercoin = Double.valueOf(mStrUserCoin);
                    final double choiceCoin = Double.valueOf(mCoin);

                    final double poor = choiceCoin - usercoin;  //所缺公益币,充值
                    final double sxf = (poor / 10) * 0.05;  //手续费
                    final double money = (poor / 10) * (1 + 0.05);
                    //选择的币数大于用户现有的金币，公益币不足，提示充值
                    if (poor > 0 || poor == 0) {
                        final AlertDialog.Builder builder =
                                new AlertDialog.Builder(HelpDonateActivity.this);
                        builder.setMessage("账号公益币：" + usercoin + "，还缺" + poor + "公益币，您将花费"
                                + money + "元（由于个人开发团队限制，" +
                                "平台收取5%的手续费）");
                        builder.setPositiveButton("充值", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //充值金额包括公益币、充值Bmob收取的0.05的费率。
                                mySqlManager.pay(false, money, choiceCoin, poor + "");
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    } else {
                        showPayKeyBoard1(mSixPwd);
                    }
                }
                break;
        }
    }

    @Override
    public void pay(boolean alipayOrWechatPay, double price, final double allCoin, String changeCoin) {

    }

    public void showPayKeyBoard1(String inputPwd) {
        mySqlManager = new MySqlManagerImpl(this, this, "请输入支付密码", "-" + mCoin, "您将支付" + mCoin + "公益币");
        mySqlManager.showSixPwdOnFinishInput(inputPwd, 1);
    }

    public void showPayKeyBoard2() {
        mySqlManager = new MySqlManagerImpl(this, this, "设置六位数密码", "", "新密码");
        mySqlManager.showSixPwdOnFinishInput("", 0);
    }

    public void showSixPwdOnFinishInput(String sixPwd, int event) {
        Log.i("六位数密码", "showSixPwdOnFinishInput: " + sixPwd);
        if (event == 0) {
            mySqlManager.updateUserSixPwdByObjectId(sixPwd);
        } else {
            double usercoin = Double.valueOf(mStrUserCoin);
            final double choiceCoin = Double.valueOf(mCoin);
            mySqlManager.updateUserCoinByObjectId(usercoin - choiceCoin + "", mCoin);
        }
    }

    @Override
    public boolean updateUserCoinByObjectId(String sumCoin, String changeCoin) {
        mStrUserCoin = sumCoin;
        mAvail.setText(mStrUserCoin);
        return false;
    }

    @Override
    public boolean queryUserCoinAndSixPwdByObjectId(String mUserCoin, String sixPwd) {
        mStrUserCoin = mUserCoin;
        mSixPwd = sixPwd;
        mAvail.setText(mUserCoin);
        return false;
    }

    @Override
    public boolean updateUserSixPwdByObjectId(String sixPwd) {
        mSixPwd = sixPwd;
        return false;
    }
}
