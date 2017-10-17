
var mongoose = require('mongoose'),
    crypto = require('crypto');

// MongoOSE 이용 DB 초기화
var database;

var Schema = {
    User: null,
    Queue: null
};

var Model = {
    User: null,
    Queue: null
};

/**
 * 데이터베이스에 연결합니다.
 * @param {Express} app Express Application
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
                console.log('[심각] MongoDB 연결 불가능. 서버를 종료합니다.ㄴ');
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
    Schema.User = mongoose.Schema({
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

    Schema.User.virtual('password').set(function (plaintext) {
        this._plaintext = plaintext;
        this.salt = this.makeSalt();
        this.hashed_password = this.encryptSHA1(plaintext);
    }).get(function () {
        return this._plaintext;
    });


    Schema.User.method('encryptSHA1', function (plaintext, salt) {
        return crypto.createHmac('sha1', salt || this.salt).update(plaintext).digest('hex');
    });

    Schema.User.method('auth', function (plaintext, salt, hashed_password) {
        return this.encryptSHA1(plaintext, salt || null) == hashed_password;
    });

    Schema.User.method('makeSalt', function () {
        return Math.floor(Date.now() * Math.random() * Math.random());
    });

    Schema.User.static('findId', function (UserInfo, callback) {
        this.find({ id: UserInfo.id }, function (err, result) {
            if (err)
                throw err;
            else if (result.length == 0)
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
                    id: UserInfo.id,
                    doc: result[0]._doc
                });
        });
    });

    Schema.User.static('authenticate', function (UserInfo, callback) {
        this.findId(UserInfo, function (result) {
            if (!result.result)
                callback(result);
            else {
                var User = new Model.User({ id: UserInfo.id });
                if (User.auth(UserInfo.password, result.doc.salt, result.doc.hashed_password)) {
                    var userInfo = {
                        id: result.doc.id,
                        name: result.doc.name,
                        rank: result.doc.rank,
                        gender: result.doc.gender,
                        unit: result.doc.unit,
                        favoriteEvent: result.doc.favoriteEvent,
                        description: result.doc.description
                    };
                    callback({
                        result: true,
                        doc: userInfo

                    });
                } else
                    callback({
                        result: false,
                        reason: 'PasswordMismatch'
                    });
                return;
            }
        });
    });

    /* 
    // 경기 매칭 스키마
    Schema.matching = mongoose.Schema({
        activityType: { type: String, required: true, unique: false, default: ' ' },
        participants: { type: Array, required: true, unique: false, default: [] },
        matchId: { type: String, required: true, unique: true },
        maxUsers: { type: Number, required: true, unique: false, default: ' ' },
        start_at: { type: Date, required: true, index: { unique: false }, default: Date.now },
        finish_at: { type: Date, required: false, index: { unique: false }, default: Date.now }
    });

    Schema.matching.static('findMatch', function (matchInfo, callback) {

        if (matchInfo.matchId) {
            this.find({ 'matchId': matchInfo.matchId }, function (err, result) {
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
                        doc: result[0]._doc
                    });
            })

        } else
            callback({
                result: false,
                reason: 'NoMatchIdException'
            })
    });

    // 여기서 사용하는 matchInfo에는 기존 participants가 들어간다.
    Schema.matching.static('updateMatchParticipants', function (matchInfo, callback) {
        var newParticipants = matchInfo.participants.concat(matchInfo.participantId);

        Model.matching.update({ matchId: matchInfo.matchId }, {
            participants: newParticipants
        }, function (err) {
            if (err) {
                callback({
                    result: false,
                    reason: 'MongoError',
                    mongoerror: err
                });
                return;
            }

            callback({
                result: true,
                participants: newParticipants
            })
        });
    });

    Schema.matching.static('createMatch', function (matchInfo, callback) {
        var matchId = generateMatchId();
        console.log('[정보] 새로운 매칭을 생성합니다. 매치 ID [%s]', matchId);

        var match = new Model.matching({
            participants: [matchInfo.participantId],
            activityType: matchInfo.activityType,
            maxUsers: matchInfo.maxUsers,
            matchId: generateMatchId()
        });

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
                result: true,
                match: match
            });
        });
    });

    Schema.matching.static('quitMatch', function (matchInfo, callback) {
        this.findMatch(matchInfo, function (result) {
            if (!result.result)
                callback({
                    result: false,
                    reason: 'NoSuchMatchException'
                });
            else {
                var doc = result.doc;
                
                if(doc.participants.indexOf(matchInfo.participantId) == -1) {
                    callback({
                        result: false,
                        reason: 'NoSuchUserInMatch'
                    });
                    return;
                }
            }

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
    }); */
    
    Schema.Queue = mongoose.schema({
        activityType: { type: String, required: true, unique: false, default: ' ' },
        participants: { type: Array, required: true, unique: false, default: [] },
        matchId: { type: String, required: true, unique: true },
        start_at: { type: Date, required: true, index: { unique: false }, default: Date.now },
        finish_at: { type: Date, required: false, index: { unique: false }, default: Date.now }
    });
    
    /**
     * findUserQueue()
     * 
     * 해당 사용자가 들어있는 큐를 찾아낸다.
     * 
     * @param {String} userId 사용자의 이름을 나타냄
     * @param {Function} callback(result) 콜백 함수를 나타냄
     */
    var findUserQueue = function(userId, callback) {
        this.find({
            participants: {
                $elemMatch: userId 
            }
        }, function(err, result) {
            if(err) {
                callback({
                    result: false,
                    reason: 'MongoError',
                    mongoerror: err
                });
                return;
            }
    
            if(result.length == 0)
                callback({
                    result: false,
                    reason: 'NoSuchElementException'
                });
            else
                callback({
                    result: true,
                    doc: result[0]._doc
                });
        });
    };
    
    /**
     * findAllQueues()
     * 
     * 모든 큐를 반환한다.
     * 
     * @param {Function} callback(results) 콜백 함수
     */
    var findAllQueues = function(callback) {
        this.find({}, function(err, result) {
            if(err) {
                callback({
                    result: false,
                    reason: 'MongoError',
                    mongoerror: err
                });
                return;
            }
    
            if(result.length == 0)
                callback({
                    result: false,
                    reason: 'NoSuchElementException'
                });
            else
                callback({
                    result: true,
                    docs: result
                });
        });
    };
    
    // 스태틱 함수 설정
    Schema.Queue.static('findUserQueue', findUserQueue);
    Schema.Queue.static('findAllQueues', findAllQueues);
    
    Model.Queue = mongoose.model('queue', Schema.Queue);
    
    // 모델 만들기
    Model.User = mongoose.model('User', Schema.User);
    Model.Queue = mongoose.model('Queue', Schema.Queue);
};

/**
 * 사용자를 생성하는 함수입니다.
 * 
 * @param {Object} userInfo 사용자 정보를 담고 있는 객체
 * @param {Function} callback 콜백 함수
 */
var createUser = function (userInfo, callback) {
    var user = new Model.User(userInfo);
    user.save(function (err) {
        if (err) callback(err);
        else callback(null);
    });
};

/**
 * 큐에 사용자를 추가하는 함수입니다.
 * 
 * @param {Object} queueInfo 큐 정보를 담고 있는 객체
 * @param {Function} callback 콜백 함수
 */
var addQueue = function(queueInfo, callback) {

    Schema.Queue.findUserQueue(queueInfo.participantId, function(result) {
        if(result.result) {
            // 큐가 존재할 때
            
        } else {
            // 큐가 존재하지 않을 때

        }
    });

    var queue = new Model.Queue(queueInfo);
    queue.save(function (err) {
        if(err) callback(err)
        else callback(null);
    });
};

var generateQueueID = function () {
    return crypto.randomBytes(48).toString('hex');
};

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
    createUser: createUser,
    addQueue: addQueue
};