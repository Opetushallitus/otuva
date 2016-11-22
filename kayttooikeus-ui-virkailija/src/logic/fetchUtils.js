
const queryParams = (map) => {
    const params = new URLSearchParams();
    for (var k in map) {
        if (map.hasOwnProperty(k)) {
            params.append(k, map[k]);
        }
    }
    return params.toString();
};

export const queryString = (map) => {
    const params = queryParams(map);
    if (params) {
        return '?'+params;
    }
    return '';
};
export const postFormBoby = (map) => {
    return queryParams(map);
};

export const handleFetchError = (response) => {
    if (!response.ok) {
        throw response;
    }
    return response;
};