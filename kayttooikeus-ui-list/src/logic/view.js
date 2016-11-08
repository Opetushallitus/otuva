import Bacon from 'baconjs'
import R from 'ramda'

import dispatcher from './dispatcher'

const d = dispatcher();

const view = {
  toProperty: (initialOrgs=[]) => {
    return Bacon.update(initialOrgs,
      [d.stream('changeCurrentView')], (current, newView) => [newView]
    )
  },
  change: toView => d.push('changeCurrentView', toView)
};

export default view
