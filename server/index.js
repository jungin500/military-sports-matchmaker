/**
 * 전투체육 매칭 Backend
 * 
 * 이 App에서는 매칭 데이터를 받아 처리한 뒤,
 * Client에서 원하는 때 받아 쓸 수 있도록 처리해두고
 * 필요할 때 request하여 받아쓸 수 있게 합니다.
 * 
 * @version 1.0.0
 * @description 전투체육 매칭 Application Backend
 * @author 김범수, 안정인, 임대인
 */

var http = require('http'),
    express = require('express'),
    bodyParser = require('body-parser'),
    session = require('session'),
    expressErrorHandler = require('express-error-handler'),
    //static = require('serve-static'),
    //path = require('path'),
    
    mongoose = require('mongoose');

// MongoOSE 이용 DB 초기화
var database;

var Schema = {
    user: null,
    matching: null
};

var Model = {
    user: null,
    matching: null
};

function connectDB() {
    var userDBUrl = 'mongodb://localhost:27017/users';
    var matchingDBUrl = 'mongodb://localhost:27017/matching';

    var matching
}

// express 이용 HTTP 서버 설정
var app = express();
app.set('port', process.env.PORT || 14402);

http.createServer(app).listen(app.get('port'), function() {
    console.log('[정보] 서버 시작됨. %d에서 listen 중', app.get('port'));
});

// express Router 이용 Request routing
var router = express.Router();

// 사용자 추가 (회원가입)
router.route('/process/registerUser').post(function(req, res) {
    var userInfo = {
        username: req.body.username,
        password: req.body.password,
        budae: req.body.budae,
        milId: req.body.milId,
        favoriteEvent : req.body.favoriteEvent,
        description: req.body.description
    };

    // 정보 중 하나라도 빠졌을 시 오류
    for(var key in userInfo)
        if(!userInfo[key]) {
            res.writeHead(200, {'Content-Type':'text/html;charset=utf8'});
            res.end('정보가 잘못되었습니다. 모든 정보를 입력해주세요.');
            return;
        }

    // 가져온 정보를 MongoOSE 이용하여 DB에 저장

});


app.use(router);