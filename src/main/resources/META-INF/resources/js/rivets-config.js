// This Rivet.js configuration, normally it should be inside a webjar.

// from https://stackoverflow.com/a/46473808/8516495
// custom formatter for arguments
rivets.formatters.args = function (fn) {
    var args = Array.prototype.slice.call(arguments, 1);
    return function () {
        return fn.apply(null, args);
    };
};

// from example in http://jsfiddle.net/LeedsEbooks/ep6yp2bg/
// define a view-model object
function ViewModel(vmClass, vm) {
    var self = this;
    this.vmClass = vmClass;
    this.vm = vm;

    this.remote = function (operation, arg1, arg2, arg3) {
        $.ajax({
            url: '/dynamic',
            headers: {vmClass: self.vmClass, operation: operation},
            method: 'POST',
            data: {model: JSON.stringify(self.vm), arg1: arg1, arg2: arg2, arg3: arg3}
        }).done(function (vm) {
            console.log('Remote response:', vm);
            // update view-model
            self.vm = vm;
        });

        return self;
    };
}

$(document).ready(function () {
    // for each view DOM element, bind an instance of a view-model
    $('[data-vm]').each(function () {
            var view = this;
            rivets.bind(view, new ViewModel($(view).attr('data-vm'), {})
                .remote('init'));
        }
    );

});

