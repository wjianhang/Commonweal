package com.goldenratio.commonweal.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.goldenratio.commonweal.MyApplication;
import com.goldenratio.commonweal.R;
import com.goldenratio.commonweal.bean.Bid;
import com.goldenratio.commonweal.bean.Deposit;
import com.goldenratio.commonweal.bean.Good;
import com.goldenratio.commonweal.bean.User_Profile;
import com.goldenratio.commonweal.util.MD5Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 输入支付密码
 *
 * @author lining
 */
public class PopEnterPassword extends PopupWindow {

    private PasswordView pwdView;

    private View mMenuView;

    private Activity mContext;
    private TextView mTvType;
    private TextView mTvRemark;
    private TextView mTvCoin;
    private String mUserId;

    public PopEnterPassword(Activity context, String type, final String coin, String remark) {
        //设置6位数密码
        super(context);
        this.mContext = context;
        initView();

        mTvType.setText(type);
        mTvCoin.setText(coin);
        mTvRemark.setText(remark);
        //添加密码输入完成的响应
        pwdView.setOnFinishInput(new OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                updateUserSixPwd2MySql(MD5Util.createMD5(password));
            }
        });
    }

    private void updateUserSixPwd2MySql(String md5Pwd) {
        String url = "http://123.206.89.67/WebService1.asmx/UpdateUserSixPwdByObjectId";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("ObjectId", mUserId)
                .add("SixPwd", md5Pwd)
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                final String e1 = e.getMessage();
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "密码设置失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.contains("success")) {
                            dismiss();
                            Toast.makeText(mContext, "密码设置成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Kiuber_LOG", "fail: " + result);
                        }
                    }
                });
            }
        });
    }

    public PopEnterPassword(final Activity context, final String userCoin, final String userPwd, String type
            , String coin, String remark, final String goodObjectId) {
        //支付保证金验证
        super(context);
        this.mContext = context;
        initView();

        mTvType.setText(type);
        mTvCoin.setText(coin);
        mTvRemark.setText(remark);
        //添加密码输入完成的响应
        pwdView.setOnFinishInput(new OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                if (MD5Util.createMD5(password).equals(userPwd)) {
                    User_Profile user_profile = new User_Profile();
                    user_profile.setObjectId(mUserId);

                    Deposit deposit = new Deposit();
                    deposit.setD_User(user_profile);
                    deposit.setD_GoodId(goodObjectId);
                    deposit.setD_Coin("0.01");
                    deposit.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                updateUserCoin(userCoin);
                            } else {
                                Toast.makeText(mContext, "保证金支付失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(context, "密码错误", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });
    }

    public PopEnterPassword(final Activity context, String type, final String coin, String remark,
                            final String userObjectId, final String goodObjectId, int a) {
        //出价验证
        super(context);
        this.mContext = context;
        initView();

        mTvType.setText(type);
        mTvCoin.setText(coin);
        mTvRemark.setText(remark);
        //添加密码输入完成的响应
        pwdView.setOnFinishInput(new OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                querySixPwdFromMysql(password, userObjectId, goodObjectId, coin, coin);
            }
        });
    }

    private void saveBidToBmob(final String user, final String goodId, String bidCoin, final String nowCoin) {
        User_Profile user_profile = new User_Profile();
        user_profile.setObjectId(user);
        Good good = new Good();
        good.setObjectId(goodId);

        Bid bid = new Bid();
        bid.setBid_User(user_profile);
        bid.setBid_Good(good);
        bid.setBid_Coin(bidCoin);
        bid.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    updateGood2Bmob(user, goodId, objectId, nowCoin);
                } else {
                    Log.d("Kiuber_LOG", "done: " + e.getMessage());
                }
            }
        });
    }

    private void updateGood2Bmob(final String user_id, final String good_id, final String bid_id, String coin) {
        User_Profile user_profile = new User_Profile();
        user_profile.setObjectId(user_id);
        Bid bid = new Bid();
        bid.setObjectId(bid_id);

        Good good = new Good();
        good.setGood_NowCoin(coin);
        good.setGood_NowBidUser(user_profile);
        good.setGood_Bid(bid);
        good.setGood_IsFirstBid(false);
        good.update(good_id, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    updateGood2MySql(good_id, user_id);
                } else {
                    Log.d("Kiuber_LOG", "done: " + e.getMessage());
                }
            }
        });
    }

    private void updateGood2MySql(String good, final String user) {
        String url = "http://123.206.89.67/WebService1.asmx/UpdateGoodNowBid";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Good_ObjectId", good)
                .add("User_ObjectId", user)
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                final String e1 = e.getMessage();
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, e1, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.contains("success")) {
                            dismiss();
                            mContext.finish();
                            mContext.startActivity(mContext.getIntent());
                            Toast.makeText(mContext, "出价成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Kiuber_LOG", "fail: " + result);
                        }
                    }
                });
            }
        });
    }

    private void initView() {

        mUserId = ((MyApplication) mContext.getApplication()).getObjectID();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.pop_enter_password, null);
        pwdView = (PasswordView) mMenuView.findViewById(R.id.pwd_view);

        mTvType = (TextView) mMenuView.findViewById(R.id.tv_type);
        mTvCoin = (TextView) mMenuView.findViewById(R.id.tv_coin);
        mTvRemark = (TextView) mMenuView.findViewById(R.id.tv_remark);
        // 监听X关闭按钮
        pwdView.getImgCancel().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 监听键盘上方的返回
        pwdView.getVirtualKeyboardView().getLayoutBack().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.pop_add_ainm);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x66000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
    }


    public PopEnterPassword(final Activity context, String type, final String coin, String remark, final String user, final String order, final String userCoin, int aa) {
        super(context);
        //订单支付
        this.mContext = context;
        this.mContext = context;
        initView();

        mTvType.setText(type);
        mTvCoin.setText(coin);
        mTvRemark.setText(remark);
        //添加密码输入完成的响应
        pwdView.setOnFinishInput(new OnPasswordInputFinish() {
            @Override
            public void inputFinish(String password) {
                querySixPwdFromMysql(password, user, order, userCoin);
            }
        });
    }

    private void querySixPwdFromMysql(final String pwd, final String user, final String
            orderId, final String userCoin) {
        String url = "http://123.206.89.67/WebService1.asmx/QueryUserSixPwdByObjectId";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("ObjectId", user)
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                final String e1 = e.getMessage();
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, e1, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                mContext.runOnUiThread(new Runnable() {

                    private String sixPwd;

                    @Override
                    public void run() {
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = new JSONArray(result);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                sixPwd = jsonObject.getString("User_SixPwd");
                            }
                            if (MD5Util.createMD5(pwd).equals(sixPwd)) {
                                updateOrderStatus(orderId, user, userCoin);
                            } else {
                                Toast.makeText(mContext, "密码错误", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        } catch (JSONException e) {
                            Log.d("Kiuber_LOG", e.getMessage() + request);
                        }
                    }
                });
            }
        });
    }

    private void updateOrderStatus(String order, String user, String userCoin) {
        String url = "http://123.206.89.67/WebService1.asmx/UpdateOrderStatus";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("ObjectId", order)
                .add("UseId", user)
                .add("UserCoin", userCoin)
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                final String e1 = e.getMessage();
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, e1, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                mContext.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (result.equals("success")) {
                            Toast.makeText(mContext, "支付成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "支付失败", Toast.LENGTH_SHORT).show();
                        }
                        dismiss();
                    }
                });
            }
        });
    }

    private void querySixPwdFromMysql(final String pwd, final String user, final String
            goodId, final String bidCoin, final String nowCoin) {
        String url = "http://123.206.89.67/WebService1.asmx/QueryUserSixPwdByObjectId";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("ObjectId", user)
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                final String e1 = e.getMessage();
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, e1, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String result = response.body().string();
                mContext.runOnUiThread(new Runnable() {

                    private String sixPwd;

                    @Override
                    public void run() {
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = new JSONArray(result);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                sixPwd = jsonObject.getString("User_SixPwd");
                            }
                            if (MD5Util.createMD5(pwd).equals(sixPwd)) {
                                saveBidToBmob(user, goodId, bidCoin, nowCoin);
                            } else {
                                Toast.makeText(mContext, "密码错误", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        } catch (JSONException e) {
                            Log.d("Kiuber_LOG", e.getMessage() + request);
                        }
                    }
                });
            }
        });
    }

    private void updateUserCoin(String sumCoin) {
        String url = "http://123.206.89.67/WebService1.asmx/UpdateUserCoinByObjectId";
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("ObjectId", mUserId)
                .add("UserCoin", sumCoin + "")
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                final String e1 = e.getMessage();
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, e1, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "保证金收取成功", Toast.LENGTH_SHORT).show();
                        mContext.finish();
                        mContext.startActivity(mContext.getIntent());
                    }
                });
            }
        });
    }

}
