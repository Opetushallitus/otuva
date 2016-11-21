import React from "react";
import Bacon from "baconjs";
import BasicInfo from "./BasicInfo";
import AddToOrganisation from "./AddToOrganisation";
import KutsuConfirmation from "./KutsuConfirmation";
import {orgsP} from "../../external/organisations";
import {languagesP} from "../../external/languages";
import {omaOidP} from "../../external/omattiedot";
import {addedOrganizationsP} from "../../logic/organisations";
import {l10nP, localeP} from "../../external/l10n";
import {basicInfoP} from "../../logic/basicInfo";
import "./KutsuForm.css";

const KutsuForm = React.createClass({
    getInitialState: function () {
        return {
            confirmationModalOpen: false
        }
    },

    render: function () {
        const L = this.props.l10n;
        const uiLang = this.props.locale;
        const confirmationProps = {
            l10n: L,
            locale: uiLang,
            basicInfo: this.props.basicInfo,
            addedOrgs: this.props.addedOrgs,
            modalCloseFn: this.closeConfirmationModal,
            modalOpen: this.state.confirmationModalOpen
        };
        
        return (
            <form className="kutsuFormWrapper">

                <div className="kutsuFormHeader">
                    <h1>{L['VIRKAILIJAN_LISAYS_OTSIKKO']}</h1>
                </div>

                <BasicInfo l10n={L} locale={uiLang}
                           languages={this.props.languages}/>
                <AddToOrganisation l10n={L} uiLang={uiLang} omaOid={this.props.omaOid}
                                   orgs={this.props.orgs} addedOrgs={this.props.addedOrgs}/>

                <div className="kutsuFormFooter">
                    <button onClick={this.openConfirmationModal}>
                        {L['VIRKAILIJAN_LISAYS_TALLENNA']}
                    </button>
                </div>

                <KutsuConfirmation {...confirmationProps} />
            </form>
        )
    },

    openConfirmationModal: function (e) {
        e.preventDefault();
        this.setState({
            confirmationModalOpen: true
        })
    },

    closeConfirmationModal: function (e) {
        e.preventDefault();
        this.setState({
            confirmationModalOpen: false
        })
    }
});

export const kutsuFormContentP = Bacon.combineWith(l10nP, localeP, orgsP, addedOrganizationsP, basicInfoP, languagesP, omaOidP,
    (l10n, locale, orgs, addedOrgs, basicInfo, languages, omaOid) => {
        const props = {l10n, locale, orgs, addedOrgs, basicInfo, languages, omaOid};
        return {
            content: <KutsuForm {...props}/>
        };
    });

export default KutsuForm