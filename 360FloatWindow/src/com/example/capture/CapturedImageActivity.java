package com.example.capture;

import java.io.File;

import com.example.floatwindow.R;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class CapturedImageActivity extends Activity{
	
	static final String BASE_PATH = Environment.getExternalStorageDirectory() + "/screens_shot";
	
	private static Bitmap mBitmap;
	
    /** ��װ�� "access_token"��"expires_in"��"refresh_token"�����ṩ�����ǵĹ�����  */
    private Oauth2AccessToken mAccessToken;
	
    /** ΢�� Web ��Ȩ�࣬�ṩ��½�ȹ���  */
    private WeiboAuth mWeiboAuth;
    
    /** ע�⣺SsoHandler ���� SDK ֧�� SSO ʱ��Ч */
    private SsoHandler mSsoHandler;
    
    private MyRequestListener requestListener = new MyRequestListener();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_image);
        
        // ����΢��ʵ��
        // mWeiboAuth = new WeiboAuth(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
              
        ImageView imgView = (ImageView)findViewById(R.id.imageView);
        imgView.setImageBitmap(mBitmap);

        ImageButton imgBtn = (ImageButton)findViewById(R.id.imageButton);
        imgBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO �Զ����ɵķ������
				Intent intent=new Intent(Intent.ACTION_SEND);   
                intent.setType("image/*");   
                intent.putExtra(Intent.EXTRA_SUBJECT, "Share");  
                File file = new File(CaptureService.imageFile); 
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)); 
                
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   
                startActivity(Intent.createChooser(intent, getTitle()));  
//                mSsoHandler = new SsoHandler(CapturedImageActivity.this, mWeiboAuth);
//                mSsoHandler.authorize(new AuthListener());	
			}
		});
    }
    
    public static void setBitmap(Bitmap bitmap)
    {
    	mBitmap = bitmap;
    }
    
    
    /**
     * ΢����֤��Ȩ�ص��ࡣ
     * 1. SSO ��Ȩʱ����Ҫ�� {@link #onActivityResult} �е��� {@link SsoHandler#authorizeCallBack} ��
     *    �ûص��Żᱻִ�С�
     * 2. �� SSO ��Ȩʱ������Ȩ�����󣬸ûص��ͻᱻִ�С�
     * ����Ȩ�ɹ����뱣��� access_token��expires_in��uid ����Ϣ�� SharedPreferences �С�
     */
    class AuthListener implements WeiboAuthListener {
    	
    	
        
        @Override
        public void onComplete(Bundle values) {
        	Log.e("onComplete","onComplete");
            // �� Bundle �н��� Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                // ��ʾ Token
                //updateTokenView(false);
                
                // ���� Token �� SharedPreferences
               // AccessTokenKeeper.writeAccessToken(CapturedImageActivity.this, mAccessToken);
                
               
//                WeiboParameters params = new WeiboParameters();
//                params.add("access_token", mAccessToken);
//                params.add("status", "");
//                params.add("pic", mBitmap);
//                AsyncWeiboRunner.requestAsync("https://upload.api.weibo.com/2/statuses/upload.json", params, "POST", requestListener);
               
                WeiboParameters params = new WeiboParameters();
                params.add("access_token", mAccessToken);
                params.add("status", "i love you");
                AsyncWeiboRunner.requestAsync("https://api.weibo.com/2/statuses/update.json", params, "POST", requestListener);
                
                
                
//                Toast.makeText(CapturedImageActivity.this, 
//                        R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
            } else {
                // ���¼�������������յ� Code��
                // 1. ����δ��ƽ̨��ע���Ӧ�ó���İ�����ǩ��ʱ��
                // 2. ����ע���Ӧ�ó��������ǩ������ȷʱ��
                // 3. ������ƽ̨��ע��İ�����ǩ��������ǰ���Ե�Ӧ�õİ�����ǩ����ƥ��ʱ��
//                String code = values.getString("code");
//                //String message = getString(R.string.weibosdk_demo_toast_auth_failed);
//                if (!TextUtils.isEmpty(code)) {
//                    message = message + "\nObtained the code: " + code;
//                }
//                Toast.makeText(CapturedImageActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
//            Toast.makeText(CapturedImageActivity.this, 
//                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(CapturedImageActivity.this, 
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    class MyRequestListener implements RequestListener{

		@Override
		public void onComplete(String arg0) {
			// TODO �Զ����ɵķ������
			Log.i("weibo","onComplete");
		}

		@Override
		public void onWeiboException(WeiboException arg0) {
			// TODO �Զ����ɵķ������
			Log.i("weibo","onWeiboException:"+arg0.getLocalizedMessage());
		}
    	
    }
    


}
