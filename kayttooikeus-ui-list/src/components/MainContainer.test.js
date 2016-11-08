import React from 'react'
import ReactDOM from 'react-dom'

import MainContainer from './MainContainer'

const appState = {
  l10n: {
    'fi': { }
  },
  view: ''
};

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<MainContainer {...appState} />, div)
});
