LiquidCore.on('ping', function() {
    LiquidCore.emit('pong');
});

LiquidCore.emit('ready');