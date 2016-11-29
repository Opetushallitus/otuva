import React from 'react'
import Select2 from 'select';
import $ from 'jquery'

const OrgSelect2 = React.createClass({
    render: function() {
        const props = {...this.props, passData: true};
        (props.options || (props.options = {})).templateResult 
            = result => $(`<span style="padding-left:${7*(result.level-1)}px"></span>`).text(result.visibleText);
        return (<Select2 {...props}/>)
    }
});
export default OrgSelect2;