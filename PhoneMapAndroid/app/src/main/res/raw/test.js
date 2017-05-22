LiquidCore.on('onStart', function() {
    setInterval(function() {
        LiquidCore.emit("return", "Test");
    }, 1000);
});

LiquidCore.emit('ready');