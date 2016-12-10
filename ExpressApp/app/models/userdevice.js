// app/models/device.js
// load the things we need
var mongoose = require('mongoose');
var shortid = require('shortid');
var deviceSchema = mongoose.Schema({
	DeviceName	: String,
	DeviceToken	: {type: String,unique: true},
});

deviceSchema.options.toJSON= {
    transform: function(doc, ret) {
        delete ret._id;
        delete ret.__v;
    }
};
deviceSchema.methods.getPlantID=function(){
    return PlantID;
};
// create the model for device and expose it to our app
module.exports = mongoose.model('UserDevice', deviceSchema);