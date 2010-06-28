package com.borismus.mindstorms;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * A view that abstracts the Android camera.
 * @author boris
 *
 */
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;

    CameraPreview(Context context) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Callback for when a surface is created. Sets up a camera object
     * and a preview (necessary for takePicture to work)
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open();
        try {
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            // TODO: add more exception handling logic here
        }
    }

    /**
     * Callback for when surface is destroyed just tears down the camera and 
     * releases it for other processes to use.
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    /**
     * Resize surface callback. Hardcoded preview size for Nexus One.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();

        parameters.setPreviewSize(800, 480);
        parameters.setPictureFormat(ImageFormat.JPEG);
//        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
        parameters.setRotation(90);

        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }
    
    /**
     * For actually taking a picture.
     * @param callback the callback that gets called when the picture is taken successfully
     */
    public void capture(PictureCallback callback) {
    	if (mCamera == null) {
    		return;
    	}
    	mCamera.takePicture(null, null, callback);
    }
}
