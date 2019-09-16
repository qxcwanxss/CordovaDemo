cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
  {
    "id": "com.test.MyPlugin.MyPlugin",
    "file": "plugins/com.test.MyPlugin/www/MyPlugin.js",
    "pluginId": "com.test.MyPlugin",
    "clobbers": [
      "xyz"
    ]
  }
];
module.exports.metadata = 
// TOP OF METADATA
{
  "cordova-plugin-whitelist": "1.3.3",
  "com.test.MyPlugin": "1.0.0"
};
// BOTTOM OF METADATA
});