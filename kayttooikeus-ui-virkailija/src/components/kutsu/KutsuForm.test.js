import 'oph-urls-js';

import React from 'react'
import ReactDOM from 'react-dom'

import KutsuForm from './KutsuForm'

const appState = {
  addedOrgs: [],
  basicInfo: {},
  l10n: {},
  locale: 'fi',
  organizationsFlatInHierarchyOrder: [
    {organisaatio: {oid: 1, tyypit: []}}
  ],
  languages: [{ code: '', name: {}}],
  omaOid: '1.2.3'
};

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<KutsuForm {...appState} />, div)
});
