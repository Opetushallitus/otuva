'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _bind = require('classnames/bind');

var _bind2 = _interopRequireDefault(_bind);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Button = _react2.default.createClass({
    displayName: 'Button',

    propTypes: {
        action: _react2.default.PropTypes.func,
        disabled: _react2.default.PropTypes.bool,
        href: _react2.default.PropTypes.string,
        confirm: _react2.default.PropTypes.bool,
        big: _react2.default.PropTypes.bool
    },

    render: function render() {
        var className = (0, _bind2.default)({
            'oph-button': true,
            'oph-button-primary': !this.props.confirm,
            'oph-button-confirm': this.props.confirm,
            'oph-button-big': this.props.big,
            '${this.props.className}': this.props.className
        });
        return this.props.href ? _react2.default.createElement(
            'a',
            { href: this.props.href, className: 'a', onClick: this.props.action },
            this.props.children
        ) : _react2.default.createElement(
            'button',
            { className: className, disabled: this.props.disabled, onClick: !this.props.disabled ? this.props.action : function () {} },
            this.props.children
        );
    }
});

exports.default = Button;
