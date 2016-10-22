'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var wrapperStyles = {
  position: 'fixed',
  top: 0,
  right: 0,
  bottom: 0,
  left: 0,
  background: 'rgba(0, 0, 0, 0.8)',
  zIndex: 99999,
  pointerEvents: 'auto',
  overflowY: 'auto'
};

var Modal = _react2.default.createClass({
  displayName: 'Modal',

  propTypes: {
    show: _react2.default.PropTypes.bool.isRequired,
    onClose: _react2.default.PropTypes.func.isRequired,
    closeOnOuterClick: _react2.default.PropTypes.bool.isRequired
  },

  render: function render() {
    if (this.props.show) {
      return _react2.default.createElement(
        'div',
        {
          style: wrapperStyles,
          onClick: this.hideOnOuterClick,
          'data-modal': 'true' },
        this.props.children
      );
    } else {
      return null;
    }
  },

  hideOnOuterClick: function hideOnOuterClick(e) {
    if (e.target.dataset.modal && this.props.closeOnOuterClick) {
      this.props.onClose(e);
    }
  }

});

exports.default = Modal;
