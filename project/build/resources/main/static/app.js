$(function () {
    var ChatManager = (function () {
        function ChatManager() {
        }

        ChatManager.textarea = $('#chat-content');
        ChatManager.socket = null;
        ChatManager.stompClient = null;
        ChatManager.sessionId = null;
        ChatManager.chatRoomId = null;
        ChatManager.joinInterval = null;

        ChatManager.join = function () {
            $.ajax({
                url: 'join',
                headers: {
                    "Content-Type": "application/json"
                },
                beforeSend: function () {
                    $('#btnJoin').text('Cancel');
                    ChatManager.updateText('waiting anonymous user', false);
                    ChatManager.joinInterval = setInterval(function () {
                        ChatManager.updateText2('.', true);
                    }, 1000);
                },
                success: function (chatResponse) {
                    console.log('Success to receive join result. \n', chatResponse);
                    if (!chatResponse) {
                        return;
                    }

                    // clearInterval(ChatManager.joinInterval);
                    if (chatResponse.responseResultStatus == 'SUCCESS') {
                        ChatManager.sessionId = chatResponse.sessionId;
                        ChatManager.chatRoomId = chatResponse.chatRoomId;
                        ChatManager.updateTemplate('chat');
                        ChatManager.updateText('>> Connected anonymous user :)\n', false);
                        ChatManager.connectAndSubscribe();
                    } else if (chatResponse.responseResultStatus == 'CANCEL') {
                        ChatManager.updateText('>> Success to cancel', false);
                        $('#btnJoin').text('Join');
                    } else if (chatResponse.responseResultStatus == 'TIMEOUT') {
                        ChatManager.updateText('>> Can`t find user :(', false);
                        $('#btnJoin').text('Join');
                    }
                },
                error: function (jqxhr) {
                    // clearInterval(ChatManager.joinInterval);
                    if (jqxhr.status == 503) {
                        ChatManager.updateText('\n>>> Failed to connect some user :(\nPlz try again', true);
                    } else {
                        ChatManager.updateText(jqxhr, true);
                    }
                    console.log(jqxhr);
                },
                complete: function () {
                    clearInterval(ChatManager.joinInterval);
                }
            })
        };

        ChatManager.cancel = function () {
            $.ajax({
                url: 'cancel',
                headers: {
                    "Content-Type": "application/json"
                },
                success: function () {
                    ChatManager.updateText('', false);
                },
                error: function (jqxhr) {
                    console.log(jqxhr);
                    alert('Error occur. please refresh');
                },
                complete: function () {
                    clearInterval(ChatManager.joinInterval);
                }
            })
        };

        ChatManager.connectAndSubscribe = function () {
            if (ChatManager.stompClient == null || !ChatManager.stompClient.connected) {    //????????? ????????? ??? ?????? ??????
                var socket = new SockJS('/chat-websocket');
                ChatManager.stompClient = Stomp.over(socket);
                ChatManager.stompClient.connect({chatRoomId: ChatManager.chatRoomId}, function (frame) {
                    console.log('Connected: ' + frame);
                    ChatManager.subscribeMessage();
                });
            } else {    //????????? ????????? ???????????? ?????? url??? ????????? ????????? ?
                ChatManager.subscribeMessage();
            }
        };

        ChatManager.disconnect = function () {
            if (ChatManager.stompClient !== null) {
                ChatManager.stompClient.disconnect();
                ChatManager.stompClient = null;
                ChatManager.updateTemplate('wait');
            }
        };

        ChatManager.sendMessage = function () {
            console.log('Check.. >>\n', ChatManager.stompClient);
            console.log('send message.. >> ');
            var $chatTarget = $('#chat-message-input');
            var message = $chatTarget.val();
            $chatTarget.val('');

            var payload = {
                messageType: 'CHAT',
                senderSessionId: ChatManager.sessionId,
                message: message
            };

            ChatManager.stompClient.send('/app/chat.message/' + ChatManager.chatRoomId, {}, JSON.stringify(payload));
        };

        ChatManager.subscribeMessage = function () {
            ChatManager.stompClient.subscribe('/topic/chat/' + ChatManager.chatRoomId, function (resultObj) {
                console.log('>> success to receive message\n', resultObj.body);
                var result = JSON.parse(resultObj.body);
                var message = '';

                if (result.messageType == 'CHAT') {
                    var date = new Date()
                    var dateMinutes = date.getMinutes()
                    var dateHours = date.getHours()
                    if (dateHours.toString().length === 1) {
                        dateHours = "0" + dateHours
                    }
                    if (dateMinutes.toString().length === 1) {
                        dateMinutes = "0" + dateMinutes
                    }
                    var dateHoursMinutes = dateHours + ":" + dateMinutes
                    if (result.senderSessionId === ChatManager.sessionId) {
                        message = message + '[Me '+ dateHoursMinutes +'] : ';
                    } else {
                        message = message + '[Anonymous '+ dateHoursMinutes +'] : ';
                    }
                    message += result.message + '\n';
                } else if (result.messageType == 'DISCONNECTED') {
                    message = '>> Disconnected user :(';
                    ChatManager.disconnect();
                }
                if (result.messageType == 'CHAT') {
                    ChatManager.updateText1(message, true, result);
                } else {
                    ChatManager.updateText(message, true);
                }
            });
        };

        ChatManager.updateTemplate = function (type) {
            var source;
            if (type == 'wait') {
                source = $('#wait-chat-template').html();
            } else if (type == 'chat') {
                source = $('#send-chat-template').html();
            } else {
                console.log('invalid type : ' + type);
                return;
            }
            var template = Handlebars.compile(source);
            var $target = $('#chat-action-div');        //?????? id??? ????????? ?????? ?????? ?????????.
            $target.empty();
            $target.append(template({}));
        };

        ChatManager.updateText = function (message, append) {
            if (append) {
                //TODO: ??????????????? ????????? ??????????????? div ????????? ?????? ????????????
                ChatManager.textarea.html(ChatManager.textarea.html() + "<br/>" + message)
            } else {
                ChatManager.textarea.text(message);
            }
        };
        ChatManager.updateText1 = function (message, append, result) {
            if (append) {
                if (result.senderSessionId === ChatManager.sessionId) {
                    //TODO: ?????? ?????? ????????? ?????? ?????????????????? message.substring(0, 3)
                    ChatManager.textarea.html(ChatManager.textarea.html()+ "<br>" + '<span id="chat-contents1" style=" float: right; display: block" readonly>'+ message +'</span>')
                } else {
                    ChatManager.textarea.html(ChatManager.textarea.html() + "<br>" +'<span id="chat-contents2" style=" float: left;display: block" readonly>'+ message +'</span>')
                }

            } else {
                ChatManager.textarea.text(message);
            }
        };

        ChatManager.updateText2 = function (message, append) {
            if (append) {
                ChatManager.textarea.text(ChatManager.textarea.text() + message)
            } else {
                ChatManager.textarea.text(message);
            }
        };

        return ChatManager;
    }());

    $(document).on('click', '#btnJoin', function () {
        var type = $(this).text();
        if (type == 'Join') {
            ChatManager.join();
        } else if (type == 'Cancel') {
            ChatManager.cancel();
        }
    });

    $(document).on('click', '#btnSend', function () {
        var input = document.getElementById('chat-message-input').value.trim()
        if (!input.toString().length) return false
        ChatManager.sendMessage();
    });
    $(document).on('keydown', '#chat-action-div', function (key) {
        if (key.keyCode == 13) {
            var input = document.getElementById('chat-message-input').value.trim()
            if (!input.toString().length) return false
            ChatManager.sendMessage();
        }
    });

    ChatManager.updateTemplate('wait');
});