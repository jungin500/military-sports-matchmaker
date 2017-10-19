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

console.log('전투체육 매칭 Backend');
console.log('=====================');
console.log('[정보] 서버를 시작합니다.');

var http = require('http'),
    express = require('express'),
    cookieParser = require('cookie-parser'),
    session = require('express-session'),
    expressErrorHandler = require('express-error-handler'),
    bodyParser = require('body-parser'),
    fs = require('fs'),

    cors = require('cors'),
    multer = require('multer'),

    formidable = require('formidable'),
    crypto = require('crypto'),
    md5 = require('md5'),

    at = require('array-tools'),

    static = require('serve-static'),
    path = require('path'),
    DatabaseManager = require('./lib/DatabaseManager'),
    UserManager = require('./lib/UserManager');

process.on('uncaughtException', function (err) {
    console.log('[심각] 치명적 오류 발생: ' + err);
});

// express 이용 HTTP 서버 설정
var app = express();
app.set('port', process.env.PORT || 14402);
app.set('mongoose-reconnect-max', 5);

// express Router 이용 Request routing
var router = express.Router();

function checkAndSendLoggedIn(req, res) {
    if (!req.session.userInfo) {
        res.json({
            result: false,
            reason: 'NotLoggedInException'
        });
        res.end();
        return false;
    }

    return true;
}

function sendIllegalParameters(req, res) {
    res.json({
        result: false,
        reason: 'IllegalParametersException'
    });
    res.end();
}

// 사용자 이미지 파일 저장소 설정

var storage = multer.diskStorage({
    destination: function (req, file, callback) {
        callback(null, 'files/image');
    },
    filename: function (req, file, callback) {
        var split = file.originalname.split('.');
        var extension = split[split.length - 1];
        var randomId = crypto.randomBytes(24).toString('hex');

        callback(null, randomId + '.' + extension);
    }
})

var upload = multer({
    storage: storage,
    limits: {
        files: 1,
        fileSize: 1024 * 1024 * 16
    }
});



// 라우터 설정
// 사용자 추가 (회원가입)
router.route('/process/registerUser').post(upload.single('profPic'), function (req, res) {
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
            sendIllegalParameters(req, res);
            return;
        }

    // 사용자 프로필 이미지 파일 처리
    userInfo.profile_image = (req.file) ? req.file.path : null;

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
            req.session.userInfo = {
                id: userInfo.id
            };

            res.json({
                result: true
            });
            res.end();
            return;
        }
    });
});

router.route('/process/loginUser').post(function (req, res) {
    var userInfo = {
        id: req.body.id,
        password: req.body.password
    };

    for (var key in userInfo)
        if (!userInfo[key]) {
            sendIllegalParameters(req, res);
            return;
        }

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
    if (checkAndSendLoggedIn(req, res))
        req.session.destroy(function (err) {
            if (err) throw err;
            res.json({
                result: true
            });
            res.end();

            console.log('[정보] 로그아웃 및 세션 초기화 완료');
        });
});

router.route('/process/checkExistingUser').post(function (req, res) {
    // 기존 회원 ID를 확인한다.

    var id = req.body.id;
    if (!id) { sendIllegalParameters(req, res); return; }

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

router.route('/process/getUserDetails').post(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;

    var id = req.body.id;
    if (!id) { sendIllegalParameters(req, res); return; }

    DatabaseManager.Model.user.findId({ id: id }, function (result) {
        if (result.result)
            res.json({
                result: true,
                name: result.doc.name,
                rank: result.doc.rank,
                profile_image: result.doc.profile_image ? true : false
            });
        else
            res.json({
                result: false,
                reason: 'NoSuchUserException'
            });
        res.end();
    });
});

router.route('/process/getUsersDetails').post(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;

    var users = req.body.users;
    if (!users) { sendIllegalParameters(req, res); return; }

    DatabaseManager.Model.user.getUsersDetails(users, function (result) {
        res.json(result);
        res.end();
    });
});

router.route('/process/getUserInfo').get(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;

    DatabaseManager.Model.user.getUserInfo(req.session.userInfo, function (result) {
        res.json(result);
        res.end();
    });
});

router.route('/process/updateUserInfo').post(upload.single('profPic'), function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;

    var targetId = req.session.userInfo.id;

    var userInfo = {
        name: req.body.name,
        rank: req.body.rank,
        gender: req.body.gender,
        password: req.body.password,
        unit: req.body.unit,
        favoriteEvent: req.body.favoriteEvent,
        description: req.body.description,
        profile_image: req.file ? req.file.path : null
    };

    // 빈 값은 들어가지 않도록 한다.
    for (var key in userInfo)
        if (!userInfo[key])
            delete userInfo[key];

    DatabaseManager.Model.user.updateUserInfo(targetId, userInfo, function (result) {
        res.json(result);
        res.end();
    });
});

router.route('/process/getProfileImage').get(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;
    var userId = req.query.userid;

    DatabaseManager.Model.user.getProfileImagePath({ id: userId }, function (result) {
        if (!result.result) {
            res.json(result);
            res.end();
            console.dir(result);
        } else {
            fs.createReadStream(path.join(__dirname, result.profile_image)).pipe(res);
        }
    });
});

router.route('/process/getMatchList').get(function (req, res) {
    DatabaseManager.Model.matching.getAll(function (result) {
        res.json(result);
        res.end();
    });
});

router.route('/process/getUserMatch').get(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;
    DatabaseManager.Model.matching.getUserMatch(req.session.userInfo.id, function (result) {
        if (!result.result) {
            res.json(result);
            res.end();
            return;
        }

        // 현재 사용자가 초대사용자인지 확인
        // 초대를 수락한 뒤에는 False.
        var pendingPlayers = result.pendingPlayers;
        if (pendingPlayers && pendingPlayers.indexOf(req.session.userInfo.id) != -1)
            result.is_pending = true;
        else
            result.is_pending = false;

        res.json(result);
        res.end();
    });
});

router.route('/process/requestMatch').post(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;

    if (!(req.body.activityType && req.body.players)) { sendIllegalParameters(req, res); return; }

    var initiatorId = req.session.userInfo.id;
    var players = req.body.players.split('|');

    // TODO: initiator 플레이어가 중앙에 있으면 큰 문제가 된다.
    // 그럴 일은 일단 없으므로 Pass.
    if (!players.includes(initiatorId))
        players.unshift(initiatorId);

    // 로그인되지 않은 Player의 경우 항상 players에 간다. (있을 경우만)
    DatabaseManager.Model.user.checkIdsExistance(players, function (result) {
        if (!result.result) {
            res.json(result);
            res.end();
            return;
        }

        var matchInfo = {
            initiatorId: initiatorId,
            activityType: req.body.activityType,
            players: result.notFoundUser.concat(initiatorId),
            pendingPlayers: at(result.existingUser).without(initiatorId)._data,
            is_team: req.body.is_team
        };

        console.dir(matchInfo);

        DatabaseManager.Model.matching.getUserMatch(req.session.userInfo.id, function (result) {
            if (result.result) {
                // 사용자가 이미 매치를 가지고 있는 경우
                res.json({
                    result: false,
                    reason: 'MatchAlreadyExistsException'
                });
                res.end();
            } else  // 그렇지 않은 경우
                DatabaseManager.Model.matching.createMatch(matchInfo, function (result) {
                    res.json(result);
                    res.end();
                });
        });
    });
});

router.route('/process/deleteMatch').post(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;

    if (!req.body.matchId) { sendIllegalParameters(req, res); return; }

    var initiatorId = req.session.userInfo.id;
    var matchId = req.body.matchId;
    DatabaseManager.Model.matching.deleteMatch(initiatorId, matchId, function (result) {
        res.json(result);
        res.end();
    });
});

// 매치를 참가할 것인가 결정한다.
router.route('/process/decideMatch').post(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;

    var isParticipating = req.body.isParticipating;
    var userInfo = req.session.userInfo;

    DatabaseManager.Model.user.getUserInfo(userInfo, function (result) {
        if (result.match_status == 'ready')
            res.json({
                result: false,
                reason: 'NotInMatchException'
            });
        else if (result.match_status == 'matching' && isParticipating)
            res.json({
                result: false,
                reason: 'AleradyInMatchException'
            });
        else {
            DatabaseManager.Model.matching.decideMatch(userInfo, isParticipating, function (result) {
                res.json(result);
                res.end();
            });
            return;
        }
        res.end();
    });
});

router.route('/process/getStadiumList').get(function (req, res) {
    DatabaseManager.Model.stadium.find({}, function (err, result) {
        if (err)
            res.json({
                result: false,
                reason: 'MongoError',
                mongoerror: err
            });
        else
            res.json(result);
        res.end();
    })
});

router.route('/process/getUserStadium').get(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;

    // 사용자의 unit이 속한 경기장 리스트 보여주기
    DatabaseManager.Model.user.findId({ id: req.session.userInfo.id }, function (result) {
        if (!result.result) {
            res.json(result);
            res.end();
        } else {
            DatabaseManager.Model.stadium.findUserStadium(result.doc.unit, function (result) {
                res.json(result);
                res.end();
            })
        }
    });
});

/**
 * 경기장
 */
router.route('/process/createStadium').post(function (req, res) {
    // DEBUG
    //if(!checkAndSendLoggedIn(req, res)) return;

    var stadiumInfo = {
        name: req.body.name,
        available_type: req.body.available_type,
        belong_at: req.body.belong_at,
        max_players: req.body.max_players
    };

    // 정보 중 하나라도 빠졌을 시 오류
    for (var key in stadiumInfo)
        if (!stadiumInfo[key]) {
            sendIllegalParameters(req, res);
            return;
        }

    // "|" 으로 분리
    stadiumInfo.available_type = stadiumInfo.available_type.split('|');

    DatabaseManager.Model.stadium.createStadium(stadiumInfo, function (result) {
        res.json(result);
        res.end();
    });
});

router.route('/process/prepareMatchingTeamStadium').post(function (req, res) {
    if (!checkAndSendLoggedIn(req, res)) return;

    var stadiumInfo = { name: req.body.name };
    if (!stadiumInfo.name) { sendIllegalParameters(req, res); return; }

    DatabaseManager.Model.stadium.prepareMatchingTeamStadium(stadiumInfo, function (result) {
        console.log('{Object} => %s', md5(JSON.stringify(result)));
        res.json(result);
        res.end();
    });
});

router.route('/process/heartbeat').get(function (req, res) {
    // Heartbeat
    res.json({ result: 'result' });
    res.end();
});

// Express에 각 미들웨어 적용 및 서버 시작
app.use(cookieParser());
app.use(session({
    secret: 'F$GKeE%tJaf($&#(SfGISf*%#n#@!zSWh9',
    resave: true,
    saveUninitialized: true
}));

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

app.use(cors());

router.use(function (req, res, next) {
    var connectionInfo = {
        timestamp: Date.now(),
        location: (req.connection.remoteAddress == '::1') ? '로컬' : req.connection.remoteAddress.toString().split('::ffff:')[1],
        requestUrl: req.url,
        requests: req.body,
        session: req.session
    }

    if (connectionInfo.requestUrl.indexOf('favicon.ico') == -1) {
        console.log('[정보] 연결 정보');
        console.dir(connectionInfo);
        console.log('');
    }

    next();
});

app.use(static(path.join(__dirname, 'public')));
app.use(static(path.join(__dirname, 'files')));

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

