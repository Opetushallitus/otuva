import { locationP } from './location'
import { kutsuListViewContentP } from '../components/KutsuListView'
import { anomusListViewContentP } from '../components/AnomusListView'

export const routeP = locationP.flatMapLatest(({path, queryString}) => {
    if (path === '/kutsu') {
        return kutsuListViewContentP;
    } else if (path === '/' || !path) {
        return anomusListViewContentP;
    }
}).toProperty();

export const contentP = routeP.map('.content');
export const routeErrorP = contentP.map(content => content ? {} : { httpStatus: 404, comment: 'route not found' });
