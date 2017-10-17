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
var mongoose = require('mongoose');

var QueueSchema;
var QueueModel;

QueueSchema = mongoose.schema({
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
QueueSchema.static('findUserQueue', findUserQueue);
QueueSchema.static('findAllQueues', findAllQueues);

QueueModel = mongoose.model('queue', QueueSchema);

// 모듈 반환
module.exports = {
    QueueSchema: QueueSchema,
    QueueModel: QueueModel
};