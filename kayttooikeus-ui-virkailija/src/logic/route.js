import Bacon from 'baconjs'

import { locationP } from './location'
import { l10nP } from '../external/l10n'
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
export const routeErrorP =  Bacon.combineWith(l10nP, contentP, locationP, (l10n, content, location) => 
    content ? {} : { httpStatus: 404, comment: l10n.msg('ROUTE_NOT_FOUND', location.path)});
