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
    session = require('express-session'),
    expressErrorHandler = require('express-error-handler'),
    bodyParser = require('body-parser'),
    static = require('serve-static'),
    path = require('path'),
    
    crypto = require('crypto'),
    mongoose = require('mongoose');

// express 이용 HTTP 서버 설정
var app = express();
app.set('port', process.env.PORT || 14402);
app.set('mongoose-reconnect-max', 5);

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
    var databaseUrl = 'mongodb://localhost:27017/matching';

    mongoose.Promise = global.Promise;
    mongoose.connect(databaseUrl, { useMongoClient: true });
    database = mongoose.connection;

    database.on('error', console.error.bind(console, '[심각] MongoDB 연결 오류'));
    database.on('open', function() {
        console.log('[정보] MongoDB 연결 성공');
        createSchema();

        database.on('disconnected', function() {
            var tries;
            if(!(tries = app.get('mongoose-reconnect-try')))
                app.set('mongodb-reconnect-try', 1);
            else if (tries >= app.get('mongoose-reconnect-max')) {
                console.log('[심각] MongoDB 연결 불가능. 서버를 종료합니다.ㄴ');
                return;
            }
            app.set('mongodb-reconnect-try', tries + 1);

            console.log('[심각] MongoDB 연결 끊김. 5초 뒤 재연결 시도합니다.');
            setTimeout(connectDB, 5000);
        });
    });
}

function createSchema() {

    // 사용자 스키마
    Schema.user = mongoose.Schema({
        id: { type: String, required: true, unique: true, default: ' ' },
        hashed_password: { type: String, required: true, unique: false, default: ' ' },
        salt: { type: String, required: true },
        name: { type: String, index: 'hashed', default: ' ' },
        rank: { type: Number, default: 0 },
        gender: { type: Number, default: 0 },
        unit: { type: String, index: 'hashed', default: ' ' },
        favoriteEvent: { type: Object, required: false, default: {} },
        description: { type: String, required: true, unique: false, default: ' ' },
        created_at: { type: Date, index: { unique: false }, default: Date.now },
        updated_at: { type: Date, index: { unique: false }, default: Date.now }
    });

    Schema.user.virtual('password').set(function(plaintext) {
        this._plaintext = plaintext;
        this.salt = this.makeSalt();
        this.hashed_password = this.encryptSHA1(plaintext);
    }).get(function() {
        return this._plaintext;
    });

    Schema.user.method('encryptSHA1', function(plaintext, salt) {
        return crypto.createHmac('sha1', salt || this.salt).update(plaintext).digest('hex');
    });

    Schema.user.method('auth', function(plaintext, salt, hashed_password) {
        return this.encryptSHA1(plaintext, salt || null) == hashed_password;
    });

    Schema.user.static('authenticate', function(userInfo, callback) {
        this.find({ username: userInfo.username }, function(err, result) {
            if(err) throw err;

            var resultDoc = result._doc;
            if(resultDoc) {
                var user = new Model.user({ username: userInfo.username });
                if(user.auth(userInfo.password, resultDoc[0].salt, resultDoc[0].hashed_password))
                    callback(true);
                else
                    callback(false);
                return;
            } else
                callback(null);
        });
    });

    // 경기 매칭 스키마
    Schema.matching = mongoose.Schema({
        activityType: { type: String, required: true, unique: false, default: ' ' },
        participants: { type: Object, required: true, unique: false, default: {} },
        start_at: { type: String, required: true, unique: false, default: ' ' }
    });
}

function createUser(userInfo, callback) {
    var User = new Model.user(userInfo);
    User.save(function(err){
        if(err) callback(err);
        else callback(null);
    });
}

// express Router 이용 Request routing
var router = express.Router();

// 사용자 추가 (회원가입)
router.route('/process/registerUser').post(function(req, res) {
    var userInfo = {
        id: req.body.id,
        password: req.body.password,
        name: req.body.name,
        rank: req.body.rank,
        unit: req.body.unit,
        gender: req.body.gender,
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
    createUser(userInfo, function(err) {
        if(err) throw err;
        console.log('[정보] 회원가입 완료: ID [%s]', userInfo.username);
    });
});

router.route('/process/loginUser').post(function(req, res) {
    var userInfo = {
        username: req.body.username,
        password: req.body.password
    };
    Model.user.authenticate(userInfo, function(result) {
        if(result) {
            console.log('[알림] 로그인 성공!');
            res.write({ result: 'Success' });
        } else if (result == null)
            res.write({ result: 'No such user.' });
        else if (result == false)
            res.write({ result: 'Password not match.' });
        res.end();
    });
});

router.route('/process/getMatchList').get(function(req, res) {
    // 현재 진행중인 Match 목록
    res.json({ result: 'Router Works (getMatchList)' });
    res.end();
});

router.route('/process/requestMatch').post(function(req, res) {
    // 새로 추가된 Match
    // 무결성 검증 필요
    res.json({ result: 'Router Works (addMatch)' });
    res.end();
});

router.route('/process/heartbeat').get(function(req, res) {
    // Heartbeat
    res.json({ result: 'Success' });
    res.end();
});

router.route('/process/checkExistingUser').post(function (req, res) {
    // 기존 회원 ID를 확인한다.


});

// Express에 각 미들웨어 적용 및 서버 시작

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.use(static(path.join(__dirname, 'public')));

app.use(router);

app.use(session({
    secret: 'F$GKeE%tJaf($&#(SfGISf*%#n#@!zSWh9',
    resave: true,
    saveUninitialized: true
}));

app.use(expressErrorHandler.httpError(404));
app.use(expressErrorHandler({
    static: {
        '404': './include/404.html'
    }
}));

// HTTP 서버 구동
http.createServer(app).listen(app.get('port'), function() {
    connectDB();
    console.log('[정보] 서버 시작됨. %d에서 listen 중', app.get('port'));
});
