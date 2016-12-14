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
        this.delay = document.getElementById('delay');
        this.period = document.getElementById('period');

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

        // set user_id on startup
        this.userId = "USER_" + device.uuid;

        this.addMessage("User ID: " + this.userId);

        this.createSession(function(session) {
          this.addMessage('Session created successfully: ' + JSON.stringify(session));
        }, function(err){
          this.addMessage("Session Error " + JSON.stringify(err));
        }.bind(this));

        document.getElementById('register').onclick = this.register.bind(this);
        document.getElementById('login').onclick = this.login.bind(this);
        document.getElementById('clear').onclick = this.clear.bind(this);
        document.getElementById('session').onclick = this.session.bind(this);
        document.getElementById('startCollectingData').onclick = this.startCollectingData.bind(this);
        document.getElementById('stopCollectingData').onclick = this.stopCollectingData.bind(this);
        document.getElementById('submitCollectedData').onclick = this.submitCollectedData.bind(this);
        document.getElementById('getScore').onclick = this.getScore.bind(this);
        document.getElementById('scheduleDataSubmission').onclick = this.scheduleDataSubmission.bind(this);
    },

    createSession: function(success, error){
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
        this.addMessage('Registration in progress');
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
        // if(!this.registerDone){
        //     alert('Please complete registration first');
        //     return;
        // }

        var media = this.media.value;
        this.addMessage('Starting authenticate with ' + media);
        if(media == 'Video'){
            AimBrain.captureVideoToAuthenticate(function(data){
                alert('Score: ' + data.score * 100 + '%');
                this.addMessage('Login successful ' + JSON.stringify(data));
                this.loginDone = true;
            }.bind(this), function(err){
                this.addMessage('Login failed ' + JSON.stringify(err));
            }.bind(this));
        }else{
            AimBrain.captureImageToAuthenticate(function(data){
                alert('Score: ' + data.score * 100 + '%');
                this.addMessage('Login successful ' + JSON.stringify(data));
                this.loginDone = true;
            }.bind(this), function(err){
                this.addMessage('Login failed ' + JSON.stringify(err));
            }.bind(this));
        }
    },

    clear: function() {
      this.messages.innerHTML = '';
    },

    session: function() {
      if(!this.loginDone){
          alert('Please complete login first');
          return;
      }

      AimBrain.getSession(function(session){
        this.addMessage('Session: ' + JSON.stringify(session));
      }.bind(this), function(err){
        this.addMessage('Failed to get session: ' + JSON.stringify(err));
      }.bind(this));
    },

    startCollectingData: function(){
      if(!this.loginDone){
          alert('Please complete login first');
          return;
      }

      AimBrain.startCollectingData(function(){
        this.addMessage('Data Collection Started');
      }.bind(this), function(err){
        this.addMessage('Error while starting data collection ' + JSON.stringify(err));
      }.bind(this));
    },

    stopCollectingData: function(){
      if(!this.loginDone){
          alert('Please complete login first');
          return;
      }

      AimBrain.stopCollectingData(function(){
        this.addMessage('Data Collection Stopped');
      }.bind(this), function(err){
        this.addMessage('Error while stopping data collection ' + JSON.stringify(err));
      }.bind(this));
    },

    submitCollectedData: function(){
      if(!this.loginDone){
          alert('Please complete login first');
          return;
      }

      AimBrain.submitCollectedData(function(data){
        this.addMessage('Data Collection Submitted');
        this.addMessage(JSON.stringify(data));
      }.bind(this), function(err){
        this.addMessage('Error while submitting data collection ' + JSON.stringify(err));
      }.bind(this));
    },

    getScore: function(){
      if(!this.loginDone){
          alert('Please complete login first');
          return;
      }

      AimBrain.getCurrentScore(function(data){
        this.addMessage('Current Score ' + JSON.stringify(data));
      }.bind(this), function(err){
        this.addMessage('Error while getting current score ' + JSON.stringify(err));
      }.bind(this));
    },

    scheduleDataSubmission: function(){
      if(!this.loginDone){
          alert('Please complete login first');
          return;
      }

      var delay = parseInt(this.delay.value);
      var period = parseInt(this.period.value);

      this.addMessage('Scheduling data submission for delay ' + delay + ' and period ' + period);
      AimBrain.scheduleDataSubmission(delay, period, function(data){
        this.addMessage('Scheduling successfully done');
      }.bind(this), function(err){
        this.addMessage('Error while scheduling ' + JSON.stringify(err));
      }.bind(this));
    }
};

app.initialize();
