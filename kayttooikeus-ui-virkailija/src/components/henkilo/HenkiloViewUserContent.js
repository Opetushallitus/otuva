import './HenkiloViewUserContent.css'
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
            <div className="henkiloViewUserContentWrapper">
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
                {this.state.readOnly
                    ? <div className="henkiloViewButtons">
                        <Button action={() => this.setState({readOnly: false})}>{L['MUOKKAA_LINKKI']}</Button>
                        <Button action={() => {}}>{L['YKSILOI_LINKKI']}</Button>
                        <Button action={() => {}}>{L['PASSIVOI_LINKKI']}</Button>
                        <Button action={() => {}}>{L['LISAA_HAKA_LINKKI']}</Button>
                    </div>
                    : <div className="henkiloViewEditButtons">
                        <Button action={() => {}}>{L['TALLENNA_LINKKI']}</Button>
                        <Button action={() => {}}>{L['PERUUTA_LINKKI']}</Button>
                    </div> }
            </div>
        )
    }
});

HenkiloViewUserContent.propTypes = {
    l10n: React.PropTypes.object.isRequired
};

export default HenkiloViewUserContent
