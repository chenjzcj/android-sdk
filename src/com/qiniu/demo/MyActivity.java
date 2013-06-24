package com.qiniu.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.qiniu.R;
import com.qiniu.auth.JSONObjectRet;
import com.qiniu.resumable.ResumableIO;
import com.qiniu.resumable.RputExtra;
import com.qiniu.resumable.RputNotify;
import org.json.JSONObject;

public class MyActivity extends Activity implements View.OnClickListener{

	public static final int PICK_PICTURE_RESUMABLE = 0;

	// @gist upload
	// 在七牛绑定的对应bucket的域名. 可以到这里绑定 https://dev.qiniutek.com/buckets
	public static String domain = "http://api-demo.qiniudn.com";
	public static String bucketName = "demo";
	// upToken 这里需要自行获取. SDK 将不实现获取过程.
	public static final String UP_TOKEN = "";
	// @endgist

	private Button btnResumableUpload;
	private ProgressBar progressBar;
    private TextView hint;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initWidget();
    }

	/**
	 * 初始化控件
	 */
	private void initWidget() {
        hint = (TextView) findViewById(R.id.textView1);

		btnResumableUpload = (Button) findViewById(R.id.button1);
		btnResumableUpload.setOnClickListener(this);

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setMax(100);
		progressBar.setProgress(0);
	}

	/**
	 * 断点续上传
	 * @param uri
	 */
	// @gist upload
	private void doResumableUpload(Uri uri) {
        RputExtra extra = getPutExtra();

        progressBar.setProgress(0);
		ResumableIO.putFile(this, UP_TOKEN, null, uri, extra, new JSONObjectRet() {
            @Override
            public void onSuccess(JSONObject resp) {
                String hash;
                try {
                    hash = resp.getString("hash");
                } catch (Exception ex) {
                    hint.setText(ex.getMessage());
                    return;
                }
                String redirect = domain + "/" + hash;
                hint.setText("上传成功! " + hash);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redirect));
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception ex) {
                hint.setText("错误: " + ex.getMessage());
            }
        });
	}

	private RputExtra getPutExtra() {
		RputExtra extra = new RputExtra(bucketName);
		extra.mimeType = "image/png";
		extra.notify = new RputNotify() {
            @Override
            public synchronized void onProcess(long uploaded, long total) {
                progressBar.setProgress((int) (uploaded * 100 / total));
            }
		};
		return extra;
	}
	// @endgist

	@Override
	public void onClick(View view) {
		if (view.equals(btnResumableUpload)) {
			Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, PICK_PICTURE_RESUMABLE);
			return;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;

		if (requestCode == PICK_PICTURE_RESUMABLE) {
			doResumableUpload(data.getData());
			return;
		}
	}
}
