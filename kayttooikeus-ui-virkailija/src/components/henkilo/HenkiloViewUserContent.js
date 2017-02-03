import './HenkiloView.css'
import React from 'react'
import Columns from 'react-columns'
import Field from 'field';
import Button from "button";

const HenkiloViewUserContent = React.createClass({
    getInitialState: function() {
        return {
            readOnly: this.props.readOnly
        }
    },
    render: function() {
        const L = this.props.l10n;
        return (
            <div>
                <Columns columns={3}>
                    <div>
                        <div className="header">
                            <h2>{L['HENKILO_PERUSTIEDOT_OTSIKKO']}</h2>
                        </div>
                        <div className="henkiloViewContent">
                            <Field readOnly={this.state.readOnly}>readonly?</Field>
                        </div>
                    </div>
                    <div>
                        <div className="header">
                            <h2>{L['HENKILO_YHTEYSTIEDOT_OTSIKKO']}</h2>
                        </div>
                        <div className="henkiloViewContent"></div>
                    </div>
                    <div>
                        <div className="header">
                            <h2>{L['HENKILO_ORGANISAATIOT_OTSIKKO']}</h2>
                        </div>
                        <div className="henkiloViewContent"></div>
                    </div>
                </Columns>
                <Button action={() => this.setState({readOnly: false})}>{L['MUOKKAA_LINKKI']}</Button>
            </div>
        )
    }
});

HenkiloViewUserContent.propTypes = {
    l10n: React.PropTypes.object.isRequired
};

export default HenkiloViewUserContent
