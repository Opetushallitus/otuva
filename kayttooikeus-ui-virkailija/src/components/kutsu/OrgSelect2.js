import React from 'react'
import Select2 from 'select';
import R from 'ramda'
import $ from 'jquery'

const OrgSelect2 = React.createClass({
    render: function() {
        const MIN_SEARCH_CHARS = 2;
        const {data, l10n, ...props} = this.props;
        const opts = (props.options || (props.options = {}));
        opts.templateResult  = result => $(`<span style="padding-left:${7*(result.level-1)}px"></span>`).text(result.text);
        const filterData = term => org => org.id == this.props.value || (term && org.searchText && org.searchText.indexOf(term) >= 0);
        props.passData = true;
        if (data.length < 100) {
            props.data = data;
        } else {
            opts.language = {
                errorLoading: function () {
                    return l10n['VIRHE_LADATTAESSA_VASTAUKSIA'];
                },
                inputTooShort: function (args) {
                    return l10n.msg('SYOTA_VAHINTAAN_MERKKIA', args.minimum);
                },
                loadingMore: function () {
                    return l10n['LADATAAN'];
                },
                noResults: function () {
                    return l10n['EI_TULOKSIA'];
                },
                searching: function () {
                    return l10n['ETSITAAN'];
                }
            };
            opts.minimumInputLength = MIN_SEARCH_CHARS;
            opts.formatInputTooShort = function() {
                return l10n.msg('SYOTA_VAHINTAAN_MERKKIA', MIN_SEARCH_CHARS);
            };
            opts.ajax = {
                data: function(params) {
                    return params.term;
                },
                processResults: function(result) {
                    console.info('results', result);
                    return {results:result};
                },
                transport: function(params, success, failure) {
                    if (props.value || (params.data && params.data.length >= MIN_SEARCH_CHARS) || data.length < 100) {
                        setTimeout(() => success(R.filter(filterData(params.data ? params.data.toLowerCase() : null), data)), 0);
                    } else {
                        setTimeout(() => failure(), 0);
                    }
                    return {status:'0'};
                }
            };
            props.data = R.filter(filterData(null), data);
        }
        return (<Select2 {...props}/>)
    }
});
export default OrgSelect2;