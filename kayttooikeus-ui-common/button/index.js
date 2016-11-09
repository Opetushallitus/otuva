'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

require('./Button.css');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Button = _react2.default.createClass({
    displayName: 'Button',

    propTypes: {
        action: _react2.default.PropTypes.func.isRequired
    },

    render: function render() {
        var className = "button " + (this.props.className ? " " + this.props.className : "");
        return _react2.default.createElement(
            'span',
            { className: className, onClick: this.props.action },
            this.props.children
        );
    }
});
exports.default = Button;
