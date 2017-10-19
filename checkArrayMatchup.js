function checkArrayMatchup(arr) {
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

var objects = [
    { size: 1 },
    { size: 10 },
    { size: 6 },
    { size: 9 },
    { size: 12 }
];

var result = checkArrayMatchup(objects, Comparator);
console.log(getArraySize(result.left) - getArraySize(result.right));