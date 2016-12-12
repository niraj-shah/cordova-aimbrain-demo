/********* AimBrain.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "AimbrainSDK/AimbrainSDK.h"
#import "AimbrainSDK/AMBNEnrollFaceResult.h"
#import "AimbrainSDK/AMBNSessionCreateResult.h"
#import "AimbrainSDK/AMBNBehaviouralResult.h"
#import "AimbrainSDK/AMBNAuthenticateFaceResult.h"

@interface AimBrain : CDVPlugin <AMBNFaceRecordingViewControllerDelegate>{
	CDVPluginResult *pluginResult;
	NSString* captureVideo;
	CDVInvokedUrlCommand* globalCommand;
}

- (void) init:(CDVInvokedUrlCommand*)command;
- (void) createSession:(CDVInvokedUrlCommand*)command;
- (void) setSession: (CDVInvokedUrlCommand*)command;
- (void) getSession: (CDVInvokedUrlCommand*)command;
- (void) startCollectingData: (CDVInvokedUrlCommand*)command;
- (void) stopCollectingData: (CDVInvokedUrlCommand*)command;
- (void) submitCollectedData: (CDVInvokedUrlCommand*)command;
- (void) getCurrentScore: (CDVInvokedUrlCommand*)command;
- (void) captureImageToEnroll: (CDVInvokedUrlCommand*)command;
- (void) captureVideoToEnroll: (CDVInvokedUrlCommand*)command;
- (void) captureImageToAuthenticate: (CDVInvokedUrlCommand*)command;

@end

@implementation AimBrain

NSString *topHint = @"To authenticate please face the camera directly and press 'camera' button";
NSString *bottomHint = @"Position your face fully within the outline.";
int batchSize = 3;
float delay = 0.3;
NSString *recordingHint = @"";
int videoLength = 2;

- (CDVPluginResult*) errorPluginResult:(NSString*) code message: (NSString*) message{
    NSDictionary *errorObject = @{
                                  @"code" : code,
                                  @"message" : message
                                  };
    return [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary: errorObject];
}

- (CDVPluginResult*) checkError: (NSError*) error{
    if(error.code == -1009){
        return [self errorPluginResult: @"7" message: @"NETWORK_ERROR"];
    }else{
        return [self errorPluginResult: @"0" message: @"UNKNOWN_ERROR"];
    }
}

- (void) init:(CDVInvokedUrlCommand*)command
{
    pluginResult = nil;
    NSString* apiKey = [command.arguments objectAtIndex:0];
    NSString* secret = [command.arguments objectAtIndex:1];

    if (apiKey != nil && [apiKey length] > 0 && secret != nil && [secret length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [[AMBNManager sharedInstance] configureWithApiKey: apiKey secret: secret];
    } else{
    	pluginResult = [self errorPluginResult:@"1" message:@"INVALID_ARGUMENT_ERROR"];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) createSession:(CDVInvokedUrlCommand*)command
{
    pluginResult = nil;
    NSString* userId = [command.arguments objectAtIndex:0];

    if (userId != nil && [userId length] > 0) {
        [[AMBNManager sharedInstance] createSessionWithUserId: userId completionHandler:^(AMBNSessionCreateResult *result, NSError *error) {
	        if(!error){
	            NSDictionary *sessionObject = @{
	                                        @"sessionId" : result.session,
	                                        @"faceStatus" : result.face,
	                                        @"behaviourStatus" : result.behaviour
	                                        };
	            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: sessionObject];
	            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	        }else if(error){
	            NSLog(@"error: %@", error);
                pluginResult = [self checkError: error];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	        }
	    }];
    } else{
    	pluginResult = [self errorPluginResult:@"1" message:@"INVALID_ARGUMENT_ERROR"];
    	[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) setSession: (CDVInvokedUrlCommand*)command{
	pluginResult = nil;
    NSString* sessionId = [command.arguments objectAtIndex:0];
    if (sessionId != nil && [sessionId length] > 0){
        [AMBNManager sharedInstance].session = sessionId;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }else{
        pluginResult = [self errorPluginResult:@"1" message:@"INVALID_ARGUMENT_ERROR"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) getSession: (CDVInvokedUrlCommand*)command{
	pluginResult = nil;
    NSString *session = [AMBNManager sharedInstance].session;
    if(session){
        NSDictionary *sessionObject = @{
                                        @"session": session
                                        };
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: sessionObject];
    }else{
        pluginResult = [self errorPluginResult:@"5" message:@"NOT_SUPPORTED_ERROR"];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) startCollectingData: (CDVInvokedUrlCommand*)command{
	[[AMBNManager sharedInstance] start];
} 

- (void) stopCollectingData: (CDVInvokedUrlCommand*)command{
	[[AMBNManager sharedInstance] disableCapturingForAllViews];
}

- (void) submitCollectedData: (CDVInvokedUrlCommand*)command{
	pluginResult = nil;
    if(![AMBNManager sharedInstance].session){
        pluginResult = [self errorPluginResult:@"5" message:@"NOT_SUPPORTED_ERROR"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        [[AMBNManager sharedInstance] submitBehaviouralDataWithCompletionHandler:^(AMBNBehaviouralResult *result, NSError *error){
            if(result){
                NSDictionary *collectedData = @{
                                                @"session" : result.session,
                                                @"score" : result.score,
                                                @"status" : @(result.status)
                                                };
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: collectedData];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
            if(error){
                NSLog(@"Error: %@", error);
                pluginResult = [self checkError: error];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
        }];
    }
}

- (void) getCurrentScore: (CDVInvokedUrlCommand*)command{
	pluginResult = nil;
    if(![AMBNManager sharedInstance].session){
        pluginResult = [self errorPluginResult:@"5" message:@"NOT_SUPPORTED_ERROR"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        [[AMBNManager sharedInstance] getScoreWithCompletionHandler:^(AMBNBehaviouralResult *result, NSError *error){
            if(result){
                NSDictionary *currentScore = @{
                                               @"session" : result.session,
                                               @"score" : result.score,
                                               @"status" : @(result.status)
                                               };
                pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: currentScore];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
            if(error){
                NSLog(@"Error: %@", error);
                pluginResult = [self checkError: error];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
        }];
    }
} 

- (void) captureImageToEnroll: (CDVInvokedUrlCommand*)command{
	pluginResult = nil;
    if(![AMBNManager sharedInstance].session){
        pluginResult = [self errorPluginResult:@"5" message:@"NOT_SUPPORTED_ERROR"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        [[AMBNManager sharedInstance] openFaceImagesCaptureWithTopHint: topHint bottomHint: bottomHint batchSize: batchSize delay: delay fromViewController:self.viewController completion:^(NSArray *images, NSError *error) {
            if(images){
                [[AMBNManager sharedInstance] enrollFaceImages:images completionHandler:^(AMBNEnrollFaceResult *result, NSError *error) {
                    if(result.success){
                        double value = [result.imagesCount doubleValue];
                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:value ];
                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                    }
                }];
            }
            if(error){
                NSLog(@"Error: %@", error);
                pluginResult = [self checkError: error];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
        }];
    }
    
//    [self.commandDelegate runInBackground:^{
//        [[AMBNManager sharedInstance] openFaceImagesCaptureWithTopHint: topHint bottomHint: bottomHint batchSize: batchSize delay: delay fromViewController:self.viewController completion:^(NSArray *images, NSError *error) {
//            if(images){
//                [[AMBNManager sharedInstance] enrollFaceImages:images completion:^(BOOL success, NSNumber *imageCount, NSError *error) {
//                    if(success){
//                        double value = [imageCount doubleValue];
//                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:value ];
//                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
//                    }
//                }];
//            }
//            if(error){
//                NSLog(@"Error: %@", error);
//            }
//        }];
//    }];
}

- (void) captureVideoToEnroll: (CDVInvokedUrlCommand*)command{
	globalCommand = command;
	pluginResult = nil;
	captureVideo = @"enroll";
    pluginResult = nil;
    if(![AMBNManager sharedInstance].session){
        NSLog(@"Session doesn't exist");
        pluginResult = [self errorPluginResult:@"5" message:@"NOT_SUPPORTED_ERROR"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        AMBNFaceRecordingViewController *controller = [[AMBNManager sharedInstance] instantiateFaceRecordingViewControllerWithTopHint: topHint bottomHint: bottomHint recordingHint: recordingHint videoLength: videoLength];
        controller.delegate = self;
        [self.viewController presentViewController:controller animated:YES completion:nil];
    }
}

-(void)faceRecordingViewController:(AMBNFaceRecordingViewController *)faceRecordingViewController recordingResult:(NSURL *)video error:(NSError *)error {
    [faceRecordingViewController dismissViewControllerAnimated:YES completion:nil];
    if(error){
        pluginResult = [self checkError: error];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:globalCommand.callbackId];
    }else{
        if ([captureVideo isEqualToString:@"enroll"]){
            NSLog(@"Enroll");
            [[AMBNManager sharedInstance] enrollFaceVideo:video completionHandler:^(AMBNEnrollFaceResult *result, NSError *error) {
                if(result){
                    NSLog(@"Successful: %@", result.imagesCount);
                    NSDictionary *faceResult = @{
                                                 @"imagesCount" : result.imagesCount,
                                                 };
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: faceResult];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:globalCommand.callbackId];
                }else{
                    pluginResult = [self checkError: error];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:globalCommand.callbackId];
                }
            }];
        }
        else if([captureVideo isEqualToString:@"authenticate"]){
            NSLog(@"Authenticate");
            [[AMBNManager sharedInstance] authenticateFaceVideo:video completionHandler:^(AMBNAuthenticateFaceResult *result, NSError *error) {
                if(!error){
                    NSDictionary *faceResult = @{
                                                 @"score" : result.score,
                                                 @"liveliness" : result.liveliness
                                                 };
                    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: faceResult];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:globalCommand.callbackId];
                }else{
                    pluginResult = [self checkError: error];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:globalCommand.callbackId];
                }
            }];
        }
    }
}

- (void) captureImageToAuthenticate: (CDVInvokedUrlCommand*)command{
	pluginResult = nil;
    if(![AMBNManager sharedInstance].session){
        pluginResult = [self errorPluginResult:@"5" message:@"NOT_SUPPORTED_ERROR"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        [[AMBNManager sharedInstance] openFaceImagesCaptureWithTopHint: topHint bottomHint: bottomHint batchSize: batchSize delay: delay fromViewController:self.viewController completion:^(NSArray *images, NSError *error) {
            if(images){
                [[AMBNManager sharedInstance] authenticateFaceImages:images completionHandler:^(AMBNAuthenticateFaceResult *result, NSError *error) {
                    if(!error){
                        NSDictionary *faceResult = @{
                                                     @"score" : result.score,
                                                     @"liveliness" : result.liveliness
                                                     };
                        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: faceResult];
                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                    }else{
                        pluginResult = [self checkError: error];
                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                    }
                }];
            }
            if(error){
                NSLog(@"Error: %@", error);
                pluginResult = [self checkError: error];
                [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            }
        }];
    }
}

- (void) captureVideoToAuthenticate: (CDVInvokedUrlCommand*)command{
	globalCommand = command;
	captureVideo = @"authenticate";
	pluginResult = nil;
    if(![AMBNManager sharedInstance].session){
        pluginResult = [self errorPluginResult:@"5" message:@"NOT_SUPPORTED_ERROR"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        AMBNFaceRecordingViewController *controller = [[AMBNManager sharedInstance] instantiateFaceRecordingViewControllerWithTopHint: topHint bottomHint: bottomHint recordingHint: recordingHint videoLength: videoLength];
        controller.delegate = self;
        [self.viewController presentViewController:controller animated:YES completion:nil];
    }
}

@end