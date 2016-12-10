r mongoose = require('mongoose');
var shortid = require('shortid');
var logSchema = mongoose.Schema({

    logID	: {type: String,unique: true,'default': shortid.generate},
    data    : {
        type: Object,'default':{}
    },
    time : { type : Date, default: Date.now }
    
});

logSchema.options.toJSON= {
    transform: function(doc, ret) {
        delete ret._id;
        delete ret.__v;
    }
};
logSchema.methods.getLog=function(){
    return this;
};
// create the model for milk and expose it to our app
module.exports = mongoose.model('MilkLog', logSchema);