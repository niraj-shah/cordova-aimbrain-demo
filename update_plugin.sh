cordova plugin rm cordova-plugin-aimbrain --verbose

rm -fr plugins/cordova-plugin-aimbrain

cordova plugin add https://bitbucket.org/n_shah/aimbraincordovaplugin --variable AIMBRAIN_API_KEY=de545b49-62bf-41c9-8e69-5ff1a8860d18 --variable AIMBRAIN_SECRET_KEY=2bb3c63945d742c688c6c29e5428f80edb6b38d19cae4b7986417a8c3cfaec74 --variable AIMBRAIN_RECORDING_HINT="Please BLINK now"
