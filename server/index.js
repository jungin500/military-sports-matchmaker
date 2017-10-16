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
    expressErrorHandler = require('express-error-handler'),
    //static = require('serve-static'),
    //path = require('path'),
    
    mongoose = require('mongoose');

// express 이용 HTTP 서버 설정
var app = express();
app.set('port', process.env.PORT || 14402);

http.createServer(app).listen(app.get('port'), function() {
    console.log('[정보] 서버 시작됨. %d에서 listen 중', app.get('port'));
});

// express Router 이용 Request routing
var router = express.Router();

router.route('/').post(function(req, res) {
    
});


app.use(router);