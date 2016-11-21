import { locationP } from './location'
import { kutsuListViewContentP } from '../components/kutsu/KutsuListView'
import { anomusListViewContentP } from '../components/anomus/AnomusListView'
import { kutsuFormContentP } from '../components/kutsu/KutsuForm'

export const routeP = locationP.flatMapLatest(({path, queryString}) => {
    console.info(`PATH ${path}`);
    if (path === '/kutsu') {
        return kutsuFormContentP;
    } else if (path === '/kutsu/list') {
        return kutsuListViewContentP;
    } else if (path === '/' || !path) {
        return anomusListViewContentP;
    }
}).toProperty();

export const contentP = routeP.map('.content');
export const routeErrorP = contentP.map(content => content ? {} : { httpStatus: 404, comment: 'route not found' });
