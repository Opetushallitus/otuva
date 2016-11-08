import Bacon from 'baconjs'
import R from 'ramda'

const dispatcher = () => {
  const bus = R.memoize(name => {
    return new Bacon.Bus()
  });

  return {
    stream: name => bus(name),
    push: (name, value) => bus(name).push(value)
  }
};

export default dispatcher
