cordova.define("com.test.MyPlugin.MyPlugin", function(require, exports, module) {
var exec = require('cordova/exec');

exports.aaa = function (arg0, success, error) {
    exec(success, error, 'MyPlugin', 'aaa', [arg0]);
};

});
