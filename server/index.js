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
    DatabaseManager = require('./lib/DatabaseManager'),
    UserManager = require('./lib/UserManager');

// express 이용 HTTP 서버 설정
var app = express();
app.set('port', process.env.PORT || 14402);
app.set('mongoose-reconnect-max', 5);

// express Router 이용 Request routing
var router = express.Router();

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
    DatabaseManager.createUser(userInfo, function (err) {
        if (err) {
            if (err.code == 11000) {
                console.log('[오류] 이미 존재하는 사용자에 대한 회원가입');
                res.json({
                    result: false,
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
                result: true
            });
            res.end();
            return;
        }
    });
});

router.route('/process/checkLoggedIn').get(function (req, res) {

    if (req.session.userInfo)
        res.json({
            result: true
        });
    else
        res.json({
            result: false
        });

    res.end();
});

router.route('/process/loginUser').post(function (req, res) {
    var userInfo = {
        id: req.body.id,
        password: req.body.password
    };
    DatabaseManager.Model.user.authenticate(userInfo, function (result) {
        if (result.result) {
            console.log('[정보] 로그인 완료. 세션에 추가 중');
            req.session.userInfo = {
                id: userInfo.id
            };
        } else {
            console.log('[오류] 로그인 불가. 사유: %s', result.reason);
        }
        res.json(result);
        res.end();
    });
});

router.route('/process/logoutUser').get(function (req, res) {
    req.session.destroy(function (err) {
        if (err) throw err;
        res.json({
            result: true
        });
        res.end();

        console.log('[정보] 로그아웃 및 세션 초기화 완료');
    });
});

router.route('/process/getMatchList').get(function (req, res) {
    DatabaseManager.Model.matching.getAllMatches(function (result) {
        res.json(result);
        res.end();
    });
});

router.route('/process/getUserMatch').get(function (req, res) {
    if (!req.session.userInfo) {
        res.json({
            result: false,
            reason: 'NotLoggedInException'
        });
        res.end();
        return;
    }

    DatabaseManager.Model.matching.getMatch(req.session.userInfo.id, function (result) {
        res.json(result);
        res.end();
    });
});

router.route('/process/requestMatch').post(function (req, res) {
    if (!req.session.userInfo) {
        res.json({
            result: false,
            reason: 'NotLoggedInException'
        });
        res.end();
        return;
    }

    var matchInfo = {
        initiatorId: req.session.userInfo.id,
        activityType: req.body.activityType,
        players: req.body.players.split('|')
    }

    DatabaseManager.Model.matching.createMatch(matchInfo, function (result) {
        res.json(result);
        res.end();
    });
});

router.route('/process/deleteMatch').post(function (req, res) {
    if (!req.session.userInfo) {
        res.json({
            result: false,
            reason: 'NotLoggedInException'
        });
        res.end();
        return;
    }

    var initiatorId = req.session.userInfo.id;
    var matchId = req.body.matchId;
    DatabaseManager.Model.matching.deleteMatch(initiatorId, matchId, function (result) {
        res.json(result);
        res.end();
    });
});

router.route('/process/getStadiumList').post(function (req, res) {
    if (!req.session.userInfo) {
        res.json({
            result: false,
            reason: 'NotLoggedInException'
        });
        res.end();
        return;
    }

    DatabaseManager.Model.user.findId(req.session.userInfo, function (result) {
        if (!result.result) {
            res.json(result);
            res.end();
        } else {
            //  vi userInfo = 
        }
    })
});

router.route('/process/heartbeat').get(function (req, res) {
    // Heartbeat
    res.json({ result: 'result' });
    res.end();
});

router.route('/process/checkExistingUser').post(function (req, res) {
    // 기존 회원 ID를 확인한다.
    var userInfo = {
        id: req.body.id
    };

    DatabaseManager.Model.user.findId(userInfo, function (result) {
        if (result.result)
            res.json({
                result: true
            });
        else
            res.json(result);
        res.end();
    });
});

router.route('/process/searchUserDetails').post(function (req, res) {
    if (req.session.userInfo)
        DatabaseManager.Model.user.findId({ id: req.body.id }, function (result) {
            if (result.result)
                res.json({
                    result: true,
                    name: result.doc.name,
                    rank: result.doc.rank
                });
            else
                res.json({
                    result: false,
                    reason: 'NoSuchUserException'
                });
            res.end();
        });
    else {
        res.json({
            result: false,
            reason: 'NotLoggedInException'
        });
        res.end();
    }
});

// Express에 각 미들웨어 적용 및 서버 시작
app.use(cookieParser());
app.use(session({
    secret: 'F$GKeE%tJaf($&#(SfGISf*%#n#@!zSWh9',
    resave: true,
    saveUninitialized: true
}));

app.use(function(req, res, next) {
    console.log('접근 받음');
    console.dir(req);

    next();
});

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.use(static(path.join(__dirname, 'public')));

app.use('/', router);

app.use(expressErrorHandler.httpError(404));
app.use(expressErrorHandler({
    static: {
        '404': './include/404.html'
    }
}));

// HTTP 서버 구동
var server = http.createServer(app).listen(app.get('port'), function () {
    DatabaseManager.connectDB(app);
    console.log('[정보] 서버 시작됨. %d번 Port에서 listen 중', app.get('port'));
});

server.on('request', function (req, res) {
    if (req.connection.remoteAddress == '::1')
        console.log('[정보] 내부 연결');
    else
        console.log('[정보] 외부 연결: %s', req.connection.remoteAddress.toString().split('::ffff:')[1]);
});