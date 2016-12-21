'use strict';

Object.defineProperty(exports, "__esModule", {
    value: true
});

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

require('./SortByHeader.css');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var SortByHeader = _react2.default.createClass({
    displayName: 'SortByHeader',

    propTypes: {
        by: _react2.default.PropTypes.string.isRequired,
        state: _react2.default.PropTypes.object.isRequired, // {sortBy: <by-field>, direction: 'ASC'|'DESC'}
        onChange: _react2.default.PropTypes.func.isRequired // (sortBy: string, direction: 'ASC'|'DESC') => void
    },

    render: function render() {
        var className = "sortHeader" + (this.isSelected() ? " sortedBy" : "");
        return _react2.default.createElement(
            'th',
            { className: className, onClick: this.toggle },
            _react2.default.createElement(
                'span',
                { className: 'text' },
                this.props.children
            ),
            ' ',
            this.symbol()
        );
    },

    isSelected: function isSelected() {
        return this.props.by === this.props.state.sortBy;
    },

    toggle: function toggle() {
        var direction = this.isSelected() ? this.props.state.direction === 'ASC' ? 'DESC' : 'ASC' : "ASC";
        this.props.onChange(this.props.by, direction);
    },

    symbol: function symbol() {
        return _react2.default.createElement(
            'span',
            { className: 'symbol' },
            this.isSelected() ? this.props.state.direction === 'ASC' ? "∧" : "∨" : "∧"
        );
    }
});
exports.default = SortByHeader;
