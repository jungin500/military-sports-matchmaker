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
var at = require('array-tools'),
    mongoose = require('mongoose'),
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
    // var databaseUrl = 'mongodb://military-sports-matchmaker:622dfe4f39c220a76ec78eabd75e609b@ds125335.mlab.com:25335/heroku_w2n7bnmn';
    var databaseUrl = 'mongodb://localhost:27017/matching';
   
    mongoose.Promise = global.Promise;
    mongoose.connect(databaseUrl, { useMongoClient: true });
    database = mongoose.connection;

    database.on('unhandledException', function() { console.log('123a'); });
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
        switch (err.code) {
            case 11000:
                callback({
                    result: false,
                    reason: 'DuplicatedEntityException',
                    mongoerror: err
                });
                break;
            default:
                callback({
                    result: false,
                    reason: 'MongoError',
                    mongoerror: err
                });
        }

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
        match_ongoing: { type: String, required: false, unique: false, default: '' },
        match_status: { type: String, required: true, unique: false, default: 'ready' }, // ready, pending, matching
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
            if (!mongoErrorCallbackCheck(err, callback)) return;

            if (result.length == 0)
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
                    match_status: result.doc.match_status,
                    match_history: result.doc.match_history,
                    match_ongoing: result.doc.match_ongoing,
                    created_at: result.doc.created_at,
                    updated_at: result.doc.updated_at,
                    profile_image: result.doc.profile_image ? true : false
                });
        });
    };

    var getUsersDetails = function (userIdList, callback) {

        this.find({ id: { $in: userIdList } }, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;

            var sensitive_datas = ['_id', 'salt', '__v', 'hashed_password'];
            var resultSet = [];
            var resultObj = {
                result: true,
                data: resultSet
            };

            var resultUsersList = [];
            if (!(resultObj.complete = (userIdList.length > result.length) ? false : true)) {
                // 만약 하나라도 없으면?
                for (var resIdx = 0; resIdx < result.length; resIdx++)
                    resultUsersList.push(result[resIdx].id);

                resultObj.omittedUsers = at(userIdList).without(resultUsersList).val();
            }
            for (var i = 0; i < result.length; i++) {
                // 민감한 정보 제거
                var data = JSON.parse(JSON.stringify(result[i]));
                for (var key in data)
                    if (sensitive_datas.includes(key))
                        delete data[key];

                // 프로필 사진
                data.profile_image = result[i].profile_image ? true : false;

                // Set에 추가
                resultSet.push(data);
            }

            console.log(resultObj);

            callback(resultObj);
        });
    };

    var getProfileImagePath = function (userInfo, callback) {
        this.findId(userInfo, function (result) {
            if (!result.result)
                callback(result);
            else if (!result.doc.profile_image)
                callback({
                    result: false,
                    reason: 'NoProfileImageException'
                });
            else
                callback({
                    result: true,
                    profile_image: result.doc.profile_image
                });
        });
    };

    var updateUserInfo = function (targetId, query, callback) {
        this.update({ id: targetId }, query, function (err) {
            if (!mongoErrorCallbackCheck(err, callback)) return;

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
        });
    };

    var checkIdsExistance = function (ids, callback) {
        this.find({ id: { $in: ids } }, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;

            var data = [];
            for (var key in result)
                data.push(result[key]._doc.id);

            callback({
                result: true,
                existingUser: data,
                notFoundUser: at(ids).without(data).val()
            });
        });
    };

    Schema.user.static('findId', findId);
    Schema.user.static('authenticate', authenticate);
    Schema.user.static('getUserInfo', getUserInfo);
    Schema.user.static('getUsersDetails', getUsersDetails);
    Schema.user.static('updateUserInfo', updateUserInfo);
    Schema.user.static('getProfileImagePath', getProfileImagePath);
    Schema.user.static('checkIdsExistance', checkIdsExistance);

    /**
     * 경기 매칭 스키마 및 메소드 정의
     */
    Schema.matching = mongoose.Schema({
        initiatorId: { type: String, required: true, unique: false },
        activityType: { type: String, required: true, unique: false, default: ' ' },
        players: { type: Array, required: true, unique: false, default: [] },
        pendingPlayers: { type: Array, required: false, unique: false, default: [] },
        rejectedPlayers: { type: Array, required: false, unique: false, default: [] },
        is_team: { type: Boolean, required: false, unique: false, default: false },
        matchId: { type: String, required: true, unique: true },
        stadium: { type: String, required: true, unique: false },
        start_at: { type: Date, required: true, index: { unique: false }, default: Date.now }
    });

    var getMatch = function (matchId, callback) {
        this.find({ 'matchId': matchId }, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;

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
    };

    var getUserMatch = function (userId, callback) {
        this.findOne({ $or: [{ players: userId }, { pendingPlayers: userId }] }, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;

            if (!result)
                callback({
                    result: false,
                    reason: 'NoSuchMatchException'
                });
            else {
                var stadiumName = result._doc.stadium;
                var userResult = {
                    result: true,
                    match: result._doc
                };

                Model.stadium.findOne({ name: stadiumName }, function (err, result) {
                    if (!mongoErrorCallbackCheck(err, callback)) return;

                    userResult.match.stadium = result._doc;

                    callback(userResult);
                });
            }
        });
    };

    /**
     * 새로운 매치를 생성한다.
     * 
     * @param {Object} matchInfo 매치에 대한 정보(initiatorId, activityType, {Array} players)
     * @param {Function} callback 콜백 함수 ({Object} result)
     */
    var createMatch = function (matchInfo, callback) {
        matchInfo.matchId = crypto.randomBytes(24).toString('hex');

        // 적절한 위치를 찾아서 매치를 생성한다.
        Model.user.findOne({ id: matchInfo.initiatorId }, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;

            var query = {
                available_type: matchInfo.activityType,
                belong_at: result._doc.unit
            };

            Model.user.find({
                $or: [{
                    id: { $in: matchInfo.players },
                    match_status: { $ne: 'ready' }
                }, {
                    id: { $in: matchInfo.pendingPlayers },
                    match_status: { $ne: 'ready' }
                }]
            }, function (err, result) {
                if (!mongoErrorCallbackCheck(err, callback)) return;

                if (result.length > 0) {
                    callback({
                        result: false,
                        reason: 'SubuserAlreadyInMatchException'
                    });
                    return;
                }

                Model.stadium.find(query, function (err, result) {
                    if (!mongoErrorCallbackCheck(err, callback)) return;
                    if (result.length == 0) {
                        callback({
                            result: false,
                            reason: 'NoMatchingStadiumException'
                        });
                        return;
                    } else {
                        // 오름차순 Sorting
                        result = result.sort(function (a, b) {
                            var leftStadiumLeft = a.max_players - a.in_players;
                            var rightStadiumLeft = b.max_players - b.in_players;
                            return leftStadiumLeft == rightStadiumLeft ? 0 :
                                leftStadiumLeft < rightStadiumLeft ? -1 : 1;
                        });

                        // 있는 것들중에서 가장 낮은수의 남은 Player의 Stadium부터 Assign.
                        for (var i = 0; i < result.length; i++) {
                            var doc = result[i];
                            var totalPlayers = matchInfo.players.length + matchInfo.pendingPlayers.length;
                            var remainSeats = doc.max_players - doc.in_players;

                            console.log('%d명의 총 플레이어, %d명의 남는 자리.', totalPlayers, remainSeats);

                            if (remainSeats >= totalPlayers) {
                                console.log('총 %d명을 추가합니다!', totalPlayers);

                                var in_players = doc.in_players + totalPlayers;

                                Model.stadium.updateMany({ _id: doc._id }, {
                                    $inc: {
                                        in_players: totalPlayers
                                    },
                                    $push: {
                                        matchings: matchInfo.matchId
                                    }
                                }, function (err) {
                                    if (!mongoErrorCallbackCheck(err, callback)) return;

                                    // 완료되면 사용자에게 매치 데이터를 저장한다.
                                    // 저장할 때 Stadium 정보가 필요하므로 기록했던 데이터로부터 가져온다.
                                    matchInfo.stadium = doc.name;
                                    var match = new Model.matching(matchInfo);

                                    match.save(function (err) {
                                        if (!mongoErrorCallbackCheck(err, callback)) return;

                                        // 사용자들에게 해당 Match를 저장한다.
                                        // 필터 기준은 id가 각각 players와 pendingPlayers에 속하는 경우.
                                        var findQuery = {
                                            $or: [{ id: { $in: matchInfo.pendingPlayers } }, { id: { $in: matchInfo.players } }]
                                        };

                                        var updateQuery = {
                                            match_ongoing: matchInfo.matchId,
                                            $push: {
                                                match_history: matchInfo.matchId
                                            },
                                            match_status: 'pending'
                                        };

                                        Model.user.updateMany(findQuery, updateQuery, function (err) {
                                            if (!mongoErrorCallbackCheck(err, callback)) return;

                                            Model.user.updateMany({ id: { $in: matchInfo.players } }, {
                                                match_status: 'matching'
                                            }, function (err) {
                                                if (!mongoErrorCallbackCheck(err, callback)) return;

                                                console.log('[정보] 새로운 매칭을 생성합니다. 매치 ID [%s]', matchInfo.matchId);
                                                console.log('[정보] 유저 [%s, %s](들)에게 매치 데이터를 저장했습니다.', JSON.stringify(matchInfo.players), JSON.stringify(matchInfo.pendingPlayers));
                                                callback({
                                                    result: true,
                                                    stadium: doc.name,
                                                    ready: doc.max_players <= in_players
                                                });
                                            });
                                        });
                                    });

                                });
                                return; // 종료해야 2개 이상의 Stadium에 Assign되지 않음.
                            }
                        }

                        // Assign 불가능할 경우 Fail.
                        callback({
                            result: false,
                            reason: 'FailedAssigningStadiumException'
                        });
                        return;
                    }

                });
            });
        });
    };

    var deleteMatch = function (initiatorId, matchId, callback) {
        this.find({ matchId: matchId }, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;

            if (result.length == 0)
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
                Model.matching.findOne({ matchId: matchId }, function (err, result) {
                    var totalPlayers = result._doc.players.length + result._doc.pendingPlayers.length;
                    var stadiumName = result._doc.stadium;

                    var doc = result._doc;

                    Model.matching.remove({ matchId: matchId }, function (err) {
                        if (!mongoErrorCallbackCheck(err, callback)) return;

                        console.log('[정보] 매치를 삭제합니다. 매치 ID [%s]', matchId);

                        Model.user.updateMany({
                            $or: [{ id: { $in: doc.pendingPlayers } }, { id: { $in: doc.players } }]
                        }, {
                                match_ongoing: '',
                                match_status: 'ready'
                            }, function (err) {
                                if (!mongoErrorCallbackCheck(err, callback)) return;

                                Model.stadium.update({ name: stadiumName }, {
                                    $pull: {
                                        matchings: matchId
                                    },
                                    $inc: {
                                        in_players: -totalPlayers
                                    }
                                }, function (err) {
                                    if (!mongoErrorCallbackCheck(err, callback)) return;
                                    callback({
                                        result: true
                                    });
                                });
                            })
                    });
                });
        });
    };

    // 호출은 getAll
    var getAllMatchings = function (callback) {
        this.find({}, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;
            callback({
                result: true,
                docs: result
            });
        });
    };

    var decideMatch = function (userInfo, isParticipating, callback) {
        this.findOne({ pendingPlayers: userInfo.id }, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;
            else if (!result) {
                callback({
                    result: false,
                    reason: 'NoSuchUserException'
                });
                return;
            }

            var matchId = result._doc.matchId;

            Model.matching.update({ matchId: matchId }, {
                $pull: { pendingPlayers: userInfo.id }
            }, function (err) {
                if (!mongoErrorCallbackCheck(err, callback)) return;

                // 해당 사용자의 상태를 ready로 변경한다.
                Model.user.update({ id: userInfo.id }, {
                    match_status: 'ready'
                }, function (err) {
                    if (!mongoErrorCallbackCheck(err, callback)) return;

                    // 매치 정보 업데이트 (matching)
                    var updateAddPlayer = { $push: { players: userInfo.id } };
                    var updateAddRejectedPlayer = { $push: { rejectedPlayers: userInfo.id } };

                    // 사용자 정보 업데이트 (user)
                    var updateMatchingPlayerStatus = { match_status: 'matching' };
                    var updateRemovingOngoingUserStatus = { match_ongoing: '' };

                    // 스타디움 정보 업데이트 (매치 거절했을 경우)
                    var updateDecreasePlayers = { $inc: { in_players: -1 } };

                    // [매치 정보 업데이트] 참가하는 경우: 참가 플레이어에 추가, 참가하지 않는경우: 불참가 플레이어에 추가
                    Model.matching.update({ matchId: matchId }, isParticipating ? updateAddPlayer : updateAddRejectedPlayer, function (err) {
                        if (!mongoErrorCallbackCheck(err, callback)) return;

                        // [사용자 정보 업데이트] 매치 상태 업데이트
                        Model.user.update({ id: userInfo.id }, isParticipating ? updateMatchingPlayerStatus : updateRemovingOngoingUserStatus, function (err) {
                            if (!mongoErrorCallbackCheck(err, callback)) return;

                            if (!isParticipating)
                                Model.stadium.update({ matchings: matchId }, updateDecreasePlayers, function (err) {
                                    if (!mongoErrorCallbackCheck(err, callback)) return;
                                    callback({
                                        result: true
                                    });
                                });
                            else
                                callback({
                                    result: true
                                });
                        });
                    });

                });
            });
        });
    };

    Schema.matching.static('getMatch', getMatch);
    Schema.matching.static('getUserMatch', getUserMatch);
    Schema.matching.static('createMatch', createMatch);
    Schema.matching.static('deleteMatch', deleteMatch);
    Schema.matching.static('getAll', getAllMatchings);
    Schema.matching.static('decideMatch', decideMatch);

    // 경기장 스키마
    Schema.stadium = mongoose.Schema({
        name: { type: String, required: true, unique: true },
        available_type: { type: Array, required: true, unique: false },
        belong_at: { type: String, required: true, unique: false },
        max_players: { type: Number, required: true, unique: false },
        in_players: { type: Number, required: false, unique: false, default: 0 },
        matchings: { type: Array, required: false, unique: false, default: [] },
        modified_at: { type: Date, required: true, index: { unique: false }, default: Date.now }
    });

    // 호출은 getAll
    var getAllStadiums = function (callback) {
        this.find({}, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;
            callback({
                result: true,
                docs: result
            })
        });
    };

    var findUserStadium = function (belong_at, callback) {
        this.find({ belong_at: belong_at }, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;

            if (result.length == 0)
                callback({
                    result: false,
                    reason: 'NoSuchStadiumException'
                });
            else
                callback({
                    result: true,
                    stadium: result
                })
        })
    };

    var createStadium = function (stadiumInfo, callback) {
        var stadium = Model.stadium(stadiumInfo);

        stadium.save(function (err) {
            if (!mongoErrorCallbackCheck(err, callback)) return;
            callback({
                result: true
            });
        });
    };

    var prepareMatchingTeamStadium = function (stadiumInfo, callback) {
        this.findOne({ name: stadiumInfo.name }, function (err, result) {
            if (!mongoErrorCallbackCheck(err, callback)) return;

            if (!result) {
                callback({
                    result: false,
                    reason: 'NoSuchStadiumException'
                });
                return;
            }

            var stadium = result._doc;

            Model.matching.find({ matchId: { $in: stadium.matchings } }, function (err, result) {
                if (!mongoErrorCallbackCheck(err, callback)) return;
                // console.dir(result);

                // 꽉 찼는지 확인
                // 가져온 모든 매치에 대해 sum 수행.
                var pendingPlayers = 0;
                for(var key in result)
                    pendingPlayers += result[key]._doc.pendingPlayers.length;

                if (stadium.max_players > stadium.in_players - pendingPlayers) {
                    callback({
                        result: false,
                        reason: 'PreparingNotReadyException'
                    });
                    return;
                }


                // 사용자를 모두 가져온다.
                var users = [];
                for (var matchIdx in result) {
                    var document = result[matchIdx]._doc;
                    if (!document.is_team)
                        for (var playerIdx = 0; playerIdx < document.players.length; playerIdx++)
                            users.push({
                                type: 'player',
                                size: 1,
                                players: [document.players[playerIdx]]
                            });
                    else
                        users.push({
                            type: 'team',
                            size: document.players.length,
                            players: document.players
                        });
                }

                // 가져온 사용자들을 팀으로 나눈다.
                var teams = checkArrayMatchup(users);
                callback({
                    result: true,
                    leftTeam: teams.left,
                    rightTeam: teams.right
                });
            })


        });
    };

    Schema.stadium.static('getAll', getAllStadiums);
    Schema.stadium.static('createStadium', createStadium);
    Schema.stadium.static('findUserStadium', findUserStadium);
    Schema.stadium.static('prepareMatchingTeamStadium', prepareMatchingTeamStadium);

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
        if(!mongoErrorCallbackCheck(err, callback)) return;
        
        callback({
            result: true
        });
    });
};

/**
 * 매치를 확인하는 함수입니다.
 */
var checkArrayMatchup = function (arr) {
    function getArraySize(arr) {
        var size = 0;
        for (var key in arr)
            size += arr[key].size;
        return size;
    }

    var Comparator = {
        inverse: function (a, b) { return a.size == b.size ? 0 : a.size > b.size ? 1 : -1; },
        reverse: function (a, b) { return a.size == b.size ? 0 : a.size < b.size ? 1 : -1; }
    };

    var team1 = [], team2 = [];

    // 먼저 Sort를 실시한다.
    arr = JSON.parse(JSON.stringify(arr));
    arr.sort(Comparator.inverse);

    // 확인해가면서 가장 큰 Data부터 team에 넣는다.
    while (arr.length > 0)
        if (getArraySize(team1) < getArraySize(team2))
            team1.push(arr.pop());
        else
            team2.push(arr.pop());

    return {
        left: team1,
        right: team2
    };
}

/**
* 모듈 Export
*/
module.exports = {
    // Global Variables
    database: database,
    connection: mongoose.connection,
    Schema: Schema,
    Model: Model,

    // Functions
    connectDB: connectDB,
    createUser: createUser
};