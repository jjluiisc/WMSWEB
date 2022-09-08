
function Map() {
    var keys = new Array();
    var data = new Object();

    this.put = function (key, value) {
        keys.push(key);
        data[key] = value;
    };

    this.get = function (key) {
        return data[key];
    };

    this.remove = function (key) {
        keys.remove(key);
        data[key] = null;
    };

    this.each = function (fn) {
        if (typeof fn!=='function') {
            return;
        }
        var len = keys.length;
        for (var i = 0; i < len; i++) {
            var k = keys[i];
            fn(k, data[k], i);
        }
    };

    this.entrys = function () {
        var len = keys.length;
        var entrys = new Array(len);
        for (var i = 0; i < len; i++) {
            entrys[i] = {
                key: keys[i],
                value: data[i]
            };
        }
        return entrys;
    };

    this.keySet = function () {
        return keys;
    };

    this.isEmpty = function () {
        return keys.length===0;
    };

    this.size = function () {
        return keys.length;
    };
}