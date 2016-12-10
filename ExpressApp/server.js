#!/usr/bin/node
// server.js
// set up ======================================================================
// get all the tools we need
var express  = require('express');
var app      = express({name:'IoT Server'});
var port     = process.env.PORT || 80;
var vhost= require('vhost');
var morgan       = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser   = require('body-parser');


// configuration ===============================================================


// set up our express application
var mongoose = require('mongoose');
var passport = require('passport');
var flash    = require('connect-flash');
var session      = require('express-session');
var mongoManager= require("./app/mongooseManager.js");
var configDB = require('./config/database.js');
mongoose.connect(configDB.url); // connect to our database

app.use(morgan('dev')); // log every request to the console
app.use(cookieParser()); // read cookies (needed for auth)
app.use(bodyParser()); // get information from html forms
app.set('view engine', 'ejs'); // set up ejs for templating
app.set('views', __dirname + '/views');
//app.set('static', __dirname + '/static');
app.use('/static',express.static(__dirname + '/static'));

app.use(flash()); // use connect-flash for flash messages stored in session

require('./app/apiroutes.js')(app,mongoManager,mongoose, passport); // load our routes and pass in our app and fully configured

app.listen(port);

console.log('The magic happens on port ' + port);
