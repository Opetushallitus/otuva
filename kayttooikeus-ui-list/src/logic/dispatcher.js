import Bacon from 'baconjs'
import R from 'ramda'

const dispatcher = (scope='global') => {
  const bus = R.memoize(() => {
    return new Bacon.Bus()
  });

  return {
    stream: name => bus(scope+"."+name),
    push: (name, value) => bus(scope+"."+name).push(value)
  }
};

export default dispatcher
