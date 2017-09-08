import * as React from 'react';
import { ControlLabel } from 'react-bootstrap'
import { connect } from 'react-redux';
import { Dispatch } from 'redux'
import { FormProps, Field, reduxForm } from 'redux-form';

import { APP_STATE_PROPS } from '../../../reducers/index'

import { PackageModel } from '../../Wizards/Packages/model'

import { TextArea } from '../../../components/Form/TextArea'
import { TextInput } from '../../../components/Form/TextInput'
import { Attributes } from '../../../components/Form/Attributes'


const styles = require<any>('./PackageForm.css');

interface PackageFormOwnProps {
    handleNarrativeChange?: (val) => void
    model?: PackageModel
    readOnly?: boolean
}

interface PackageFormState extends FormProps<any, any, any> {
    dispatch?: Dispatch<any>
}

interface PackageFormStateProps {

}

type PackageFormProps = PackageFormOwnProps & PackageFormState;

function mapStateToProps(state: APP_STATE_PROPS, ownProps: PackageFormOwnProps): PackageFormState {

    return {
        initialValues: ownProps.model
    };
}

export class PackageFormImpl extends React.Component<PackageFormProps, PackageFormStateProps> {

    constructor(props?: PackageFormProps) {
        super(props);

        this.handleNarrativeChange = this.handleNarrativeChange.bind(this);
    }

    handleNarrativeChange(event: React.ChangeEvent<HTMLTextAreaElement>) {
        const { handleNarrativeChange } = this.props;
        const value = event.currentTarget.value;

        if (value && handleNarrativeChange && typeof handleNarrativeChange === 'function') {
            handleNarrativeChange(value);
        }

    }

    renderAttributes() {
        const { model } = this.props;
        if (model) {
            const { attributes, narrative } = model;
            return <Attributes attributes={attributes} narrative={narrative}/>
        }

        return null;
    }

    render() {
        const { model, readOnly } = this.props;

        return (
            <div>
                <form>
                    <div className="row clearfix">
                        <div className="col-sm-8">
                            <div className="row clearfix">
                                <div className="col-xs-2">
                                    <ControlLabel>Package Id</ControlLabel>
                                </div>
                                <div className="col-xs-10">
                                    <ControlLabel>Description</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-2">
                                    <Field
                                        component={TextInput}
                                        disabled={readOnly}
                                        name='pkgId'/>
                                </div>
                                <div className="col-xs-10">
                                    <Field
                                        component={TextInput}
                                        disabled={readOnly}
                                        name='description'/>
                                </div>
                            </div>
                            <div className={"row clearfix " + styles['margin-top']}>
                                <div className="col-xs-12">
                                    <ControlLabel>Narrative</ControlLabel>
                                </div>
                            </div>
                            <div className="row clearfix">
                                <div className="col-xs-12">
                                    <Field
                                        component={TextArea}
                                        disabled={readOnly}
                                        name='narrative'
                                        onChange={this.handleNarrativeChange}
                                        rows={6}/>
                                </div>
                            </div>
                        </div>
                        <div className="col-sm-4">

                        </div>
                    </div>
                </form>

                    <div className="row clearfix">
                        <div className={"col-sm-12 " + styles['margin-top']}>
                            <strong>Attributes <i className="fa fa-refresh" style={{cursor: 'pointer'}}/></strong>
                        </div>
                        <div className={"col-sm-12 " + styles['margin-top']}>
                            {this.renderAttributes()}
                        </div>
                    </div>

            </div>
        )
    }
}

const PackageForm = reduxForm({
    enableReinitialize: true,
    form: 'packageForm'
})(PackageFormImpl);

export const ConnectedPackageForm = connect<PackageFormStateProps, any, PackageFormOwnProps>(mapStateToProps)(PackageForm);