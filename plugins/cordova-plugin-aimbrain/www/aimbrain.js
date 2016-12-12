var argscheck = require('cordova/argscheck'),
  exec = require('cordova/exec');

var AimBrain = {
  SessionStatus: {
    NOT_ENROLLED: 0,
    ENROLLED: 1,
    BUILDING: 2
  },

  ScoreStatus: {
    AUTHENTICATING: 1,
    ENROLLING: 0
  },

  ErrorCode: {
    UNKNOWN_ERROR: 0,
    INVALID_ARGUMENT_ERROR: 1,
    TIMEOUT_ERROR: 2,
    PENDING_OPERATION_ERROR: 3,
    IO_ERROR: 4,
    NOT_SUPPORTED_ERROR: 5,
    OPERATION_CANCELLED_ERROR: 6,
    PERMISSION_DENIED_ERROR: 20
  },

  init: function(key, secret, successCB, errorCB){
    var keyVar, secretVar, successF = function(){}, errorF = function(){};
    var args = [];

    if(arguments.length === 4){
      keyVar = arguments[0];
      secretVar = arguments[1];
      successF = successCB;
      errorF = errorCB;
    }else if(arguments.length <= 2){
      if(typeof arguments[0] === 'string') keyVar = arguments[0];
      if(typeof arguments[1] === 'string') secretVar = arguments[1];
      if(typeof arguments[0] === 'function') successF = arguments[0];
      if(typeof arguments[1] === 'function') successF = arguments[1];
    }

    if(keyVar && secretVar){
      args = [keyVar, secretVar];
    }
    exec(successF, errorF, "AimBrain", "init", args);
  },
  createSession: function(sessionId, successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "createSession", [sessionId]);
  },
  startCollectingData: function(successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "startCollectingData", []);
  },
  stopCollectingData: function(successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "stopCollectingData", []);
  },
  getSession: function(successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "getSession", []);
  },
  setSession: function(id, successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "setSession", [id]);
  },
  getCurrentScore: function(successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "getCurrentScore", []);
  },
  submitCollectedData: function(successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "submitCollectedData", []);
  },
  scheduleDataSubmission: function(delay, period, successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "scheduleDataSubmission", [delay, period]);
  },
  captureImageToEnroll: function(successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "captureImageToEnroll", []);
  },
  captureVideoToEnroll: function(successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "captureVideoToEnroll", []);
  },
  captureImageToAuthenticate: function(successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "captureImageToAuthenticate", []);
  },
  captureVideoToAuthenticate: function(successCB, errorCB){
    exec(successCB, errorCB, "AimBrain", "captureVideoToAuthenticate", []);
  },
  clearImage: function(successCB, errorCB){
    errorCB("Not yet implemented");
  },
  clearVideo: function(successCB, errorCB){
    errorCB("Not yet implemented");
  }
}

module.exports = AimBrain;
