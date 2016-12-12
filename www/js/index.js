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
    // Application Constructor
    initialize: function() {
        this.bindEvents();
        this.messages = document.getElementById('messages');
        this.media = document.getElementById('media');
        this.hasSession = false;
        this.registerDone = false;
        this.loginDone = false;
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        // Call AimBrain Configure Here
        if(window.AimBrain){
          var KEY = "de545b49-62bf-41c9-8e69-5ff1a8860d18";
          var SECRET = "2bb3c63945d742c688c6c29e5428f80edb6b38d19cae4b7986417a8c3cfaec74";
          AimBrain.init(KEY, SECRET);
          console.log('AimBrain configured');
        }

        document.getElementById('register').onclick = this.register.bind(this);
        document.getElementById('login').onclick = this.login.bind(this);
    },

    createSession: function(success, error){
        this.userId = "USER_" + (new Date().getTime());
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
            this.addMessage("Session Error " + JSON.stringify(err));
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
                this.addMessage('Enrollment failed ' + JSON.stringify(err));
            }.bind(this));
        }else{
            AimBrain.captureImageToEnroll(function(data){
                this.addMessage('Enrollment successful ' + JSON.stringify(data));
                this.registerDone = true;
            }.bind(this), function(err){
                this.addMessage('Enrollment failed ' + JSON.stringify(err));
            }.bind(this));
        }
    },

    addMessage: function(message){
        this.messages.innerHTML = this.messages.innerHTML + "<li>" + message + "</li>";
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
                this.addMessage('Enrollment successful ' + JSON.stringify(data));
                this.loginDone = true;
            }.bind(this), function(err){
                this.addMessage('Enrollment failed ' + JSON.stringify(err));
            }.bind(this));
        }else{
            AimBrain.captureImageToAuthenticate(function(data){
                this.addMessage('Enrollment successful ' + JSON.stringify(data));
                this.loginDone = true;
            }.bind(this), function(err){
                this.addMessage('Enrollment failed ' + JSON.stringify(err));
            }.bind(this));
        }
    }
};

app.initialize();
