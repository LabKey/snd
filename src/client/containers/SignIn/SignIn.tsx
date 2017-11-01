/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as React from 'react';
import { Dispatch } from 'redux';
import { connect } from 'react-redux';
import { Button, ControlLabel, Panel } from 'react-bootstrap';
import { FormProps, Field, reduxForm } from 'redux-form';

import { readCookie, signInRequestAction } from './actions';
import { COOKIE_EMAIL } from './constants'

import { FieldCheckboxInput } from '../../components/Form/Checkbox';
import { EmailInput } from '../../components/Form/EmailInput';
import { PasswordInput } from '../../components/Form/PasswordInput';
import { emailValidator, requiredValidator } from '../../components/Form/validators';

interface SignInProps extends FormProps<any, any, any> {
    dispatch?: Dispatch<any>;
}

function mapStateToProps(): SignInProps {
    const email = readCookie(COOKIE_EMAIL);

    return {
        initialValues: {
            email,
            remember: email !== null
        }
    };
}

class SignInImpl extends React.Component<SignInProps, any> {

    constructor(props: any) {
        super(props);

        this.state = {
            allowSubmit: false
        };

        this.handleValidSubmit = this.handleValidSubmit.bind(this);
    }

    handleValidSubmit(values) {
        const { dispatch } = this.props;
        let saveValues = Object.assign({}, values);

        // address reduxForm regression where checkbox elements lose value attribute
        // https://github.com/erikras/redux-form/issues/2922
        if (saveValues.remember === true) {
            dispatch(signInRequestAction(values));
        }
        else {
            saveValues.remember = false;
            dispatch(signInRequestAction(saveValues));
        }
    }

    render() {
        const { handleSubmit, valid } = this.props;

        return (
            <Panel>
                <form onSubmit={handleSubmit(this.handleValidSubmit)}>
                    <h3 style={{marginTop: 0}}>Sign In</h3>
                    <div className="row" style={{marginBottom: '10px'}}>
                        <div className="col-sm-12">
                            <ControlLabel>Email</ControlLabel>
                        </div>
                        <div className="col-md-7 col-sm-9 col-xs-12">
                            <Field
                                component={EmailInput}
                                name="email"
                                required
                                validate={[requiredValidator, emailValidator]}/>
                        </div>
                    </div>
                    <div className="row" style={{marginBottom: '10px'}}>
                        <div className="col-sm-12">
                            <ControlLabel>Password</ControlLabel>
                        </div>
                        <div className="col-md-7 col-sm-9 col-xs-12">
                            <Field
                                component={PasswordInput}
                                name="password"
                                validate={requiredValidator}/>
                        </div>
                    </div>
                    <div style={{marginBottom: '10px'}}>
                        <Field
                            component={FieldCheckboxInput}
                            name="remember"
                            type="checkbox"/>
                        <ControlLabel>Remember my email address</ControlLabel>
                    </div>
                    <div>
                        <Button type="submit" disabled={!valid}>Sign In</Button>
                    </div>
                </form>
            </Panel>
        );
    }
}

const SignInForm = reduxForm({
    form: 'signIn'
})(SignInImpl);

export const SignIn = connect(mapStateToProps)(SignInForm);