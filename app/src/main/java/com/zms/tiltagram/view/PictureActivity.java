package com.zms.tiltagram.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultAdapter;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;
import com.zms.tiltagram.R;

public class PictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);

        if (ActivityCompat.checkSelfPermission(PictureActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return TODO;
        }else{
            //you can configure the fragment by the configuration builder
            final CameraFragment cameraFragment = CameraFragment.newInstance(new Configuration.Builder().build());

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    cameraFragment.toggleFlashMode();
                    cameraFragment.switchCameraTypeFrontBack();
                    cameraFragment.switchActionPhotoVideo();
                    //cameraFragment.openSettingDialog();
                    cameraFragment.takePhotoOrCaptureVideo(new CameraFragmentResultListener() {
                        @Override
                        public void onVideoRecorded(String filePath) {

                        }

                        @Override
                        public void onPhotoTaken(byte[] bytes, String filePath) {

                        }
                    }, "teste", "teste");
                }
            }, 2000);


            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout1, cameraFragment)
                    .commit();
        }
    }
}
