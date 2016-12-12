# Cordova plugin for AimBrain SDK

# [Install](#install)

```
cordova plugin add https://bitbucket.org/n_shah/aimbraincordovaplugin
```

# [Functions](#functions)

As of now, the plugin supports following set of functions for both Android and iOS. We will keep adding more functions as they make sense.

## init

User can pass the `Key` and `Secret`:
```javascript
var KEY = "application key";
var SECRET = "application secret";
AimBrain.init(KEY, SECRET);
```

User can skip the `Key` and `Secret`, if these values are already set in preference:
```javascript
AimBrain.init();
```

User can skip the `Key` and `Secret`, if these values are set in preference and catch error if any (like Keys are missing):
```javascript
AimBrain.init(function(){  
}, function(err){
});
```

## createSession

```javascript
var userId = "...";
AimBrain.createSession(userId, function(session){
  console.log('Session:', session);
}, function(err){
  console.log('Error:', err);
});
```

Check [Structures]("#status-structures") for session status.

## setSession

```javascript
var sessionId = "...";
AimBrain.setSession(sessionId, function(){
  console.log('Success');
}, function(err){
  console.log('Error:', err);
});
```

## getSession

```javascript
AimBrain.getSession(function(session){
  console.log('Session:', session);
}, function(err){
  console.log('Error:', err);
});
```
Check [Structures]("#status-structures") for session status.

## startCollectingData

```javascript
AimBrain.startCollectingData(function(){
  console.log('Success');
}, function(err){
  console.log('Error:', err);
});
```

## stopCollectingData

```javascript
AimBrain.stopCollectingData(function(){
  console.log('Success');
}, function(err){
  console.log('Error:', err);
});
```

## submitCollectedData

```javascript
AimBrain.submitCollectedData(function(score){
  console.log('Score', score);
}, function(err){
  console.log('Error:', err);
});
```
Check [Structures]("#status-structures") for score status.

## scheduleDataSubmission

```javascript
var delay = 1000;
var period = 5000;
AimBrain.submitCollectedData(delay, period, function(){
  console.log('Success');
}, function(err){
  console.log('Error:', err);
});
```

## getCurrentScore

```javascript
AimBrain.getCurrentScore(function(score){
  console.log('Score', score);
}, function(err){
  console.log('Error:', err);
});
```
Check [Structures]("#status-structures") for score status.

## captureImageToEnroll
```javascript
AimBrain.captureImageToEnroll(function(data){
    console.log('Enrollment successful', data);    
}, function(err){
    console.log('Enrollment failed', err);
});
```

## captureVideoToEnroll
```javascript
AimBrain.captureVideoToEnroll(function(data){
    console.log('Enrollment successful', data);    
}, function(err){
    console.log('Enrollment failed', err);
});

```

## captureImageToAuthenticate
```javascript
AimBrain.captureImageToAuthenticate(function(data){
    console.log('Authentication successful',data);    
}, function(err){
    console.log('Enrollment failed',err);
});
```

## captureVideoToAuthenticate
```javascript
AimBrain.captureVideoToAuthenticate(function(data){
    console.log('Authentication successful',data);    
}, function(err){
    console.log('Enrollment failed',err);
});
```

## clearImage
Not Applicable at the moment

## clearVideo
Not Applicable at the moment

# [Structures]("#status-structures")

There are structures available in AimBrain JS module to compare the numeric status/errorCode values coming from AimBrain backend.

## SessionStatus
```javascript
AimBrain.SessionStatus.NOT_ENROLLED
AimBrain.SessionStatus.ENROLLED
AimBrain.SessionStatus.BUILDING
```

## ScoreStatus
```javascript
AimBrain.ScoreStatus.AUTHENTICATING
AimBrain.ScoreStatus.ENROLLING
```

## ErrorCode
```javascript
AimBrain.ErrorCode.UNKNOWN_ERROR
AimBrain.ErrorCode.INVALID_ARGUMENT_ERROR
AimBrain.ErrorCode.TIMEOUT_ERROR
AimBrain.ErrorCode.PENDING_OPERATION_ERROR
AimBrain.ErrorCode.IO_ERROR
AimBrain.ErrorCode.NOT_SUPPORTED_ERROR
AimBrain.ErrorCode.OPERATION_CANCELLED_ERROR
AimBrain.ErrorCode.PERMISSION_DENIED_ERROR
```

# [Configuration through Preferences](#configuration-through-preferences)

The plugin allows the developer to set some of the parameters via preferences.

* `AIMBRAIN_API_KEY` and `AIMBRAIN_SECRET_KEY` if set in preference, then the developer can omit while calling `init()` from JavaScript.
* `AIMBRAIN_FACIAL_ENABLED` is set to enable/disable Facial Module
* `AIMBRAIN_BEHAVIOURAL_ENABLED` is set to enable/disable Behavioural Module
* ~~AIMBRAIN_COLLECT_AT_START~~ is set to start collecting Behavioural data on start. Works only if `AIMBRAIN_API_KEY` and `AIMBRAIN_SECRET_KEY` is set and `AIMBRAIN_BEHAVIOURAL_ENABLED` is enabled. [DEPRECATED]
* ~~AIMBRAIN_AUTO_SUBMIT_ON~~ is set to auto submit the collected Behavioural data. Works only if ~~AIMBRAIN_COLLECT_AT_START~~ is enabled. [DEPRECATED]
* ~~AIMBRAIN_AUTO_SUBMIT_DELAY~~ and ~~AIMBRAIN_AUTO_SUBMIT_PERIOD~~ is set to milliseconds for the auto submit schedule. Works only if ~~AIMBRAIN_AUTO_SUBMIT_ON~~ is enabled. [DEPRECATED]
* ~~AIMBRAIN_AUTO_SUBMIT_CALLBACK~~ is to pass the Behavioural data auto submit update back to JavaScript. Works only if ~~AIMBRAIN_AUTO_SUBMIT_ON~~ is enabled. [DEPRECATED]
* `AIMBRAIN_ENROLL_UPPER_TEXT` and `AIMBRAIN_ENROLL_LOWER_TEXT` is use to set the text displayed during the Facial enrollment on Camera screen
* `AIMBRAIN_AUTH_UPPER_TEXT` and `AIMBRAIN_AUTH_LOWER_TEXT` is use to set the text displayed during the Facial authentication on Camera screen

**Note: [DEPRECATED] preferences were planned initially, but due to none compatible logic, removed later.**

## Android

After installing plugin go to `/res/values/strings.xml` and look for the values starting with `AIMBRAIN_`

```xml
<string name="AIMBRAIN_API_KEY"> </string>
<string name="AIMBRAIN_SECRET_KEY"> </string>
<string name="AIMBRAIN_FACIAL_ENABLED">true</string>
<string name="AIMBRAIN_BEHAVIOURAL_ENABLED">true</string>
<string name="AIMBRAIN_COLLECT_AT_START">false</string>
<string name="AIMBRAIN_AUTO_SUBMIT_ON">false</string>
<string name="AIMBRAIN_AUTO_SUBMIT_DELAY">0</string>
<string name="AIMBRAIN_AUTO_SUBMIT_PERIOD">0</string>
<string name="AIMBRAIN_AUTO_SUBMIT_CALLBACK">0</string>
<string name="AIMBRAIN_AUTH_UPPER_TEXT">To authenticate, please face the camera and press the camera button below</string>
<string name="AIMBRAIN_AUTH_LOWER_TEXT">Position your face fully within the outline</string>
<string name="AIMBRAIN_RECORDING_HINT"></string>
<string name="AIMBRAIN_ENROLL_UPPER_TEXT">To enroll, please face the camera and press the camera button below</string>
<string name="AIMBRAIN_ENROLL_LOWER_TEXT">Position your face fully within the outline</string>
```

## iOS

```
```

# [Caveats](#caveats)

There are few things the developer will have to do manually to use the plugin for now. We will keep clearing this list once we fix the issues.

* In Android, we need to make sure Application class is either set to `com.aimbrain.sdk.AMBNApplication.AMBNApplication` or derived from it. The plugin is not setting this automatically right now, so the app developer has to do this update manually in Manifest file.
* In Android, Gradle library version is expected to `gradle-2.14.1` (Yet to check in detail)
* In Android, the SDK is internally using the `AppCompat` library, currently the plugin is tested with `com.android.support:appcompat-v7:23.0.0`
* In Android, custom preferences defined in the plugin moves into the `/res/values/string.xml` after plugin install

# [Simple Demo (For Current Development Purpose Only, this section will be removed at the end of the plugin development)](#demo)

* Create a Cordova app
* Add Android platform
* Install the AimBrain Cordova plugin from above instruction
* Make sure you follow the Caveats fixes for Android project
* Copy the below html code
* Copy the below app.js code and run the app

## index.html

```html
<html>
    <head>
        <meta http-equiv="Content-Security-Policy" content="default-src 'self' data: gap: https://ssl.gstatic.com 'unsafe-eval'; style-src 'self' 'unsafe-inline'; media-src *">
        <meta name="format-detection" content="telephone=no">
        <meta name="msapplication-tap-highlight" content="no">
        <meta name="viewport" content="user-scalable=no, initial-scale=1, maximum-scale=1, minimum-scale=1, width=device-width">
        <link rel="stylesheet" type="text/css" href="css/index.css">
        <title>Hello World</title>
    </head>
    <body>
        <div>
            <h1>AimBrain Cordova App</h1>
        </div>
        <div>
            <label>
                <span>Select Method:</span>
                <select id="media">
                    <option>Photo</option>
                    <option>Video</option>
                </select>
            </label>
          <button id="register">Register</button>
          <button id="login">Login</button>
        </div>
        <div id="messages">
        </div>
        <script type="text/javascript" src="cordova.js"></script>
        <script type="text/javascript" src="js/index.js"></script>
    </body>
</html>
```

## app.js

```javascript
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {    
    initialize: function() {
        this.bindEvents();
        this.messages = document.getElementById('messages');
        this.media = document.getElementById('media');
        this.hasSession = false;
        this.registerDone = false;
        this.loginDone = false;
    },

    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onDeviceReady: function() {
        // Call AimBrain Configure Here
        if(window.AimBrain){
          var KEY = "...";
          var SECRET = "...";
          AimBrain.init(KEY, SECRET);
          console.log('AimBrain configured');
        }

        document.getElementById('register').onclick = this.register.bind(this);
        document.getElementById('login').onclick = this.login.bind(this);
    },

    createSession: function(success, error){
        this.userId = new Date().getTime();
        this.addMessage('Registration in progress');
        this.addMessage('Creating Session');
        AimBrain.createSession(this.userId, function(session){
            this.addMessage('Session created: ' + JSON.stringify(session));
            this.hasSession = true;
            success();
        }.bind(this), error);
    },

    register: function(){
        if(!this.hasSession){
          this.createSession(this._register.bind(this), function(err){
            this.addMessage("Session Error " + err);
          }.bind(this));
        }else{
          this._register();
        }
    },

    _register: function(){
        var media = this.media.value;
        this.addMessage('Starting enrollment with ' + media);
        if(media == 'Video'){
            AimBrain.captureVideoToEnroll(function(data){
                this.addMessage('Enrollment successful ' + JSON.stringify(data));
                this.registerDone = true;
            }.bind(this), function(err){
                this.addMessage('Enrollment failed ' + err);
            }.bind(this));
        }else{
            AimBrain.captureImageToEnroll(function(data){
                this.addMessage('Enrollment successful ' + JSON.stringify(data));
                this.registerDone = true;
            }.bind(this), function(err){
                this.addMessage('Enrollment failed ' + err);
            }.bind(this));
        }
    },

    addMessage: function(message){
        this.messages.innerHTML = this.messages.innerHTML + "<br/>" + message;
    },

    login: function(){
        if(!this.registerDone){
            alert('Please complete registration first');
            return;
        }

        var media = this.media.value;
        this.addMessage('Starting authenticate with ' + media);
        if(media == 'Video'){
            AimBrain.captureVideoToAuthenticate(function(data){
                this.addMessage('Authentication successful ' + JSON.stringify(data));
                this.loginDone = true;
            }.bind(this), function(err){
                this.addMessage('Enrollment failed ' + err);
            }.bind(this));
        }else{
            AimBrain.captureImageToAuthenticate(function(data){
                this.addMessage('Authentication successful ' + JSON.stringify(data));
                this.loginDone = true;
            }.bind(this), function(err){
                this.addMessage('Enrollment failed ' + err);
            }.bind(this));
        }
    }
};

app.initialize();
```
