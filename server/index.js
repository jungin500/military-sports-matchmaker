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
    cookieParser = require('cookie-parser'),
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
    database.on('open', function () {
        console.log('[정보] MongoDB 연결 성공');
        createSchema();

        database.on('disconnected', function () {
            var tries;
            if (!(tries = app.get('mongoose-reconnect-try')))
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

    Schema.user.virtual('password').set(function (plaintext) {
        this._plaintext = plaintext;
        this.salt = this.makeSalt();
        this.hashed_password = this.encryptSHA1(plaintext);
    }).get(function () {
        return this._plaintext;
    });

    Schema.user.method('encryptSHA1', function (plaintext, salt) {
        return crypto.createHmac('sha1', salt || this.salt).update(plaintext).digest('hex');
    });

    Schema.user.method('auth', function (plaintext, salt, hashed_password) {
        return this.encryptSHA1(plaintext, salt || null) == hashed_password;
    });

    Schema.user.method('makeSalt', function () {
        return Math.floor(Date.now() * Math.random() * Math.random());
    });

    Schema.user.static('findId', function(userInfo, callback) {
        this.find({ id: userInfo.id }, function(err, result) { 
            if(err)
                throw err;
            else if(result.length == 0)
                callback({
                    result: false,
                    reason: 'NoSuchUserException'
                });
            else if (result.length > 1)
                callback({
                    result: false,
                    reason: 'MultipleUserException'
                });
            else 
                callback({ 
                    result: true,
                    id: userInfo.id,
                    doc: result[0]._doc
                });
        });
    });

    Schema.user.static('authenticate', function (userInfo, callback) {
        this.findId(userInfo, function(result) {
            if(!result.result)
                callback(result);
            else {
                var user = new Model.user({ id: userInfo.id });
                if (user.auth(userInfo.password, result.doc.salt, result.doc.hashed_password))
                    callback({
                        result: true,
                        id: userInfo.id
                    });
                else
                    callback({
                        result: false,
                        reason: 'PasswordMismatch'
                    });
                return;
            }
        });
    });

    // 경기 매칭 스키마
    Schema.matching = mongoose.Schema({
        activityType: { type: String, required: true, unique: false, default: ' ' },
        participants: { type: Object, required: true, unique: false, default: {} },
        matchId: { type: String, required: true, unique: true, default: ' ' },
        start_at: { type: Date, required: true, index: { unique: false }, default: Date.now },
        finish_at: { type: Date, required: false, index: { unique: false }, default: Date.now }
    });

    Schema.matching.method('getMatch', function(matchId, callback) {
        this.find({ 'matchId': matchId }, function(err, result) {
            if(err) throw err;
            
            if(result.length > 0)
                callback(result[0]._doc);
            else
                callback(null);
        });
    });

    Schema.matching.static('findMatch', function(matchInfo, callback) {
        if(matchInfo.matchId) {
            // 만일 matchId를 가지고 있을 때
            this.getMatch(matchInfo.matchId, function(result) {
                if(!result)
                    callback({
                        result: true
                    });
            });
            
        }
    });

    // 모델 만들기
    Model.user = mongoose.model('user', Schema.user);
    Model.matching = mongoose.model('matching', Schema.matching);
}

function createUser(userInfo, callback) {
    var User = new Model.user(userInfo);
    User.save(function (err) {
        if (err) callback(err);
        else callback(null);
    });
}

// express Router 이용 Request routing
var router = express.Router();

// Express에 각 미들웨어 적용 및 서버 시작
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.use(static(path.join(__dirname, 'public')));

app.use(cookieParser());
app.use(session({
    secret: 'F$GKeE%tJaf($&#(SfGISf*%#n#@!zSWh9',
    resave: true,
    saveUninitialized: true,
    cookie: {
        maxAge: 1000 * 60 * 60
    }
}));

app.use(router);

app.use(expressErrorHandler.httpError(404));
app.use(expressErrorHandler({
    static: {
        '404': './include/404.html'
    }
}));

// HTTP 서버 구동
http.createServer(app).listen(app.get('port'), function () {
    connectDB();
    console.log('[정보] 서버 시작됨. %d에서 listen 중', app.get('port'));
});

// 라우터 설정
// 사용자 추가 (회원가입)
router.route('/process/registerUser').post(function (req, res) {
    var userInfo = {
        id: req.body.id,
        password: req.body.password,
        name: req.body.name,
        rank: req.body.rank,
        unit: req.body.unit,
        gender: req.body.gender,
        favoriteEvent: req.body.favoriteEvent,
        description: req.body.description
    };

    // 정보 중 하나라도 빠졌을 시 오류
    for (var key in userInfo)
        if (!userInfo[key]) {
            res.json({
                result: false,
                reason: 'MissingValuesException'
            });
            res.end();
            return;
        }

    // 가져온 정보를 MongoOSE 이용하여 DB에 저장
    createUser(userInfo, function (err) {
        if (err) {
            if (err.code == 11000) {
                console.log('[오류] 이미 존재하는 사용자에 대한 회원가입');
                res.json({
                    success: false,
                    reason: 'AlreadyExistingException'
                });
                res.end();
                return;
            } else {
                console.log('에러 발생!');
                console.dir(err);
                console.log('에러 출력 완료');
                throw err;
            }
        } else {
            console.log('[정보] 회원가입 완료: ID [%s]', userInfo.id);
            res.json({
                success: true,
                id: userInfo.id
            });
            res.end();
            return;
        }
    });
});

router.route('/process/checkLoggedIn').get(function(req, res) {
    console.dir(req.session);
    res.json({
        logged_as: req.session.username
    });
    res.end();
});

router.route('/process/loginUser').post(function (req, res) {
    var userInfo = {
        id: req.body.id,
        password: req.body.password
    };
    Model.user.authenticate(userInfo, function (result) {
        if(result.result) {
            console.log('세션에 추가 중');
            req.session.id = result.id;
            req.session.save(function(err) {
                if(err) throw err;
                console.log('세션 저장 완료');
                console.dir(req.session);
            });
        }
        res.json(result);
        res.end();
    });
});

router.route('/process/getMatchList').get(function (req, res) {
    // 현재 진행중인 Match 목록
    res.json({ result: 'Router Works (getMatchList)' });
    res.end();
});

router.route('/process/requestMatch').post(function (req, res) {
    var matchInfo = {
        id: req.session.id,
        activityType: req.body.activityType,
        maxUsers: req.body.maxUsers,
        matchId: req.body.matchId || null
    };

    Model.matching.findMatch(matchInfo, function(err, result) {
        if(err) throw err;
        
        res.json(result);
        res.end();
    });
});

router.route('/process/heartbeat').get(function (req, res) {
    // Heartbeat
    res.json({ result: 'Success' });
    res.end();
});

router.route('/process/checkExistingUser').post(function (req, res) {
    // 기존 회원 ID를 확인한다.
    var userInfo = {
        id: req.body.id
    };

    Model.user.findId(userInfo, function(result) {
        res.json(result);
        res.end();
    });
});
