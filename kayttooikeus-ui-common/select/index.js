'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _reactDom = require('react-dom');

var _reactDom2 = _interopRequireDefault(_reactDom);

var _shallowEqualFuzzy = require('shallow-equal-fuzzy');

var _shallowEqualFuzzy2 = _interopRequireDefault(_shallowEqualFuzzy);

var _jquery = require('jquery');

var _jquery2 = _interopRequireDefault(_jquery);

require('select2');

require('select2/dist/css/select2.min.css');

require('./oph-select2-style.css');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var namespace = 'react-select2-wrapper';

var Select2 = _react2.default.createClass({
  displayName: 'Select2',

  propTypes: {
    defaultValue: _react.PropTypes.oneOfType([_react.PropTypes.number, _react.PropTypes.array, _react.PropTypes.string]),
    value: _react.PropTypes.oneOfType([_react.PropTypes.number, _react.PropTypes.array, _react.PropTypes.string]),
    data: _react.PropTypes.array,
    events: _react.PropTypes.array,
    options: _react.PropTypes.object,
    multiple: _react.PropTypes.bool,
    onOpen: _react.PropTypes.func,
    onClose: _react.PropTypes.func,
    onSelect: _react.PropTypes.func,
    onChange: _react.PropTypes.func,
    onUnselect: _react.PropTypes.func,
    passData: _react.PropTypes.bool
  },

  el: null,
  forceUpdateValue: false,

  componentDidMount: function componentDidMount() {
    this.initSelect2(this.props);
    this.updateValue();
  },

  componentWillReceiveProps: function componentWillReceiveProps(nextProps) {
    this.updSelect2(nextProps);
  },

  componentDidUpdate: function componentDidUpdate() {
    this.updateValue();
  },

  componentWillUnmount: function componentWillUnmount() {
    this.destroySelect2();
  },

  initSelect2: function initSelect2(props) {
    var updateValue = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
    var options = props.options;


    this.el = (0, _jquery2.default)(_reactDom2.default.findDOMNode(this));
    // fix for updating selected value when data is changing
    if (updateValue) {
      this.forceUpdateValue = true;
      this.el.off('change.' + namespace).val(null).trigger('change');
    }
    this.el.select2(this.prepareOptions(options));
    this.attachEventHandlers(props);
  },

  updSelect2: function updSelect2(props) {
    var prevProps = this.props;

    if (!(0, _shallowEqualFuzzy2.default)(prevProps.data, props.data)) {
      this.destroySelect2(false);
      this.initSelect2(props, true);
    } else {
      var options = props.options;

      if (!(0, _shallowEqualFuzzy2.default)(prevProps.options, options)) {
        this.el.select2(this.prepareOptions(options));
      }
    }

    var handlerChanged = function handlerChanged(e) {
      return prevProps[e[1]] !== props[e[1]];
    };
    if (props.events.some(handlerChanged)) {
      this.detachEventHandlers(props);
      this.attachEventHandlers(props);
    }
  },

  updateValue: function updateValue() {
    var _props = this.props,
        value = _props.value,
        defaultValue = _props.defaultValue,
        multiple = _props.multiple;

    var newValue = this.prepareValue(value, defaultValue);
    var currentValue = multiple ? this.el.val() || [] : this.el.val();

    if (!(0, _shallowEqualFuzzy2.default)(currentValue, newValue) || this.forceUpdateValue) {
      this.el.val(newValue).trigger('change');
      this.forceUpdateValue = false;
    }
  },

  destroySelect2: function destroySelect2() {
    var withCallbacks = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : true;

    if (withCallbacks) {
      this.detachEventHandlers(this.props);
    }

    this.el.select2('destroy');
    this.el = null;
  },

  attachEventHandlers: function attachEventHandlers(props) {
    var _this = this;

    props.events.forEach(function (event) {
      if (typeof props[event[1]] !== 'undefined') {
        _this.el.on(event[0], props[event[1]]);
      }
    });
  },

  detachEventHandlers: function detachEventHandlers(props) {
    var _this2 = this;

    props.events.forEach(function (event) {
      if (typeof props[event[1]] !== 'undefined') {
        _this2.el.off(event[0]);
      }
    });
  },

  prepareValue: function prepareValue(value, defaultValue) {
    var issetValue = typeof value !== 'undefined' && value !== null;
    var issetDefaultValue = typeof defaultValue !== 'undefined';

    if (!issetValue && issetDefaultValue) {
      return defaultValue;
    }
    return value;
  },

  prepareOptions: function prepareOptions(options) {
    var opt = options;
    if (typeof opt.dropdownParent === 'string') {
      opt.dropdownParent = (0, _jquery2.default)(opt.dropdownParent);
    }
    if (this.props.passData) {
      opt.data = this.props.data;
    }
    return opt;
  },

  isObject: function isObject(value) {
    var type = typeof value === 'undefined' ? 'undefined' : _typeof(value);
    return type === 'function' || value && type === 'object' || false;
  },

  makeOption: function makeOption(item) {
    if (this.isObject(item)) {
      var itemParams = _jquery2.default.extend({}, item),
          id = item.id,
          text = item.text;
      delete itemParams.id;
      delete itemParams.text;
      return _react2.default.createElement(
        'option',
        _extends({ key: 'option-' + id, value: id }, itemParams),
        text
      );
    }
    return _react2.default.createElement(
      'option',
      { key: 'option-' + item, value: item },
      item
    );
  },

  render: function render() {
    var _this3 = this;

    var props = _jquery2.default.extend({}, this.props),
        data = this.props.data,
        value = this.props.value,
        passData = this.props.passData;
    delete props.data;
    delete props.value;
    delete props.passData;
    delete props.options;
    delete props.events;
    delete props.onOpen;
    delete props.onClose;
    delete props.onSelect;
    delete props.onChange;
    delete props.onUnselect;

    return _react2.default.createElement(
      'select',
      props,
      !passData && data.map(function (item, k) {
        if (_this3.isObject(item) && _this3.isObject(item.children)) {
          var itemParams = _jquery2.default.extend({}, item),
              children = item.children,
              text = item.text;
          delete itemParams.children;
          delete itemParams.text;
          return _react2.default.createElement(
            'optgroup',
            _extends({ key: 'optgroup-' + k, label: text }, itemParams),
            children.map(function (child) {
              return _this3.makeOption(child);
            })
          );
        }
        return _this3.makeOption(item);
      })
    );
  }
});

Select2.defaultProps = {
  data: [],
  events: [['change.' + namespace, 'onChange'], ['select2:open.' + namespace, 'onOpen'], ['select2:close.' + namespace, 'onClose'], ['select2:select.' + namespace, 'onSelect'], ['select2:unselect.' + namespace, 'onUnselect']],
  options: {},
  multiple: false
};

exports.default = Select2;
