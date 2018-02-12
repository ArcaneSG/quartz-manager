angular.module('ff-websocket')
.factory('WebsocketServiceFactory',['$q', '$timeout', function($q, $timeout){
	
	return{
		create : function(options){
					return new BaseSocketService(options)
				 }
	};
	
	function BaseSocketService(options){

		var that = this;
		
		var defaultOpt = {
				SOCKET_URL  : '',
				TOPIC_NAME  : '',
				BROKER_NAME : '',
				RECONNECT_TIMEOUT : 30000
		};
		that.options = angular.extend(defaultOpt, options);
		
		
		var _deferred = $q.defer();
		var _messageIds = [];
		var _socket = {
			      	client: null,
			      	stomp: null
			    	};
		
		var getMessage = function(data) {
			var out = {};
			out.type = 'SUCCESS';
			out.message = JSON.parse(data.body);
			out.headers = {};
			out.headers.messageId = data.headers["message-id"];
			
			if ($.contains(_messageIds, out.headers.messageId)) {
				out.self = true;
				_messageIds = _.remove(_messageIds, out.headers.messageId);
			}
			return out;
		};

		that.reconnectionPromise = null;
		
		that.scheduleReconnection = function() {
			that.reconnectionPromise = $timeout(function() {
				console.log("Socket reconnecting... (if it fails, next attempt in " + that.options.RECONNECT_TIMEOUT + " msec)");
				_initialize();
			}, that.options.RECONNECT_TIMEOUT);
		};
		
		that.reconnectNow = function(){
			_socket.stomp.disconnect();
			if(that.reconnectionPromise && that.reconnectionPromise.cancel)
				that.reconnectionPromise.cancel();
			_initialize();
		};
		
		var _errorCallback = function(errorMsg){
			var out = {};
			out.type = 'ERROR';
			out.message = errorMsg;
			_deferred.notify(out);
			that.scheduleReconnection();
		};

		var _startListener = function(frame){
			console.log('Connected: ' + frame);
			_socket.stomp.subscribe(that.options.TOPIC_NAME, function(data){
				_deferred.notify(getMessage(data));
			});
		};
		
		var _initialize = function(){
			_socket.client = new SockJS(that.options.SOCKET_URL);
			_socket.stomp = Stomp.over(_socket.client);
			_socket.stomp.connect({}, _startListener, _errorCallback);
			_socket.stomp.onclose = that.scheduleReconnection;
		};

		that.receive = function(){
			return _deferred.promise;
		};

		that.send = function(message) {
			var id = Math.floor(Math.random() * 1000000);
			_socket.stomp.send(that.options.BROKER_NAME, {
				priority: 9
			}, JSON.stringify({
				message: message,
				id: id
			}));
			_messageIds.push(id);
		};
		
		_initialize();
	}
	    
	
}]);