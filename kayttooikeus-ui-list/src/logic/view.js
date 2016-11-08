import Bacon from 'baconjs'

import dispatcher from './dispatcher'

const d = dispatcher();

const view = {
  toProperty: (initialOrgs = []) => {
    return Bacon.update(initialOrgs,
        [d.stream('changeCurrentView')], (current, newView) => [...current, newView],
        [d.stream('back')], (current) => current.length > 2 ? [...current][0..current.length-2] : []
    )
  },
  change: toView => d.push('changeCurrentView', toView),
  back: () => d.push('back')
};

export default view
