
var firebase = require('firebase-admin');
var request = require('request');

var API_KEY = "YOUR FIREBASE SERVER API KEY"; // Your Firebase Cloud Messaging Server API key

// Fetch the service account key JSON file contents
var serviceAccount = require("PATH/TO/SERVICE/ACCOUNT/KEY/FILE");

// Initialize the app with a service account, granting admin privileges
firebase.initializeApp({
    credential: firebase.credential.cert(serviceAccount),
    databaseURL: "https://<YOUR PROJECT FIREBASE DB URL>"
});

function sendNotificationToUser(recipient, message, onSuccess) {
    request({
        url: 'https://fcm.googleapis.com/fcm/send',
        method: 'POST',
        headers: {
            'Content-Type' :' application/json',
            'Authorization': 'key='+API_KEY
        },
        body: JSON.stringify({
            data:message,
            notification:{
              title:"Your Fridge has low Milk...",
                body:""+(message.liters<=0 ? " You're out of milk!..":"Current Milk level has gone below 25%. You have "+message.liters+" L")
            },
            to :recipient
        })
    }, function(error, response, body) {
        if (error) { console.error(error); }
        else if (response.statusCode >= 400) {
            console.error('HTTP Error: '+response.statusCode+' - '+response.statusMessage);
        }
        else {
            onSuccess(response);
        }
    });
};
function sendPushNotification(log){
	console.log(" Trying to send a push notification for the change in the log");
    var UserDevice= require('./models/userdevice');
    UserDevice.findOne({DeviceName:"milk_listener"},function (err, device) {
        if(err)
            console.log(err);
        else
        {
            var deviceid= device.DeviceToken;
            sendNotificationToUser(deviceid,log,function () {
               console.log("success");
            });

        }
    });
};
module.exports = {

	
	getAllMilkLogs:function (res) {
	var Log=require('./models/milk_log');
		Log.find({},function(err,logs){
			if(err) res.send(500,err);
			res.json(logs);

		});
	},
	renderAllMilkLogs:function (res) {
		var Log=require('./models/milk_log');
		Log.find({},function(err,logs){
			if(err) res.send(500,err);
			res.render('milk.ejs', {data:logs });

		});
	},
	getMilkStatus:function (res) {
		var Log=require('./models/milk_log');
		Log.findOne().sort('-time').exec(function(err, post) {
			if(err)
				res.send(500,err);
			else
				res.json(post)});
	},
	addMilkLog:function (log, res) {
		var Log=require('./models/milk_log');
		var newLog= Log({data:log});
		try{
			var liters= log.liters;
			var capacity= log.capacity;
			var current= 100*liters/capacity;
			if(current<25){
				sendPushNotification(log);
			}
		}
		catch(ex){
			console.log(ex);
		}
		newLog.save(function(err){
			if (err){ res.json(500,null);}
			res.json(200,newLog);
		});
	},
	
	updateDeviceToken:function(deviceName,token,cb){
		var UserDevice= require('./models/userdevice');
		UserDevice.findOneAndUpdate({DeviceName:deviceName}, { $set: { DeviceToken: token }},{upsert:true}, cb);
	}

}