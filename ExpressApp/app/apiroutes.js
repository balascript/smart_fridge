// app/routes.js
module.exports = function(app,mongoManager, mongoose, passport) {
    /**
     * APIs for the MILK IOT Project
     */
    
    app.get('/milk-api',function (req, res) {
        mongoManager.getAllMilkLogs(res);
    });
    app.post('/milk-api/add',function (req, res) {
       var log= req.body;
        mongoManager.addMilkLog(log,res);

    });

    app.get('/milk-api/getMilkStatus',function (req, res) {
        mongoManager.getMilkStatus(res);
    });

    app.get('/milk-api/milk',function (req, res) {
        mongoManager.renderAllMilkLogs(res);
       
    });

    app.post('/milk-api/deviceUpdate',function (req, res) {
       if(!req.body.token &&  !req.body.devicename)
           res.send(500,{message:"send token and name for the device"});
        else
           mongoManager.updateDeviceToken(req.body.devicename,req.body.token,function (err) {
               if(err)
                   res.send(500,err);
               else
                   res.json(200,{message:'success'});
           });
    });


 
};


