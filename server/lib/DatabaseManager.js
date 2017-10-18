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
 * Mongoose (MongoDB)에서 발생한 Error가 있을 경우 처리하는 함수입니다.
 * 
 * 처리한 뒤, 에러가 발생시 False, 아닐 경우 true를 반환해 Synchronous Operation에서
 * 해당 Flag로 Continuous 여부를 판단할 수 있습니다.
 * @param {Error} err MongoError 
 * @param {Function} callback 콜백 함수
 */
var mongoErrorCallbackCheck = function (err, callback) {
    if (err) {
        callback({
            result: false,
            reason: 'MongoError',
            mongoerror: err
        });
        return false;
    }
    return true;
}

/**
* 스키마를 생성합니다.
*/
var createSchema = function () {

    /**
     * 사용자 정보 스키마 및 메소드 정의
     */
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
        profile_image: { type: String, required: false, unique: false },
        match_history: { type: Array, required: false, unique: false, default: [] },
        match_ongoing: { type: String, required: false, unique: false, default: ' ' },
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

    var findId = function (userInfo, callback) {
        this.find({ id: userInfo.id }, function (err, result) {
            if (mongoErrorCallbackCheck(err, callback) && result.length == 0)
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
    };

    var authenticate = function (userInfo, callback) {
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
    };

    var getUserInfo = function (userInfo, callback) {
        this.findId(userInfo, function (result) {
            if (!result.result)
                callback(result);
            else
                callback({
                    result: true,
                    id: result.doc.id,
                    name: result.doc.name,
                    rank: result.doc.rank,
                    gender: result.doc.gender,
                    unit: result.doc.unit,
                    favoriteEvent: result.doc.favoriteEvent,
                    description: result.doc.description,
                    match_history: result.doc.match_history,
                    match_ongoing: result.doc.match_ongoing,
                    created_at: result.doc.created_at,
                    updated_at: result.doc.updated_at
                });
        });
    };

    var updateUserInfo = function (targetId, query, callback) {
        this.update({ id: targetId }, query, function (err) {
            if (mongoErrorCallbackCheck(err, callback)) {
                if (query.password) {
                    Model.user.findOne({ id: targetId }, function (err, result) {
                        if (mongoErrorCallbackCheck(err, callback)) {
                            result.set('password', query.password);
                            result.save(function (err) {
                                if (mongoErrorCallbackCheck(err, callback))
                                    callback({
                                        result: true
                                    });
                            });
                        }
                    });
                } else
                    callback({
                        result: true
                    });
            }
        });
    };

    Schema.user.static('findId', findId);
    Schema.user.static('authenticate', authenticate);
    Schema.user.static('getUserInfo', getUserInfo);
    Schema.user.static('updateUserInfo', updateUserInfo);

    /**
     * 경기 매칭 스키마 및 메소드 정의
     */
    Schema.matching = mongoose.Schema({
        initiatorId: { type: String, required: true, unique: false },
        activityType: { type: String, required: true, unique: false, default: ' ' },
        players: { type: Array, required: true, unique: false, default: [] },
        matchId: { type: String, required: true, unique: true },
        stadium: { type: String, required: true, unique: false },
        start_at: { type: Date, required: true, index: { unique: false }, default: Date.now }
    });

    var getMatch = function (initiatorId, callback) {
        this.find({ 'initiatorId': initiatorId }, function (err, result) {
            if (mongoErrorCallbackCheck(err, callback) && result.length == 0)
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
    };

    var createMatch = function (matchInfo, callback) {
        matchInfo.matchId = generateMatchId();

        // 적절한 위치를 찾아서 매치를 생성한다.
        Model.user.findOne({ id: matchInfo.initiatorId }, function (err, result) {
            var unit = result._doc.unit;

            Model.stadium.find({
                available_type: matchInfo.activityType,
                location: unit
            }, function (err, result) {
                if (mongoErrorCallbackCheck(err, callback)) {
                    if (result.length == 0)
                        callback({
                            result: false,
                            reason: 'NoMatchingStadiumException'
                        });
                    else {

                        // 오름차순 Sorting
                        result.sort(function (a, b) {
                            var leftStadiumLeft = a._doc.maxPlayers - a._doc.players.length;
                            var rightStadiumLeft = b._doc.maxPlayers - b._doc.players.length;
                            return leftStadiumLeft == rightStadiumLeft ? 0 :
                                leftStadiumLeft < rightStadiumLeft ? -1 : 1;
                        });

                        // 있는 것들중에서 가장 낮은수의 남은 Player부터 Assign.
                        for (var i = 0; i < result.length; i++) {
                            if (result[i]._doc.maxPlayers - result[i]._doc.players.length >= matchInfo.players.length) {
                                var doc = result[i]._doc;
                                if (doc.players.length == 0)
                                    doc.players = matchInfo.players;
                                else if (doc.players.length + matchInfo.players <= doc.max_players)
                                    doc.players = doc.players.concat(matchInfo.players);

                                // 완료되면 사용자에게 매치 데이터를 저장한다.

                                var match = new Model.matching(matchInfo);

                                match.save(function (err) {
                                    if (mongoErrorCallbackCheck(err, callback))
                                        // 사용자에게 해당 Match를 저장합니다.
                                        Model.user.update({ id: matchInfo.initiatorId }, {
                                            match_ongoing: matchInfo.matchId,
                                            $push: {
                                                match_history: matchInfo.matchId
                                            }
                                        }, function (err) {
                                            if (err) {
                                                callback({
                                                    result: false,
                                                    reason: 'MongoError',
                                                    mongoerror: err
                                                });
                                                return;
                                            }
                                            console.log('[정보] 새로운 매칭을 생성합니다. 매치 ID [%s]', matchInfo.matchId);
                                            console.log('[정보] 유저 %s에게 매치 데이터를 저장했습니다.', matchInfo.initiatorId);
                                            callback({
                                                result: true,
                                                stadium: doc.name
                                            });
                                        });
                                });
                                return; 
                            }
                        }

                        // Assign 불가능할 경우 Fail.
                        callback({
                            result: false,
                            reason: 'FailedAssigningStadiumException'
                        });
                        return;
                    }
                }
            });
        });
    };

    var deleteMatch = function (initiatorId, matchId, callback) {
        this.find({ matchId: matchId }, function (err, result) {
            if (mongoErrorCallbackCheck(err, callback) && result.length == 0)
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
                    if (mongoErrorCallbackCheck(err, callback)) {
                        console.log('[정보] 매치를 삭제합니다. 매치 ID [%s]', matchId);

                        Model.user.update({ id: initiatorId }, {
                            match_ongoing: ' '
                        }, function (err) {
                            if (mongoErrorCallbackCheck(err, callback))
                                callback({
                                    result: true
                                });
                        })
                    }
                });
        });


    };

    var getAllMatches = function (callback) {
        this.find({}, function (err, result) {
            if (mongoErrorCallbackCheck(err, callback))
                callback({
                    result: true,
                    docs: result
                })
        });
    };

    Schema.matching.static('getMatch', getMatch);
    Schema.matching.static('createMatch', createMatch);
    Schema.matching.static('deleteMatch', deleteMatch);
    Schema.matching.static('getAllMatches', getAllMatches);

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