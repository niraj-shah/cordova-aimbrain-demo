package com.aimbrain.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.aimbrain.sdk.Manager;
import com.aimbrain.sdk.faceCapture.FaceCaptureActivity;
import com.aimbrain.sdk.faceCapture.PhotoFaceCaptureActivity;
import com.aimbrain.sdk.faceCapture.VideoFaceCaptureActivity;
import com.aimbrain.sdk.models.FaceAuthenticateModel;
import com.aimbrain.sdk.models.FaceEnrollModel;
import com.aimbrain.sdk.models.ScoreModel;
import com.aimbrain.sdk.models.SessionModel;
import com.aimbrain.sdk.server.AMBNResponseErrorListener;
import com.aimbrain.sdk.server.FaceCapturesAuthenticateCallback;
import com.aimbrain.sdk.server.FaceCapturesEnrollCallback;
import com.aimbrain.sdk.server.ScoreCallback;
import com.aimbrain.sdk.server.SessionCallback;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

public class AimBrainPlugin extends CordovaPlugin {

    private int triesAmount = 0;
    private static final String photoAuthUpperText = "To authenticate please face the camera directly, press 'camera' button and blink";
    private static final String photoLowerText = "Position your face fully within the outline.";

    private static final String recordingHintAuthentication = "Please BLINK now...";
    private static final String[] enrollStepsTexts = {
            "To enroll please face the camera directly and press 'camera' button",
            "Face the camera slightly from the top and press 'camera' button",
            "Face the camera slightly from the bottom and press 'camera' button",
            "Face the camera slightly from the left and press 'camera' button",
            "Face the camera slightly from the right and press 'camera' button"
    };

    private static final int ENROLLMENT_REQUEST_CODE = 1542;
    private static final int AUTHENTICATION_REQUEST_CODE = 1543;

    private static final String LOG_TAG = "AimBrain Plugin";

    private CallbackContext callbackContext;        // The callback context from which we were invoked.
    private JSONArray executeArgs;

    private static final String ACTION_INIT = "init";
    private static final String ACTION_CREATE_SESSION = "createSession";
    private static final String ACTION_START_COLLECTING_DATA = "startCollectingData";
    private static final String ACTION_STOP_COLLECTING_DATA = "stopCollectingData";
    private static final String ACTION_GET_SESSION = "getSession";
    private static final String ACTION_SET_SESSION = "setSession";
    private static final String ACTION_GET_CURRENT_SCORE = "getCurrentScore";
    private static final String ACTION_SUBMIT_COLLECTION_DATA = "submitCollectedData";
    private static final String ACTION_SCHEDULE_DATA_SUBMISSION = "scheduleDataSubmission";

    private static final String ACTION_CAPTURE_IMAGE_TO_ENROLL = "captureImageToEnroll";
    private static final String ACTION_CAPTURE_VIDEO_TO_ENROLL = "captureVideoToEnroll";
    private static final String ACTION_CAPTURE_IMAGE_TO_AUTH = "captureImageToAuthenticate";
    private static final String ACTION_CAPTURE_VIDEO_TO_AUTH = "captureVideoToAuthenticate";

    public static final int UNKNOWN_ERROR = 0;
    public static final int INVALID_ARGUMENT_ERROR = 1;
    public static final int TIMEOUT_ERROR = 2;
    public static final int PENDING_OPERATION_ERROR = 3;
    public static final int IO_ERROR = 4;
    public static final int NOT_SUPPORTED_ERROR = 5;
    public static final int OPERATION_CANCELLED_ERROR = 6;
    public static final int NETWORK_ERROR = 7;
    public static final int PERMISSION_DENIED_ERROR = 20;

    private boolean ENROLL_IN_PROGRESS = false;
    private boolean AUTH_IN_PROGRESS = false;
    private boolean AUTH_ENROLL_WITH_PHOTOS = false;

    private String AIMBRAIN_API_KEY;
    private String AIMBRAIN_SECRET_KEY;
    private Boolean AIMBRAIN_FACIAL_ENABLED;
    private Boolean AIMBRAIN_BEHAVIOURAL_ENABLED;
    private Boolean AIMBRAIN_COLLECT_AT_START;
    private Boolean AIMBRAIN_AUTO_SUBMIT_ON;
    private Long AIMBRAIN_AUTO_SUBMIT_DELAY;
    private Long AIMBRAIN_AUTO_SUBMIT_PERIOD;
    private String AIMBRAIN_AUTO_SUBMIT_CALLBACK;

    private String AIMBRAIN_AUTH_UPPER_TEXT;
    private String AIMBRAIN_AUTH_LOWER_TEXT;
    private String AIMBRAIN_RECORDING_HINT;
    private String AIMBRAIN_ENROLL_UPPER_TEXT;
    private String AIMBRAIN_ENROLL_LOWER_TEXT;

    public AimBrainPlugin(){
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        AIMBRAIN_API_KEY = getStringResource("AIMBRAIN_API_KEY");
        AIMBRAIN_SECRET_KEY = getStringResource("AIMBRAIN_SECRET_KEY");
        AIMBRAIN_FACIAL_ENABLED = getBooleanResource("AIMBRAIN_FACIAL_ENABLED");
        AIMBRAIN_BEHAVIOURAL_ENABLED = getBooleanResource("AIMBRAIN_BEHAVIOURAL_ENABLED");
        AIMBRAIN_COLLECT_AT_START = getBooleanResource("AIMBRAIN_COLLECT_AT_START");
        AIMBRAIN_AUTO_SUBMIT_ON = getBooleanResource("AIMBRAIN_AUTO_SUBMIT_ON");
        AIMBRAIN_AUTO_SUBMIT_DELAY = getLongResource("AIMBRAIN_AUTO_SUBMIT_DELAY");
        AIMBRAIN_AUTO_SUBMIT_PERIOD = getLongResource("AIMBRAIN_AUTO_SUBMIT_PERIOD");
        AIMBRAIN_AUTO_SUBMIT_CALLBACK = getStringResource("AIMBRAIN_AUTO_SUBMIT_CALLBACK");

        AIMBRAIN_AUTH_UPPER_TEXT = getStringResource("AIMBRAIN_AUTH_UPPER_TEXT");
        AIMBRAIN_AUTH_LOWER_TEXT = getStringResource("AIMBRAIN_AUTH_LOWER_TEXT");
        AIMBRAIN_RECORDING_HINT = getStringResource("AIMBRAIN_RECORDING_HINT");
        AIMBRAIN_ENROLL_UPPER_TEXT = getStringResource("AIMBRAIN_ENROLL_UPPER_TEXT");
        AIMBRAIN_ENROLL_LOWER_TEXT = getStringResource("AIMBRAIN_ENROLL_LOWER_TEXT");

//      Discussed with Niraj on 8th Dec
//        if(hasKeys()){
//            if(isBehaviouralModuleEnabled()) {
                // This is difficult to implement as it would need to init the plugin and create a session too,
                // creating session requires userId, which the plugin wouldn't have
//                if (isCollectAtStart()) {
//                }

                // This is also not possible due to above condition
//                if(isAutoSubmitOn() && isAutoSubmitDelayValid() && isAutoSubmitPeriodValid()){
//                    Manager.getInstance().scheduleDataSubmission(AIMBRAIN_AUTO_SUBMIT_DELAY.longValue(), AIMBRAIN_AUTO_SUBMIT_PERIOD.longValue(), new ScoreCallback() {
//                        @Override
//                        public void success(ScoreModel scoreModel) {
//                            if(hasAutoSubmitCallback()){
//
//                            }
//                        }
//                    });
//                }
//            }
//        }
    }

    /*
     *   Check whether AimBrain Keys are available from Plugin Preference
     */
    private boolean hasKeys(){
        if(AIMBRAIN_API_KEY != null && AIMBRAIN_SECRET_KEY != null &&
                AIMBRAIN_API_KEY.trim() != "" && AIMBRAIN_SECRET_KEY.trim() != ""){
            return true;
        }

        return false;
    }

    /*
     *   Check if Behavioural Module is enabled or not
     */
    private boolean isBehaviouralModuleEnabled(){
        if(AIMBRAIN_BEHAVIOURAL_ENABLED == null) return false;

        return AIMBRAIN_BEHAVIOURAL_ENABLED.booleanValue();
    }

    /*
     *   Check if Facial Module is enabled or not
     */
    private boolean isFacialModuleEnabled(){
        if(AIMBRAIN_FACIAL_ENABLED == null) return false;

        return AIMBRAIN_FACIAL_ENABLED.booleanValue();
    }

    /*
     *   Check whether to start collecting data on application start
     */
    private boolean isCollectAtStart(){
        if(AIMBRAIN_COLLECT_AT_START == null) return false;

        return AIMBRAIN_COLLECT_AT_START.booleanValue();
    }

    /*
     *   Check whether to auto submit collection data
     */
    private boolean isAutoSubmitOn(){
        if(AIMBRAIN_AUTO_SUBMIT_ON == null) return false;

        return AIMBRAIN_AUTO_SUBMIT_ON.booleanValue();
    }

    /*
     *   Check whether the autosubmit collection callback in JavaScript is provided or not
     */
    private boolean hasAutoSubmitCallback(){
        if(AIMBRAIN_AUTO_SUBMIT_CALLBACK == null) return false;

        return AIMBRAIN_AUTO_SUBMIT_CALLBACK.trim() != "";
    }

    /*
     *   Check whether Auto Submit Delay in Milliseconds is valid or not
     */
    private boolean isAutoSubmitDelayValid(){
        return AIMBRAIN_AUTO_SUBMIT_DELAY != null;
    }

    /*
     *   Check whether Auto Submit Period in Milliseconds is valid or not
     */
    private boolean isAutoSubmitPeriodValid(){
        return AIMBRAIN_AUTO_SUBMIT_PERIOD != null;
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArray of arguments for the plugin.
     * @param callbackContext   The callback context used when calling back into JavaScript.
     * @return                  True if the action was valid, false otherwise.
     */
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.executeArgs = args;

        Log.i(LOG_TAG, "Action received: " + action + " with arguments " + args.toString());

        final Context context = this.cordova.getActivity().getApplicationContext();

        if(ENROLL_IN_PROGRESS){
            callbackContext.error(getErrorToJSON(PENDING_OPERATION_ERROR, "Enrollment in progress", null, null));
            return false;
        }else if(AUTH_IN_PROGRESS){
            callbackContext.error(getErrorToJSON(PENDING_OPERATION_ERROR, "Authentication in progress", null, null));
            return false;
        }

        if (action.equals(ACTION_INIT)) {
            if(args.length() <= 0 && !hasKeys()){
                callbackContext.error(getErrorToJSON(INVALID_ARGUMENT_ERROR, "Please provide Access Key/Secret.", null, null));
                return false;
            }

            initAimBrain(args.getString(0), args.getString(1));
        } else if (action.equals(ACTION_CREATE_SESSION)) {
            createAimBrainSession(args.getString(0));
        } else if(action.equals(ACTION_START_COLLECTING_DATA)){
            if(!isBehaviouralModuleEnabled()){
                callbackContext.error(getErrorToJSON(NOT_SUPPORTED_ERROR, "Behaviour module is not enabled", null, null));
                return false;
            }

          cordova.getThreadPool().execute(new Runnable() {
              @Override
              public void run() {
                  try{
                      Manager.getInstance().startCollectingData(AimBrainPlugin.this.cordova.getActivity().getWindow());
                      callbackContext.success();
                  }catch (Exception e){
                      Log.e(LOG_TAG, "Error", e);
                      callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                  }
              }
          });
        } else if(action.equals(ACTION_STOP_COLLECTING_DATA)){
            if(!isBehaviouralModuleEnabled()){
                callbackContext.error(getErrorToJSON(NOT_SUPPORTED_ERROR, "Behaviour module is not enabled", null, null));
                return false;
            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        Manager.getInstance().stopCollectingData();
                        callbackContext.success();
                    }catch (Exception e){
                        Log.e(LOG_TAG, "Error", e);
                        callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                    }
                }
            });
        } else if(action.equals(ACTION_GET_SESSION)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        SessionModel session = Manager.getInstance().getSession();

                        if(session != null) {
                            JSONObject item = new JSONObject();
                            item.put("sessionId", session.getSessionId());
                            item.put("faceStatus", session.getFaceStatus());
                            item.put("behaviourStatus", session.getBehaviourStatus());

                            callbackContext.success(item);
                        }else{
                            callbackContext.error(getErrorToJSON(NOT_SUPPORTED_ERROR, "No Session Object Found", null, null));
                        }
                    }catch (Exception e){
                        Log.e(LOG_TAG, "Error", e);
                        callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                    }
                }
            });
        }else if(action.equals(ACTION_SET_SESSION)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        Manager.getInstance().configure(args.getString(0));
                        callbackContext.success();
                    }catch (Exception e){
                        Log.e(LOG_TAG, "Error", e);
                        callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                    }
                }
            });
        }else if(action.equals(ACTION_GET_CURRENT_SCORE)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        Manager.getInstance().getCurrentScore(new ScoreCallback() {
                            @Override
                            public void success(ScoreModel score) {
                                try {
                                    if (score != null) {
                                        JSONObject item = new JSONObject();
                                        item.put("score", score.getScore());
                                        item.put("session", score.getSession());
                                        item.put("status", score.getStatus());

                                        callbackContext.success(item);
                                    } else {
                                        callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, "No Score Object Found", null, null));
                                    }
                                }catch(Exception e){
                                    Log.e(LOG_TAG, "Error", e);
                                    callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                                }
                            }
                        });
                    }catch (Exception e){
                        Log.e(LOG_TAG, "Error", e);
                        callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                    }
                }
            });
        }else if(action.equals(ACTION_SUBMIT_COLLECTION_DATA)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        Manager.getInstance().submitCollectedData(new ScoreCallback() {
                            @Override
                            public void success(ScoreModel score) {
                                try {
                                    if (score != null) {
                                        JSONObject item = new JSONObject();
                                        item.put("score", score.getScore());
                                        item.put("session", score.getSession());
                                        item.put("status", score.getStatus());

                                        callbackContext.success(item);
                                    } else {
                                        callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, "No Score Object Found", null, null));
                                    }
                                }catch(Exception e){
                                    Log.e(LOG_TAG, "Error", e);
                                    callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                                }
                            }
                        });
                    }catch (Exception e){
                        Log.e(LOG_TAG, "Error", e);
                        callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                    }
                }
            });
        }else if(action.equals(ACTION_SCHEDULE_DATA_SUBMISSION)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        Manager.getInstance().scheduleDataSubmission(args.getLong(0), args.getLong(1));
                        callbackContext.success();
                    }catch (Exception e){
                        Log.e(LOG_TAG, "Error", e);
                        callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                    }
                }
            });
        }else if(action.equals(ACTION_CAPTURE_IMAGE_TO_ENROLL)){
            if(!isFacialModuleEnabled()){
                callbackContext.error(getErrorToJSON(NOT_SUPPORTED_ERROR, "Facial module is not enabled", null, null));
                return false;
            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Log.e(LOG_TAG, "Enrolling with photo");
                    enrollWithPhotos();
                }
            });
        }else if(action.equals(ACTION_CAPTURE_VIDEO_TO_ENROLL)){
            if(!isFacialModuleEnabled()){
                callbackContext.error(getErrorToJSON(NOT_SUPPORTED_ERROR, "Facial module is not enabled", null, null));
                return false;
            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    Log.e(LOG_TAG, "Enrolling with video");
                    enrollWithVideos();
                }
            });
        }else if(action.equals(ACTION_CAPTURE_IMAGE_TO_AUTH)){
            if(!isFacialModuleEnabled()){
                callbackContext.error(getErrorToJSON(NOT_SUPPORTED_ERROR, "Facial module is not enabled", null, null));
                return false;
            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    authWithPhotos();
                }
            });
        }else if(action.equals(ACTION_CAPTURE_VIDEO_TO_AUTH)){
            if(!isFacialModuleEnabled()){
                callbackContext.error(getErrorToJSON(NOT_SUPPORTED_ERROR, "Facial module is not enabled", null, null));
                return false;
            }

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    authWithVideos();
                }
            });
        }

        return true;
    }

    private void initAimBrain(final String key, final String secret){
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Manager.getInstance().configure(key, secret);
                    if(callbackContext != null) callbackContext.success();
                }catch(Exception e) {
                    Log.e(LOG_TAG, "Error", e);
                    if(callbackContext != null) callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                }
            }
        });
    }

    private void createAimBrainSession(final String userId){
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Manager.getInstance().createSession(userId, AimBrainPlugin.this.cordova.getActivity().getApplicationContext(), new SessionCallback() {
                        @Override
                        public void onSessionCreated(SessionModel session) {
                            Log.i(LOG_TAG, "session id: " + session.getSessionId());
                            Log.i(LOG_TAG, "session face status: " + session.getFaceStatus());
                            Log.i(LOG_TAG, "session behaviour status: " + session.getBehaviourStatus());

                            try {
                                JSONObject item = new JSONObject();
                                item.put("sessionId", session.getSessionId());
                                item.put("faceStatus", session.getFaceStatus());
                                item.put("behaviourStatus", session.getBehaviourStatus());

                                if(callbackContext != null) callbackContext.success(item);
                            }catch(Exception e){
                                Log.e(LOG_TAG, "Error", e);
                                if(callbackContext != null) callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                            }
                        }
                    }, new AMBNResponseErrorListener(){
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            super.onErrorResponse(error);
                            if(callbackContext != null) callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, error, null));
                        }
                    });
                }catch(Exception e) {
                    Log.e(LOG_TAG, "Error", e);
                    if(callbackContext != null) callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                }
            }
        });
    }
    /*
     *  Initiate Facial Enrollment process with Photo
     */
    private void authWithPhotos(){
        AUTH_IN_PROGRESS = true;
        AUTH_ENROLL_WITH_PHOTOS = true;
        openFaceImageCaptureActivity(AUTHENTICATION_REQUEST_CODE, enrollStepsTexts[triesAmount], photoLowerText);
    }

    /*
     *  Initiate Facial Enrollment process with Video
     */
    private void authWithVideos(){
        AUTH_IN_PROGRESS = true;
        AUTH_ENROLL_WITH_PHOTOS = false;
        openFaceVideoCaptureActivity(AUTHENTICATION_REQUEST_CODE, enrollStepsTexts[triesAmount], photoLowerText);
    }

    /*
     *  Initiate Facial Authentication process with Photo
     */
    private void enrollWithPhotos() {
        ENROLL_IN_PROGRESS = true;
        AUTH_ENROLL_WITH_PHOTOS = true;
        openFaceImageCaptureActivity(ENROLLMENT_REQUEST_CODE, enrollStepsTexts[triesAmount], photoLowerText);
    }

    /*
     *  Initiate Facial Authentication process with Video
     */
    private void enrollWithVideos() {
        ENROLL_IN_PROGRESS = true;
        AUTH_ENROLL_WITH_PHOTOS = false;
        openFaceVideoCaptureActivity(ENROLLMENT_REQUEST_CODE, enrollStepsTexts[triesAmount], photoLowerText);
    }

    /*
     *  Open the AimBrain SDK Camera activity for Photo Capture
     */
    private void openFaceImageCaptureActivity(int requestCode, String upperText, String lowerText) {
        openFaceImageCaptureActivity(requestCode, upperText, lowerText, null);
    }

    /*
     *  Open the AimBrain SDK Camera activity for Video Capture
     */
    private void openFaceVideoCaptureActivity(int requestCode, String upperText, String lowerText) {
        openFaceVideoCaptureActivity(requestCode, upperText, lowerText, null);
    }

    /*
     *  Open the AimBrain SDK Camera activity for Photo Capture
     */
    private void openFaceImageCaptureActivity(int requestCode, String upperText, String lowerText, String recordingHint) {
        Intent intent = new Intent(this.cordova.getActivity().getApplicationContext(), PhotoFaceCaptureActivity.class);
        intent.putExtra(FaceCaptureActivity.EXTRA_UPPER_TEXT, upperText);
        intent.putExtra(FaceCaptureActivity.EXTRA_LOWER_TEXT, lowerText);
        if (recordingHint != null) {
            intent.putExtra(FaceCaptureActivity.RECORDING_HINT, recordingHint);
        }
        this.cordova.startActivityForResult(this, intent, requestCode);
    }

    /*
     *  Open the AimBrain SDK Camera activity for Video Capture
     */
    private void openFaceVideoCaptureActivity(int requestCode, String upperText, String lowerText, String recordingHint) {
        Intent intent = new Intent(this.cordova.getActivity().getApplicationContext(), VideoFaceCaptureActivity.class);
        intent.putExtra(FaceCaptureActivity.EXTRA_UPPER_TEXT, upperText);
        intent.putExtra(FaceCaptureActivity.EXTRA_LOWER_TEXT, lowerText);
        if (recordingHint != null) {
            intent.putExtra(FaceCaptureActivity.RECORDING_HINT, recordingHint);
        }
        this.cordova.startActivityForResult(this, intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case ENROLLMENT_REQUEST_CODE:
                if (resultCode != Activity.RESULT_OK) {
                    callbackContext.error("Something went wrong");
                    clearAuthEnrollFlags();
                    return;
                }

                try {
                    FaceCapturesEnrollCallback callback = new FaceCapturesEnrollCallback() {
                        @Override
                        public void success(FaceEnrollModel faceEnrollModel) {
                            try {
                                if (faceEnrollModel != null) {
                                    JSONObject item = new JSONObject();
                                    item.put("correctCaptureCount", faceEnrollModel.getCorrectCapturesCount());

                                    callbackContext.success(item);
                                } else {
                                    callbackContext.error(getErrorToJSON(NETWORK_ERROR, "Missing enrollment response", null, null));
                                }
                                clearAuthEnrollFlags();
                            }catch(Exception e){
                                Log.e(LOG_TAG, "Error", e);
                                callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                                clearAuthEnrollFlags();
                            }
                        }

                        @Override
                        public void failure(VolleyError volleyError) {
                            callbackContext.error(getErrorToJSON(NETWORK_ERROR, null, volleyError, null));
                            clearAuthEnrollFlags();
                        }
                    };

                    if(AUTH_ENROLL_WITH_PHOTOS) {
                        Manager.getInstance().sendProvidedFaceCapturesToEnroll(PhotoFaceCaptureActivity.images, callback);
                    }else{
                        Manager.getInstance().sendProvidedFaceCapturesToEnroll(VideoFaceCaptureActivity.video, callback);
                    }
                } catch (Exception e) {
                    callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                    clearAuthEnrollFlags();
                }
                break;
            case AUTHENTICATION_REQUEST_CODE:
                if (resultCode != Activity.RESULT_OK) {
                    callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, "Something went wrong", null, null));
                    clearAuthEnrollFlags();
                    return;
                }

                try {
                    FaceCapturesAuthenticateCallback callback = new FaceCapturesAuthenticateCallback() {
                        @Override
                        public void success(FaceAuthenticateModel faceAuthenticateModel) {
                            try {
                                if (faceAuthenticateModel != null) {
                                    JSONObject item = new JSONObject();
                                    item.put("score", faceAuthenticateModel.getScore());
                                    item.put("liveliness", faceAuthenticateModel.getLiveliness());

                                    callbackContext.success(item);
                                } else {
                                    callbackContext.error(getErrorToJSON(NETWORK_ERROR, "Missing authentication response", null, null));
                                }
                                clearAuthEnrollFlags();
                            }catch(Exception e){
                                Log.e(LOG_TAG, "Error", e);
                                callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                                clearAuthEnrollFlags();
                            }
                        }

                        @Override
                        public void failure(VolleyError volleyError) {
                            callbackContext.error(getErrorToJSON(NETWORK_ERROR, null, volleyError, null));
                            clearAuthEnrollFlags();
                        }
                    };

                    if(AUTH_ENROLL_WITH_PHOTOS) {
                        Manager.getInstance().sendProvidedFaceCapturesToAuthenticate(PhotoFaceCaptureActivity.images, callback);
                    }else{
                        Manager.getInstance().sendProvidedFaceCapturesToAuthenticate(VideoFaceCaptureActivity.video, callback);
                    }
                } catch (Exception e) {
                    callbackContext.error(getErrorToJSON(UNKNOWN_ERROR, null, e, null));
                    clearAuthEnrollFlags();
                }
                break;
        }
    }

    private void clearAuthEnrollFlags(){
        AUTH_IN_PROGRESS = false;
        ENROLL_IN_PROGRESS = false;
    }

    private JSONObject getErrorToJSON(int code, String message, Throwable error, NetworkError networkError) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject();
            jsonObject.put("code", code);
            if(message != null) {
                jsonObject.put("message", message);
            }
            if(error != null){
                jsonObject.put("message", error.getMessage());
            }
            if(networkError != null){
                jsonObject.put("messsage", networkError.getMessage());
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Returns the String resource from plugin preference
     * @param name
     * @return
     */
    private String getStringResource(String name) {
        return this.cordova.getActivity().getString(
                this.cordova.getActivity().getResources().getIdentifier(
                        name, "string", this.cordova.getActivity().getPackageName()));
    }

    /**
     * Returns the boolean resource from plugin preference
     * @param name
     * @return
     */
    private Boolean getBooleanResource(String name) {
        try {
            return Boolean.valueOf(getStringResource(name));
        }catch(Exception e){
            return null;
        }
    }

    /**
     * Returns the Long resource from plugin preference
     * @param name
     * @return
     */
    private Long getLongResource(String name) {
        try {
            return Long.valueOf(getStringResource(name));
        }catch(Exception e){
            return null;
        }
    }
}
