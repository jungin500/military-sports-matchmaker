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
var mongoose = require('mongoose'),
    crypto = require('crypto');

// MongoOSE 이용 DB 초기화
var database;

var Schema = {
    user: null,
    matching: null,
    stadium: null
};

var Model = {
    user: null,
    matching: null,
    stadium: null
};

/**
* 데이터베이스에 연결합니다.
* @param {express} app Express Application
*/
var connectDB = function (app) {
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
                console.log('[심각] MongoDB 연결 불가능. 서버를 종료합니다.');
                return;
            }
            app.set('mongodb-reconnect-try', tries + 1);

            console.log('[심각] MongoDB 연결 끊김. 5초 뒤 재연결 시도합니다.');
            setTimeout(connectDB, 5000);
        });
    });
};

/**
* 스키마를 생성합니다.
*/
var createSchema = function () {

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

    Schema.user.static('findId', function (userInfo, callback) {
        this.find({ id: userInfo.id }, function (err, result) {
            if (err) {
                callback({
                    result: false,
                    reason: 'MongoError',
                    mongoerror: err
                });
                return;
            } else if (result.length == 0)
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
                    doc: result[0]._doc
                });
        });
    });

    Schema.user.static('authenticate', function (userInfo, callback) {
        this.findId(userInfo, function (result) {
            if (!result.result)
                callback(result);
            else {
                var user = new Model.user({ id: userInfo.id });
                if (user.auth(userInfo.password, result.doc.salt, result.doc.hashed_password))
                    callback({
                        result: true,
                        id: userInfo.id,
                        name: result.doc.name,
                        rank: result.doc.rank
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

    Schema.user.static('getUserInfo', function(userInfo, callback) {
        this.findId(userInfo, function(result) {
            if(!result.result)
                callback(result);
            else 
                callback({
                    result: true,
                    id: result.doc.id,
                    name: result.doc.name,
                    rank: result.doc.name,
                    gender: result.doc.gender,
                    unit: result.doc.unit,
                    favoriteEvent: result.doc.favoriteEvent,
                    description: result.doc.description,
                    created_at: result.doc.created_at,
                    updated_at: result.doc.updated_at
                });
        });
    });

    Schema.user.static('updateUserInfo', function(userInfo, callback) {
        // Model.user
    });

    // 경기 매칭 스키마
    Schema.matching = mongoose.Schema({
        initiatorId: { type: String, required: true, unique: false },
        activityType: { type: String, required: true, unique: false, default: ' ' },
        players: { type: Array, required: true, unique: false, default: [] },
        matchId: { type: String, required: true, unique: true },
        stadium: { type: String, required: true, unique: false, default: 'Normal Stadium' },
        start_at: { type: Date, required: true, index: { unique: false }, default: Date.now }
    });

    Schema.matching.static('getMatch', function (initiatorId, callback) {
        this.find({ 'initiatorId': initiatorId }, function (err, result) {
            if (err) {
                callback({
                    result: false,
                    reason: 'MongoError',
                    mongoerror: err
                });
                return;
            }

            if (result.length == 0)
                callback({
                    result: false,
                    reason: 'NoSuchMatchException'
                });
            else if (result.length > 1)
                callback({
                    result: false,
                    reason: 'MultipleMatchException'
                })
            else
                callback({
                    result: true,
                    match: result[0]._doc
                });
        });
    });

    Schema.matching.static('findMatch', function (matchInfo, callback) {
        console.dir(matchInfo);
        if (matchInfo.matchId)
            this.getMatch(matchInfo.matchId, function (result) {
                if (!result.result)
                    callback(result);
                else {
                    var maxUsers = result.doc.maxUsers;
                    var partUsers = result.doc.participants.length;

                    if (partUsers >= maxUsers) {
                        callback({
                            result: false,
                            reason: 'FullMatchException'
                        })
                    } else {
                        callback({
                            result: true,
                            participants: result.doc.participants,
                            description: 'MatchUpdatePending'
                        })
                    }
                }
            });
        else
            callback({
                result: false,
                reason: 'NoMatchIdException'
            })
    });

    Schema.matching.static('createMatch', function (matchInfo, callback) {
        matchInfo.matchId = generateMatchId();
        console.log('[정보] 새로운 매칭을 생성합니다. 매치 ID [%s]', matchInfo.matchId);

        var match = new Model.matching(matchInfo);

        match.save(function (err) {
            if (err) {
                callback({
                    result: false,
                    reason: 'MongoError',
                    mongoerror: err
                });
                return;
            }

            callback({
                result: true
            });
        });
    });

    Schema.matching.static('deleteMatch', function (initiatorId, matchId, callback) {
        this.find({ matchId: matchId }, function (err, result) {
            if (err)
                callback({
                    result: false,
                    reason: 'MongoError',
                    mongoerror: err
                })
            else if (result.length == 0)
                callback({
                    result: false,
                    reason: 'NoSuchMatchException'
                })
            else if (result[0]._doc.initiatorId != initiatorId)
                callback({
                    result: false,
                    reason: 'ForbiddenOperationException'
                });
            else
                Model.matching.remove({ matchId: matchId }, function (err) {
                    if (err)
                        callback({
                            result: false,
                            reason: 'MongoError',
                            mongoerror: err
                        });
                    else {
                        console.log('[정보] 매치를 삭제합니다. 매치 ID [%s]', matchId);
                        callback({
                            result: true
                        });
                    }
                });
        });


    });

    Schema.matching.static('getAllMatches', function (callback) {
        this.find({}, function (err, result) {
            if (err)
                callback({
                    result: false,
                    reason: 'MongoError',
                    mongoerror: err
                });
            else
                callback({
                    result: true,
                    docs: result
                })
        });
    });

    // 경기장 스키마
    Schema.stadium = mongoose.Schema({
        name: { type: String, required: true, unique: true },
        available_type: { type: Array, required: true, unique: false },
        belong_at: { type: String, required: true, unique: false },
        max_players: { type: Number, required: true, unique: false },
        min_players: { type: Number, required: true, unique: false },
        in_players: { type: Number, required: false, unique: false },
        matches: { type: Array, required: false, unique: false },
        modified_at: { type: Date, required: true, index: { unique: false }, default: Date.now }
    });

    // 모델 만들기
    Model.user = mongoose.model('user', Schema.user);
    Model.matching = mongoose.model('matching', Schema.matching);
    Model.stadium = mongoose.model('stadium', Schema.stadium);
};

/**
* 사용자를 생성하는 함수입니다.
* 
* @param {Object} userInfo 사용자 정보를 담고 있는 객체
* @param {Function} callback 콜백 함수
*/
var createUser = function (userInfo, callback) {
    var User = new Model.user(userInfo);
    User.save(function (err) {
        if (err) callback(err);
        else callback(null);
    });
};

var generateMatchId = function () {
    return crypto.randomBytes(24).toString('hex');
}

/**
* 모듈 Export
*/
module.exports = {
    // Global Variables
    database: database,
    Schema: Schema,
    Model: Model,

    // Functions
    connectDB: connectDB,
    createUser: createUser
};