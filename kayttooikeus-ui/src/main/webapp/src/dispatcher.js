import Bacon from 'baconjs'
import R from 'ramda'

export default function Dispatcher () {
  var bus = R.memoize(name => {
    return new Bacon.Bus()
  })

  this.stream = name => bus(name)
  this.push = (name, value) => bus(name).push(value)
  this.plug = (name, value) => bus(name).plug(value)
}
