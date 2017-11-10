/*
    Rivet.js configuration and customizations. Its generic and must simply
    be included in the layout HTML page.
 */

// code modified from https://stackoverflow.com/a/46473808/8516495
// inspired by https://github.com/matthieuriolo/rivetsjs-stdlib/blob/master/src/rivetsstdlib.js
// custom formatter for function calls with arguments, prevents default event handling
rivets.formatters.args = function (fn) {
    var args = Array.prototype.slice.call(arguments, 1);
    return function (evt) {
        evt.preventDefault();
        return fn.apply(null, args);
    };
};

// same as "rivets.formatters.args" formatter but does not
// prevents the original event
rivets.formatters.nav = function (fn) {
    var args = Array.prototype.slice.call(arguments, 1);
    return function (evt) {
        return fn.apply(null, args);
    };
};

// code from https://jsfiddle.net/LeedsEbooks/fba88ph9/

function ComboboxViewModel(items) {
    this.items = items;

    var self = this;
    this.start = function (el, attributes) {
        console.log(">>>>>>>>>>>>>>>>>>", el, attributes);
        // $("combobox").combobox();
        return self;
    }
}

/*
rivets.components.combobox = {
    template: function () {
       return '<select><option rv-each-item="items">{ item.name }</option></select>'
    },
    initialize: function (el, attributes) {
        return new ComboboxViewModel([{name: "foo"}, {name: "bar"}]).start(el, attributes);
    }
    
};
*/

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
            data: {arg1: arg1, arg2: arg2, arg3: arg3}
        }).done(function (vm) {
            console.log('Remote response:', vm);
            // update view-model
            self.vm = vm;
        });

        // needed to chain remote('init') call, see below
        return self;
    };
}

// once template is processed
$(document).ready(function () {

    // $('.combobox').combobox();

    // for each view DOM element, create, initialize and bind a new instance of a view-model
    $('[data-vm]').each(function () {
            var view = this;
            rivets.bind(view, new ViewModel($(view).attr('data-vm'), {})
                .remote('init'));
        }
    );

});

