import 'oph-urls-js';

import React from 'react'
import ReactDOM from 'react-dom'

import KutsuListView from './KutsuListView'

const state = {
    l10n: {msg: () => ""},
    kutsuListState: {
        params: {sortBy: 'AIKALEIMA', direction: 'DESC'}
    },
    kutsuList: {loaded:false, result:[]}
};

it('renders without crashing', () => {
    const div = document.createElement('div');
    ReactDOM.render(<KutsuListView {...state} />, div)
});
