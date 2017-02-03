'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

require('./Field.css');

var _bind = require('classnames/bind');

var _bind2 = _interopRequireDefault(_bind);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Field = _react2.default.createClass({
    displayName: 'Field',

    propTypes: {
        readOnly: _react2.default.PropTypes.bool
    },
    getInitialState: function getInitialState() {
        return {
            readOnly: true
        };
    },
    render: function render() {
        var className = (0, _bind2.default)({ 'field': true,
            '${this.props.className}': this.props.className,
            'readonly': this.props.readOnly });
        return this.props.readOnly ? _react2.default.createElement(
            'span',
            { className: className },
            this.props.children
        ) : _react2.default.createElement('input', { className: className, defaultValue: this.props.children });
    }
});
exports.default = Field;
