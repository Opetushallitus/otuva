import React from 'react';
import ReactDOM from 'react-dom';
import Button from 'button';

import './HenkiloAddHakatunnus.css';

export const HenkiloAddHakatunnus = React.createClass({
    propTypes: function () {
        return {
            targetElement: React.PropTypes.any.isRequired
        }
    },
    componentDidMount: function () {
        let bodyRect = document.body.getBoundingClientRect();
        let targetElementRect = this.props.targetElement.getBoundingClientRect();
        let btnOffsetTop = targetElementRect.top - bodyRect.top;
        let btnOffsetLeft = targetElementRect.left - bodyRect.left;
        let scroll = document.documentElement.scrollTop || document.body.scrollTop;
        const content = ReactDOM.findDOMNode(this.refs.content);
        content.style.top = (btnOffsetTop - content.offsetHeight - 10) - scroll + 'px';
        content.style.left = (btnOffsetLeft + (this.props.targetElement.offsetWidth / 2) - (content.offsetWidth / 2)) + 'px';

        fetch('http://localhost:3000/kayttooikeus-service/henkilo/1.2.246.562.24.25726896654/organisaatiohenkilo').then( result => {
            console.log('result', result);
        });
    },
    render: function () {
        return (
            <div ref="content" className="hakatunnus_popup_content">
                <span ref="close" onClick={console.log('ähe')} className="close"><i className="fa fa-times"></i></span>
                <h2 className="popup_header">HAKA-tunnukset</h2>
                <h3>Virkailijalla ei ole HAKA-tunnuksia</h3>
                <div>
                    <input type="text" placeholder="Lisää uusi tunnus"/>
                </div>
                <Button big action={() => {
                }}>Tallenna tunnus</Button>
            </div>
        )
    }
});