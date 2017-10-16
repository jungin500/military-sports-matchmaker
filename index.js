/**
 * 전투체육 매칭 Backend
 * 
 * @author 
 */

var http = require('http'),
    express = require('express'),
    expressErrorHandler = require('express-error-handler'),
    //static = require('serve-static'),
    //path = require('path'),
    
    mongoose = require('mongoose');

var app = express();
app.set('port', process.env.PORT || 14402);
